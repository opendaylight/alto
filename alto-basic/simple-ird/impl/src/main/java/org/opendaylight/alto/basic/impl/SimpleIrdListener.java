/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.opendaylight.alto.basic.impl.rfc7285.SimpleIrdRfc7285CostTypeListener;
import org.opendaylight.alto.basic.impl.rfc7285.SimpleIrdRfc7285DefaultNetworkMapListener;
import org.opendaylight.alto.basic.simpleird.SimpleIrdUtils;
import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.Context;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.ContextKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstance;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfiguration;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.IrdEntry;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.configuration.IrdConfigurationEntry;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.ird.rev151021.ResourceTypeIrd;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SimpleIrdListener listens only to creation/deletion/update of the
 * top-level container for simple Ird.
 *
 * When a new instance is created this listener would register a resource
 * listener to it's resource list.
 *
 * */
public class SimpleIrdListener implements AutoCloseable, DataTreeChangeListener<IrdInstanceConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleIrdListener.class);

    private DataBroker mDataBroker = null;
    private ListenerRegistration<?> mReg = null;
    private InstanceIdentifier<IrdInstanceConfiguration> mIID = null;
    private Uuid mContext = null;

    private Map<ResourceId, SimpleIrdEntryListener> mListeners = null;
    private Map<ResourceId, SimpleIrdRfc7285DefaultNetworkMapListener> mRfcListeners = null;
    private Map<ResourceId, SimpleIrdRfc7285CostTypeListener> mCostTypeListeners = null;

    public SimpleIrdListener(Uuid context) {
        mListeners = new HashMap<>();
        mRfcListeners = new HashMap<>();
        mCostTypeListeners = new HashMap<>();
        mContext = context;
    }

    public void register(DataBroker dataBroker, InstanceIdentifier<IrdInstanceConfiguration> iid) {
        mDataBroker = dataBroker;
        mIID = iid;

        mReg = mDataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, mIID), this);

        LOG.info("SimpleIrdListener registered");
    }

    @Override
    public synchronized void onDataTreeChanged(Collection<DataTreeModification<IrdInstanceConfiguration>> changes) {
        /* Update the operational data store according to the configurations.
         *
         * 1. Ignore resource updates because the registered resource are
         *    managed by another listener.  Report error when path/uuid changes.
         *
         * 2. Try to accept removals and try to accept the created.
         *
         */

        WriteTransaction wx = mDataBroker.newWriteOnlyTransaction();

        for (DataTreeModification<IrdInstanceConfiguration> change: changes) {
            final DataObjectModification<IrdInstanceConfiguration> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                case SUBTREE_MODIFIED:
                    final IrdInstanceConfiguration original = rootNode.getDataBefore();
                    final IrdInstanceConfiguration updated = rootNode.getDataAfter();
                    handleSubtreeModefication(original, updated, wx);
                    break;
                case DELETE:
                    removeIrd(rootNode.getDataBefore(), wx);
                    break;
                default:
                    break;
            }
        }

        wx.submit();
    }

    protected void handleSubtreeModefication(IrdInstanceConfiguration original,
            IrdInstanceConfiguration updated, WriteTransaction wx) {
        if (original == null) {
            createIrd(updated, wx);
        } else {
            updateIrd(original, updated, wx);
        }
    }

    protected void updateIrd(IrdInstanceConfiguration original,
                                IrdInstanceConfiguration updated, WriteTransaction wx) {
        ResourceId rid = updated.getInstanceId();

        LOG.info("Updating Ird: " + "\n\tResource ID: " + rid.getValue());
        SimpleIrdEntryListener listener = mListeners.get(rid);
        if (listener != null) {
            if (original.getEntryContext().equals(updated.getEntryContext())) {
                // Changes in resources is managed by another listener
                return ;
            }

            LOG.error("Changing entry context is forbidden!  Removing the Ird instance.");
            removeIrd(original, wx);

            return;
        }
        createIrd(updated, wx);
    }

    protected void removeIrd(IrdInstanceConfiguration cfg, WriteTransaction wx) {
        ResourceId rid = cfg.getInstanceId();

        if (cfg.getEntryContext() == null) {
            LOG.error("Configuration is invalid, no need to remove it");
            return;
        }

        LOG.info("Removing Ird: " + "\n\tResource ID: " + rid.getValue());

        SimpleIrdEntryListener listener = mListeners.get(rid);
        if (listener == null) {
            LOG.error("{} is not a valid Ird instance", rid.getValue());
            return;
        }
        mListeners.remove(rid);

        SimpleIrdRfc7285DefaultNetworkMapListener rfcListener = mRfcListeners.get(rid);
        mRfcListeners.remove(rid);

        SimpleIrdRfc7285CostTypeListener ctListener = mCostTypeListeners.get(rid);
        mCostTypeListeners.remove(rid);

        try {
            listener.close();
            rfcListener.close();
            ctListener.close();
        } catch (Exception e) {
            LOG.error("Error while closing listener", e);
        }

        ResourcepoolUtils.deleteResource(mContext, rid, wx);

        InstanceIdentifier<IrdInstance> iid = SimpleIrdUtils.getInstanceIID(rid);
        wx.delete(LogicalDatastoreType.OPERATIONAL, iid);
    }

    protected boolean isValidEntryContext(InstanceIdentifier<?> iid) {
        if (iid == null) {
            LOG.error("Failed to create Ird: Must provide an entry context");
            return false;
        }

        if (!iid.getTargetType().equals(Context.class)) {
            LOG.error("Failed to create Ird: The entry context must point to a alto-resourcepool:context");
            return false;
        }

        return true;
    }

    protected void createIrd(IrdInstanceConfiguration cfg, WriteTransaction wx) {
        ResourceId rid = cfg.getInstanceId();

        InstanceIdentifier<?> entryContextIID = cfg.getEntryContext();
        if (!isValidEntryContext(entryContextIID)) {
            return;
        }

        ContextKey key = entryContextIID.firstKeyOf(Context.class);
        if (key == null) {
            LOG.error("Failed to create Ird: The entry-context must point to a certain context");
            return;
        }
        Uuid entryContext = key.getContextId();

        LOG.info("Creating Ird: " + "\n\tResource ID: " + rid.getValue());

        List<IrdConfigurationEntry> entries = cfg.getIrdConfigurationEntry();

        if (entries != null && !entries.isEmpty()) {
            LOG.warn("Do not support adding resources while create Ird, will be ignored");
        }

        InstanceIdentifier<IrdInstance> iid = SimpleIrdUtils.getInstanceIID(rid);
        IrdInstanceBuilder builder = new IrdInstanceBuilder();
        builder.fieldsFrom(cfg);
        builder.setIrdEntry(new LinkedList<IrdEntry>());

        wx.put(LogicalDatastoreType.OPERATIONAL, iid, builder.build());

        ResourcepoolUtils.createResource(mContext, rid, ResourceTypeIrd.class, wx);
        ResourcepoolUtils.lazyUpdateResource(mContext, rid, wx);

        InstanceIdentifier<Resource> resourceIID = ResourcepoolUtils.getResourceIID(mContext, rid);

        SimpleIrdEntryListener listener;
        listener = new SimpleIrdEntryListener(resourceIID, entryContext);
        listener.register(mDataBroker, SimpleIrdUtils.getConfigEntryListIID(rid));
        mListeners.put(rid, listener);

        SimpleIrdRfc7285DefaultNetworkMapListener rfcListener;
        rfcListener = new SimpleIrdRfc7285DefaultNetworkMapListener();
        rfcListener.register(mDataBroker, rid);
        mRfcListeners.put(rid, rfcListener);

        SimpleIrdRfc7285CostTypeListener ctListener;
        ctListener = new SimpleIrdRfc7285CostTypeListener();
        ctListener.register(mDataBroker, rid);
        mCostTypeListeners.put(rid, ctListener);
    }

    @Override
    public synchronized void close() throws Exception {
        try {
            if (mReg != null) {
                mReg.close();
            }
        } catch (Exception e) {
            LOG.info("Error while closing the registration", e);
        }
        try {
            for (SimpleIrdEntryListener listener: mListeners.values()) {
                listener.close();
            }
            mListeners.clear();
        } catch (Exception e) {
            LOG.info("Error while closing the registration", e);
        }
        LOG.info("SimpleIrdListener closed");
    }
}
