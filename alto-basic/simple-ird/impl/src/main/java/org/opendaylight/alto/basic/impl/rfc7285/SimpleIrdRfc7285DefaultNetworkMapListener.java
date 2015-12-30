/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl.rfc7285;

import java.util.concurrent.ExecutionException;

import com.google.common.base.Optional;

import org.opendaylight.alto.basic.simpleird.SimpleIrdUtils;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rev151021.IrdInstanceConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rev151021.ird.instance.IrdEntry;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rfc7285.rev151021.Rfc7285IrdConfigurationMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rfc7285.rev151021.Rfc7285IrdMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rfc7285.rev151021.Rfc7285IrdMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rfc7285.rev151021.ird.instance.MetaBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.context.Resource;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.ResourceTypeNetworkmap;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SimpleIrdRfc7285DefaultNetworkMapListener listens only to creation/deletion/update of the
 * top-level container for simple Ird.
 *
 * When a new instance is created this listener would register a resource
 * listener to it's resource list.
 *
 * */
public final class SimpleIrdRfc7285DefaultNetworkMapListener
                    implements AutoCloseable, DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleIrdRfc7285DefaultNetworkMapListener.class);

    private DataBroker m_dataBroker = null;
    private ListenerRegistration<DataChangeListener> m_reg = null;
    private InstanceIdentifier<IrdInstanceConfiguration> m_iid = null;
    private ResourceId m_instance = null;

    public void register(DataBroker dataBroker, ResourceId instanceId) {
        m_dataBroker = dataBroker;
        m_instance = instanceId;
        m_iid = SimpleIrdUtils.getInstanceConfigurationIID(instanceId);

        m_reg = m_dataBroker.registerDataChangeListener(
                LogicalDatastoreType.CONFIGURATION, m_iid,
                this, DataChangeScope.SUBTREE
        );

        LOG.info("SimpleIrdRfc7285DefaultNetworkMapListener registered");
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        ResourceId defaultNetworkmap = null;

        try {
            ReadWriteTransaction rwx = m_dataBroker.newReadWriteTransaction();
            defaultNetworkmap = onConfigurationChanged(change, rwx);

            setDefaultNetworkMap(m_instance, defaultNetworkmap, rwx);
            rwx.submit();
        } catch (Exception e) {
            LOG.error("Failed to update the default-network-map");
        }
    }

    synchronized ResourceId onConfigurationChanged(
                final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change,
                ReadTransaction rx) throws InterruptedException, ExecutionException {
        /**
         * Check if the resource exists and is a network map
         * */
        IrdInstanceConfiguration config;
        config = (IrdInstanceConfiguration)change.getUpdatedSubtree();

        Rfc7285IrdConfigurationMetadata metadata;
        metadata = config.getAugmentation(Rfc7285IrdConfigurationMetadata.class);

        ResourceId defaultNetworkMapId = metadata.getMetaConfiguration()
                                                    .getDefaultNetworkMap();
        if (defaultNetworkMapId == null) {
            return null;
        }

        LOG.info("New default-network-map: {}", defaultNetworkMapId.getValue());

        InstanceIdentifier<IrdEntry> iid;
        iid = SimpleIrdUtils.getEntryIID(m_instance, defaultNetworkMapId);

        Optional<IrdEntry> entry = rx.read(LogicalDatastoreType.OPERATIONAL, iid).get();
        if (entry.isPresent()) {
            if (entry.get().getInstance().getTargetType().equals(Resource.class)) {
                InstanceIdentifier<Resource> resourceIID;
                resourceIID = (InstanceIdentifier<Resource>)entry.get().getInstance();

                Resource resource = rx.read(LogicalDatastoreType.OPERATIONAL, resourceIID).get().get();
                if (resource.getType().equals(ResourceTypeNetworkmap.class)) {
                    return defaultNetworkMapId;
                }
            }

            LOG.error("{} is not a network map!", defaultNetworkMapId);
        } else {
            LOG.error("{} doesn't exist", defaultNetworkMapId);
        }
        return null;
    }

    public static ResourceId getDefaultNetworkMap(ResourceId instanceId, ReadTransaction rx)
                    throws InterruptedException, ExecutionException {
        InstanceIdentifier<Rfc7285IrdMetadata> iid;
        iid = SimpleIrdUtils.getInstanceIID(instanceId).augmentation(Rfc7285IrdMetadata.class);

        Optional<Rfc7285IrdMetadata> metadata = Optional.absent();
        try {
        metadata = rx.read(LogicalDatastoreType.OPERATIONAL, iid).get();
    } catch (InterruptedException | ExecutionException e) {
        throw e;
    } catch (Exception e) {
    }

        if (metadata.isPresent()) {
            return metadata.get().getMeta().getDefaultNetworkMap();
        }
        return null;
    }

    public static void setDefaultNetworkMap(ResourceId instanceId,
                                            ResourceId defaultNetworkMapId,
                                            WriteTransaction wx) {
        InstanceIdentifier<Rfc7285IrdMetadata> iid;
        iid = SimpleIrdUtils.getInstanceIID(instanceId).augmentation(Rfc7285IrdMetadata.class);

        MetaBuilder metaBuilder = new MetaBuilder();
        metaBuilder.setDefaultNetworkMap(defaultNetworkMapId);

        Rfc7285IrdMetadataBuilder builder = new Rfc7285IrdMetadataBuilder();
        builder.setMeta(metaBuilder.build());

        wx.merge(LogicalDatastoreType.OPERATIONAL, iid, builder.build());
    }

    @Override
    public synchronized void close() throws Exception {
        try {
            if (m_reg != null) {
                m_reg.close();
            }
        } catch (Exception e) {
            LOG.info("Error while closing the registration");
        }
        LOG.info("SimpleIrdRfc7285DefaultNetworkMapListener closed");
    }
}
