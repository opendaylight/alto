/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.opendaylight.alto.basic.manual.maps.ManualMapsUtils;
import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.costmap.rev151021.CostMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.networkmap.rev151021.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.rev151021.ConfigContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.ServiceContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.ResourceTypeConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.AltoModelCostmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.ResourceTypeCostmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AltoModelNetworkmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.ResourceTypeNetworkmap;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ManualMapsListener implements AutoCloseable, DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(ManualMapsListener.class);

    private DataBroker m_dataBroker = null;
    private InstanceIdentifier<ConfigContext> m_iid = null;
    private ListenerRegistration<DataChangeListener> m_reg = null;
    private BindingAwareBroker.RoutedRpcRegistration<AltoModelNetworkmapService> m_networkmapServiceReg = null;
    private BindingAwareBroker.RoutedRpcRegistration<AltoModelCostmapService> m_costmapServiceReg = null;

    public ManualMapsListener() {
        m_iid = ManualMapsUtils.getContextListIID();
    }

    public void register(DataBroker dataBroker) {
        m_dataBroker = dataBroker;

        m_reg = m_dataBroker.registerDataChangeListener(
                LogicalDatastoreType.CONFIGURATION, m_iid,
                this, AsyncDataBroker.DataChangeScope.SUBTREE
        );
    }

    public void setNetworkmapServiceReg(BindingAwareBroker.RoutedRpcRegistration<AltoModelNetworkmapService> reg) {
        this.m_networkmapServiceReg = reg;
    }

    public void setCostmapServiceReg(BindingAwareBroker.RoutedRpcRegistration<AltoModelCostmapService> reg) {
        this.m_costmapServiceReg = reg;
    }

    @Override
    public void close() throws Exception {
        try {
            if (m_reg != null) {
                m_reg.close();
            }
        } catch (Exception e) {
            LOG.info("Error while closing the registration");
        }
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        final ReadWriteTransaction rwx = m_dataBroker.newReadWriteTransaction();

        Map<InstanceIdentifier<?>, DataObject> original = change.getOriginalData();

        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change.getUpdatedData().entrySet()) {
            DataObject origin = original.get(entry.getKey());
            DataObject updated = entry.getValue();

            if (updated instanceof NetworkMap) {
                updateNetworkMap((NetworkMap) origin, (NetworkMap) updated);
            } else if (updated instanceof CostMap) {
                updateCostMap((CostMap) origin, (CostMap) updated);
            }
        }

        for (InstanceIdentifier<?> iid : change.getRemovedPaths()) {
            DataObject removed = original.get(iid);

            if (removed instanceof NetworkMap) {
                removeNetworkMap((InstanceIdentifier<NetworkMap>) iid, (NetworkMap) removed, rwx);
                LOG.info("Remove NetworkMap data from OPERATIONAL");
            } else if (removed instanceof CostMap) {
                removeCostMap((InstanceIdentifier<CostMap>) iid, (CostMap) removed, rwx);
                LOG.info("Remove CostMap data from OPERATIONAL");
            } else if (removed instanceof ConfigContext) {
                removeConfigContext((InstanceIdentifier<ConfigContext>) iid, (ConfigContext) removed, rwx);
                LOG.info("Remove ConfigContext data from OPERATIONAL");
            }
        }

        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change.getCreatedData().entrySet()) {
            InstanceIdentifier<?> createdIID = entry.getKey();
            DataObject created = entry.getValue();

            if (created instanceof NetworkMap) {
                createNetworkMap((NetworkMap) created, (InstanceIdentifier<NetworkMap>) createdIID, rwx);
                LOG.info("Create new NetworkMap data into OPERATIONAL");
            } else if (created instanceof CostMap) {
                createCostMap((CostMap) created, (InstanceIdentifier<CostMap>) createdIID, rwx);
                LOG.info("Create new CostMap data into OPERATIONAL");
            } else if (created instanceof ConfigContext) {
                createConfigContext((ConfigContext) created, (InstanceIdentifier<ConfigContext>) createdIID, rwx);
                LOG.info("Create new ConfigContext data into OPERATIONAL");
            }
        }

        rwx.submit();
    }

    private void updateNetworkMap(NetworkMap origin, NetworkMap updated) {
        //TODO: No Implementation
    }

    private void updateCostMap(CostMap origin, CostMap updated) {
        //TODO: No Implementation
    }

    private void removeNetworkMap(InstanceIdentifier<NetworkMap> mapIID, NetworkMap removed, final WriteTransaction wx) {
        ResourcepoolUtils.deleteResource(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT), removed.getResourceId(), wx);
        m_networkmapServiceReg.unregisterPath(ServiceContext.class,
                ResourcepoolUtils.getContextTagIID(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT),
                        removed.getResourceId(), removed.getTag()));
        removeMap(mapIID, wx);
    }

    private void removeCostMap(InstanceIdentifier<CostMap> mapIID, CostMap removed, final WriteTransaction wx) {
        ManualMapsUtils.deleteResourceCostMap(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT), removed.getResourceId(), wx);
        m_costmapServiceReg.unregisterPath(ServiceContext.class,
                ResourcepoolUtils.getContextTagIID(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT),
                        removed.getResourceId(), removed.getTag()));
        removeMap(mapIID, wx);
    }

    private void removeConfigContext(InstanceIdentifier<ConfigContext> iid, ConfigContext removed, final WriteTransaction wx) {
        ManualMapsUtils.deleteContext(removed.getContextId(), wx);
        removeMap(iid, wx);
    }

    private void removeMap(InstanceIdentifier<?> mapIID, final WriteTransaction wx) {
        wx.delete(LogicalDatastoreType.OPERATIONAL, mapIID);
    }

    private void createNetworkMap(NetworkMap created, InstanceIdentifier<NetworkMap> createdIID, final WriteTransaction wx) {
        ResourcepoolUtils.createResource(ManualMapsUtils.DEFAULT_CONTEXT,
                created.getResourceId().getValue(),
                ResourceTypeNetworkmap.class, wx);
        ResourcepoolUtils.updateResource(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT),
                created.getResourceId(), created.getTag(), null, wx);
        m_networkmapServiceReg.registerPath(ServiceContext.class,
                ResourcepoolUtils.getContextTagIID(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT),
                        created.getResourceId(), created.getTag()));
        wx.put(LogicalDatastoreType.OPERATIONAL, createdIID, created);
    }

    private void createCostMap(CostMap created, InstanceIdentifier<CostMap> createdIID, final WriteTransaction wx) {
        ResourcepoolUtils.createResource(ManualMapsUtils.DEFAULT_CONTEXT,
                created.getResourceId().getValue(),
                ResourceTypeCostmap.class, wx);
        List<InstanceIdentifier<?>> dependencies = new LinkedList<>();
        dependencies.add(
                ResourcepoolUtils.getContextTagIID(
                        new Uuid(ResourcepoolUtils.DEFAULT_CONTEXT),
                        created.getMeta().getDependentVtags().get(0).getResourceId(),
                        created.getMeta().getDependentVtags().get(0).getTag())
        );
        ResourcepoolUtils.updateResource(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT),
                created.getResourceId(), created.getTag(), dependencies, wx);
        m_costmapServiceReg.registerPath(ServiceContext.class,
                ResourcepoolUtils.getContextTagIID(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT),
                        created.getResourceId(), created.getTag()));
        wx.put(LogicalDatastoreType.OPERATIONAL, createdIID, created);
    }

    private void createConfigContext(ConfigContext created, InstanceIdentifier<ConfigContext> createdIID, final ReadWriteTransaction rwx) {
        try {
            if (!ResourcepoolUtils.contextExists(created.getContextId(), rwx)) {
                ResourcepoolUtils.createResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                        created.getContextId().getValue(),
                        ResourceTypeConfig.class, rwx);
                ResourcepoolUtils.createContext(created.getContextId(), rwx);
            }
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
        rwx.put(LogicalDatastoreType.OPERATIONAL, createdIID, created);
    }
}
