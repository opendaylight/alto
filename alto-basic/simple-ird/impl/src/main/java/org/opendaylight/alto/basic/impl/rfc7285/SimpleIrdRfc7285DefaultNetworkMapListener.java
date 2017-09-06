/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl.rfc7285;

import com.google.common.base.Optional;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import org.opendaylight.alto.basic.simpleird.SimpleIrdUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.IrdEntry;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.Rfc7285IrdConfigurationMetadata;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.Rfc7285IrdMetadata;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.Rfc7285IrdMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.ird.instance.MetaBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.ResourceTypeNetworkmap;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
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
                    implements AutoCloseable, DataTreeChangeListener<Rfc7285IrdConfigurationMetadata> {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleIrdRfc7285DefaultNetworkMapListener.class);

    private DataBroker m_dataBroker = null;
    private ListenerRegistration<?> m_reg = null;
    private ResourceId m_instance = null;

    public void register(DataBroker dataBroker, ResourceId instanceId) {
        m_dataBroker = dataBroker;
        m_instance = instanceId;

        m_reg = m_dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
            LogicalDatastoreType.CONFIGURATION, SimpleIrdUtils.getInstanceConfigurationIID(instanceId)
                .augmentation(Rfc7285IrdConfigurationMetadata.class)), this);

        LOG.info("SimpleIrdRfc7285DefaultNetworkMapListener registered");
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Rfc7285IrdConfigurationMetadata>> changes) {
        for (DataTreeModification<Rfc7285IrdConfigurationMetadata> change: changes) {
            final DataObjectModification<Rfc7285IrdConfigurationMetadata> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                case SUBTREE_MODIFIED:
                    Rfc7285IrdConfigurationMetadata metadata = rootNode.getDataAfter();

                    ReadWriteTransaction rwx = m_dataBroker.newReadWriteTransaction();
                    ResourceId defaultNetworkmap = onConfigurationChanged(metadata, rwx);

                    if (defaultNetworkmap != null) {
                        setDefaultNetworkMap(m_instance, defaultNetworkmap, rwx);
                        rwx.submit();
                    } else {
                        rwx.cancel();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private ResourceId onConfigurationChanged(
                final Rfc7285IrdConfigurationMetadata metadata, ReadTransaction rx) {
        /**
         * Check if the resource exists and is a network map
         * */

        ResourceId defaultNetworkMapId = metadata.getMetaConfiguration()
                                                    .getDefaultNetworkMap();
        if (defaultNetworkMapId == null) {
            return null;
        }

        LOG.info("New default-network-map: {}", defaultNetworkMapId.getValue());

        InstanceIdentifier<IrdEntry> iid;
        iid = SimpleIrdUtils.getEntryIID(m_instance, defaultNetworkMapId);

        try {
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
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Failed to read the default-network-map", e);
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
