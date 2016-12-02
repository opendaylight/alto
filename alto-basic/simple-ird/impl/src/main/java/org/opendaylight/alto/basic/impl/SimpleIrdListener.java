/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opendaylight.alto.basic.impl.rfc7285.SimpleIrdRfc7285CostTypeListener;
import org.opendaylight.alto.basic.impl.rfc7285.SimpleIrdRfc7285DefaultNetworkMapListener;
import org.opendaylight.alto.basic.simpleird.SimpleIrdUtils;
import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;

import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstance;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfiguration;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.IrdEntry;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.configuration.IrdConfigurationEntry;

import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.Context;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.ContextKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;

import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.ird.rev151021.ResourceTypeIrd;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
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
public class SimpleIrdListener implements AutoCloseable, DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleIrdListener.class);

    private DataBroker m_dataBroker = null;
    private ListenerRegistration<DataChangeListener> m_reg = null;
    private InstanceIdentifier<IrdInstanceConfiguration> m_iid = null;
    private Uuid m_context = null;

    private Map<ResourceId, SimpleIrdEntryListener> m_listeners = null;
    private Map<ResourceId, SimpleIrdRfc7285DefaultNetworkMapListener> m_rfcListeners = null;
    private Map<ResourceId, SimpleIrdRfc7285CostTypeListener> m_ctListeners = null;

    public SimpleIrdListener(Uuid context) {
        m_listeners = new HashMap<ResourceId, SimpleIrdEntryListener>();
        m_rfcListeners = new HashMap<ResourceId, SimpleIrdRfc7285DefaultNetworkMapListener>();
        m_ctListeners = new HashMap<ResourceId, SimpleIrdRfc7285CostTypeListener>();
        m_context = context;
    }

    public void register(DataBroker dataBroker, InstanceIdentifier<IrdInstanceConfiguration> iid) {
        m_dataBroker = dataBroker;
        m_iid = iid;

        m_reg = m_dataBroker.registerDataChangeListener(
                LogicalDatastoreType.CONFIGURATION, m_iid,
                this, DataChangeScope.ONE
        );

        LOG.info("SimpleIrdListener registered");
    }

    @Override
    public synchronized void onDataChanged(
                final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        /**
         * Update the operational data store according to the configurations.
         *
         * 1. Ignore resource updates because the registered resource are
         *    managed by another listener.  Report error when path/uuid changes.
         *
         * 2. Try to accept removals and try to accept the created.
         *
         * */

        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        Map<InstanceIdentifier<?>, DataObject> original = change.getOriginalData();

        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry: change.getUpdatedData().entrySet()) {
            DataObject _origin = original.get(entry.getKey());
            DataObject _updated = entry.getValue();

            if (!(_origin instanceof IrdInstanceConfiguration)) {
                continue;
            }
            if (!(_updated instanceof IrdInstanceConfiguration)) {
                continue;
            }

            IrdInstanceConfiguration origin = (IrdInstanceConfiguration)_origin;
            IrdInstanceConfiguration updated = (IrdInstanceConfiguration)_updated;

            updateIrd(origin, updated, wx);
        }

        for (InstanceIdentifier<?> iid: change.getRemovedPaths()) {
            DataObject _removed = original.get(iid);

            if (!(_removed instanceof IrdInstanceConfiguration)) {
                continue;
            }

            IrdInstanceConfiguration removed = (IrdInstanceConfiguration)_removed;
            removeIrd(removed, wx);
        }

        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry: change.getCreatedData().entrySet()) {
            DataObject _created = entry.getValue();

            if (!(_created instanceof IrdInstanceConfiguration)) {
                continue;
            }

            IrdInstanceConfiguration created = (IrdInstanceConfiguration)_created;
            createIrd(created, wx);
        }

        wx.submit();
    }

    protected void updateIrd(IrdInstanceConfiguration original,
                                IrdInstanceConfiguration updated, WriteTransaction wx) {
        ResourceId rid = updated.getInstanceId();

        LOG.info("Updating Ird: " + "\n\tResource ID: " + rid.getValue());
        SimpleIrdEntryListener listener = m_listeners.get(rid);
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

        SimpleIrdEntryListener listener = m_listeners.get(rid);
        if (listener == null) {
            LOG.error("{} is not a valid Ird instance", rid.getValue());
            return;
        }
        m_listeners.remove(rid);

        SimpleIrdRfc7285DefaultNetworkMapListener rfcListener = m_rfcListeners.get(rid);
        m_rfcListeners.remove(rid);

        SimpleIrdRfc7285CostTypeListener ctListener = m_ctListeners.get(rid);
        m_ctListeners.remove(rid);

        try {
            listener.close();
            rfcListener.close();
            ctListener.close();
        } catch (Exception e) {
            LOG.error("Error while closing listener");
        }

        ResourcepoolUtils.deleteResource(m_context, rid, wx);

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
        }
        Uuid entryContext = key.getContextId();

        LOG.info("Creating Ird: " + "\n\tResource ID: " + rid.getValue());

        List<IrdConfigurationEntry> entries = cfg.getIrdConfigurationEntry();

        if ((entries != null) && (!entries.isEmpty())) {
            LOG.warn("Do not support adding resources while create Ird, will be ignored");
        }

        InstanceIdentifier<IrdInstance> iid = SimpleIrdUtils.getInstanceIID(rid);
        IrdInstanceBuilder builder = new IrdInstanceBuilder();
        builder.fieldsFrom(cfg);
        builder.setIrdEntry(new LinkedList<IrdEntry>());

        wx.put(LogicalDatastoreType.OPERATIONAL, iid, builder.build());

        ResourcepoolUtils.createResource(m_context, rid, ResourceTypeIrd.class, wx);
        ResourcepoolUtils.lazyUpdateResource(m_context, rid, wx);

        InstanceIdentifier<Resource> resourceIID = ResourcepoolUtils.getResourceIID(m_context, rid);

        SimpleIrdEntryListener listener;
        listener = new SimpleIrdEntryListener(resourceIID, entryContext);
        listener.register(m_dataBroker, SimpleIrdUtils.getConfigEntryListIID(rid));
        m_listeners.put(rid, listener);

        SimpleIrdRfc7285DefaultNetworkMapListener rfcListener;
        rfcListener = new SimpleIrdRfc7285DefaultNetworkMapListener();
        rfcListener.register(m_dataBroker, rid);
        m_rfcListeners.put(rid, rfcListener);

        SimpleIrdRfc7285CostTypeListener ctListener;
        ctListener = new SimpleIrdRfc7285CostTypeListener();
        ctListener.register(m_dataBroker, rid);
        m_ctListeners.put(rid, ctListener);
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
        try {
            for (SimpleIrdEntryListener listener: m_listeners.values()) {
                listener.close();
            }
            m_listeners.clear();
        } catch (Exception e) {
            LOG.info("Error while closing the registration");
        }
        LOG.info("SimpleIrdListener closed");
    }
}
