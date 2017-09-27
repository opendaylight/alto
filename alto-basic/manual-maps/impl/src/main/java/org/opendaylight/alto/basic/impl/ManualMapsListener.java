/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.alto.basic.manual.maps.ManualMapsUtils;
import org.opendaylight.alto.basic.simpleird.SimpleIrdUtils;
import org.opendaylight.alto.core.northbound.route.costmap.AltoNbrCostmapUtils;
import org.opendaylight.alto.core.northbound.route.networkmap.AltoNbrNetworkmapUtils;
import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.costmap.rev151021.CostMap;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.ConfigContext;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.config.context.ResourceCostMap;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.config.context.ResourceNetworkMap;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.ServiceContext;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.ResourceTypeConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.AltoModelCostmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.ResourceTypeCostmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AltoModelNetworkmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.ResourceTypeNetworkmap;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManualMapsListener implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ManualMapsListener.class);

    private DataBroker dataBroker = null;
    private final List<ListenerRegistration<?>> listenerRegs = new ArrayList<>();
    private BindingAwareBroker.RoutedRpcRegistration<AltoModelNetworkmapService> networkmapServiceReg = null;
    private BindingAwareBroker.RoutedRpcRegistration<AltoModelCostmapService> costmapServiceReg = null;

    public void register(DataBroker dataBroker) {
        this.dataBroker = dataBroker;

        final InstanceIdentifier<ConfigContext> contextListIID = ManualMapsUtils.getContextListIID();

        listenerRegs.add(dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, contextListIID), changes -> onConfigContextChanged(changes)));

        listenerRegs.add(dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                        LogicalDatastoreType.CONFIGURATION, contextListIID.child(ResourceNetworkMap.class)),
                changes -> onNetworkMapChanged(changes)));

        listenerRegs.add(dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                        LogicalDatastoreType.CONFIGURATION, contextListIID.child(ResourceCostMap.class)),
                changes -> onCostMapChanged(changes)));
    }

    public void setNetworkmapServiceReg(BindingAwareBroker.RoutedRpcRegistration<AltoModelNetworkmapService> reg) {
        this.networkmapServiceReg = reg;
    }

    public void setCostmapServiceReg(BindingAwareBroker.RoutedRpcRegistration<AltoModelCostmapService> reg) {
        this.costmapServiceReg = reg;
    }

    @Override
    public void close() throws Exception {
        for (ListenerRegistration<?> reg : listenerRegs) {
            reg.close();
        }
    }

    private void onConfigContextChanged(Collection<DataTreeModification<ConfigContext>> changes) {
        final ReadWriteTransaction rwx = dataBroker.newReadWriteTransaction();

        for (DataTreeModification<ConfigContext> change : changes) {
            final DataObjectModification<ConfigContext> rootNode = change.getRootNode();
            final InstanceIdentifier<ConfigContext> identifier = change.getRootPath().getRootIdentifier();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    if (rootNode.getDataBefore() == null) {
                        createConfigContext(rootNode.getDataAfter(), identifier, rwx);
                        LOG.info("Create new ConfigContext data into OPERATIONAL");
                    }
                    break;
                case DELETE:
                    removeConfigContext(identifier, rootNode.getDataBefore(), rwx);
                    LOG.info("Remove ConfigContext data from OPERATIONAL");
                    break;
                default:
                    break;
            }
        }

        rwx.submit();
    }

    private void onCostMapChanged(Collection<DataTreeModification<ResourceCostMap>> changes) {
        final ReadWriteTransaction rwx = dataBroker.newReadWriteTransaction();

        for (DataTreeModification<ResourceCostMap> change : changes) {
            final DataObjectModification<ResourceCostMap> rootNode = change.getRootNode();
            final InstanceIdentifier<ResourceCostMap> identifier = change.getRootPath().getRootIdentifier();
            switch (rootNode.getModificationType()) {
                case WRITE:
                case SUBTREE_MODIFIED:
                    final ResourceCostMap original = rootNode.getDataBefore();
                    final ResourceCostMap updated = rootNode.getDataAfter();
                    handleCostMapModified(identifier, original, updated, rwx);
                    break;
                case DELETE:
                    removeCostMap(identifier, rootNode.getDataBefore(), rwx);
                    LOG.info("Remove CostMap data from OPERATIONAL");
                    break;
                default:
                    break;
            }
        }

        rwx.submit();
    }

    private void handleCostMapModified(InstanceIdentifier<ResourceCostMap> identifier, ResourceCostMap original, ResourceCostMap updated, ReadWriteTransaction rwx) {
        if (original == null) {
            createCostMap(updated, identifier, rwx);
            LOG.info("Create new CostMap data into OPERATIONAL");
        } else {
            updateCostMap(original, updated, identifier, rwx);
            LOG.info("Update CostMap data from OPERATIONAL");
        }
    }

    private void onNetworkMapChanged(Collection<DataTreeModification<ResourceNetworkMap>> changes) {
        final ReadWriteTransaction rwx = dataBroker.newReadWriteTransaction();

        for (DataTreeModification<ResourceNetworkMap> change : changes) {
            final DataObjectModification<ResourceNetworkMap> rootNode = change.getRootNode();
            final InstanceIdentifier<ResourceNetworkMap> identifier = change.getRootPath().getRootIdentifier();
            switch (rootNode.getModificationType()) {
                case WRITE:
                case SUBTREE_MODIFIED:
                    final ResourceNetworkMap original = rootNode.getDataBefore();
                    final ResourceNetworkMap updated = rootNode.getDataAfter();
                    handleNetworkMapModified(identifier, original, updated, rwx);
                    break;
                case DELETE:
                    removeNetworkMap(identifier, rootNode.getDataBefore(), rwx);
                    LOG.info("Remove NetworkMap data from OPERATIONAL");
                    break;
                default:
                    break;
            }
        }

        rwx.submit();
    }

    private void handleNetworkMapModified(InstanceIdentifier<ResourceNetworkMap> identifier,
            ResourceNetworkMap original, ResourceNetworkMap updated, ReadWriteTransaction rwx) {
        if (original == null) {
            createNetworkMap(updated, identifier, rwx);
            LOG.info("Create new NetworkMap data into OPERATIONAL");
        } else {
            updateNetworkMap(original, updated, identifier, rwx);
            LOG.info("Update NetworkMap data from OPERATIONAL");
        }
    }

    private <T extends NetworkMap> void createNetworkMap(T created,
            InstanceIdentifier<T> createdIID, final WriteTransaction wx) {
        ResourcepoolUtils.createResource(ManualMapsUtils.DEFAULT_CONTEXT,
            created.getResourceId().getValue(),
            ResourceTypeNetworkmap.class, wx);
        ResourcepoolUtils.updateResource(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT),
            created.getResourceId(), created.getTag(), null, wx);
        networkmapServiceReg.registerPath(ServiceContext.class,
            ResourcepoolUtils.getContextTagIID(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT),
                created.getResourceId(), created.getTag()));
        wx.put(LogicalDatastoreType.OPERATIONAL, createdIID, created);

        String path = created.getResourceId().getValue();
        AltoNbrNetworkmapUtils.createRecord(path, created.getResourceId(), wx);
        SimpleIrdUtils.createConfigEntry(AltoNbrNetworkmapUtils.BASE_URL + "/" + path,
            created.getResourceId(), new Uuid(ManualMapsUtils.DEFAULT_CONTEXT), wx);
    }

    private <T extends CostMap> void createCostMap(T created, InstanceIdentifier<T> createdIID,
            final WriteTransaction wx) {
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
        costmapServiceReg.registerPath(ServiceContext.class,
            ResourcepoolUtils.getContextTagIID(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT),
                created.getResourceId(), created.getTag()));
        wx.put(LogicalDatastoreType.OPERATIONAL, createdIID, created);

        String path = created.getResourceId().getValue();
        AltoNbrCostmapUtils.createRecord(path, created.getResourceId(), wx);
        SimpleIrdUtils.createConfigEntry(AltoNbrCostmapUtils.BASE_URL + "/" + path,
            created.getResourceId(), new Uuid(ManualMapsUtils.DEFAULT_CONTEXT), wx);
    }

    private <T extends NetworkMap> void updateNetworkMap(T origin, T updated,
            InstanceIdentifier<T> updatedIID, final WriteTransaction wx) {
        // TODO: origin is unused now
        ResourceId rid = updated.getResourceId();

        LOG.info("Updating NetworkMap: " + "\n\tResource ID: " + rid.getValue());
        createNetworkMap(updated, updatedIID, wx);
    }

    private <T extends CostMap> void updateCostMap(T origin, T updated,
            InstanceIdentifier<T> updatedIID, final WriteTransaction wx) {
        // TODO: origin is unused now
        ResourceId rid = updated.getResourceId();

        LOG.info("Updating CostMap: " + "\n\tResource ID: " + rid.getValue());
        createCostMap(updated, updatedIID, wx);
    }

    private <T extends NetworkMap> void removeNetworkMap(InstanceIdentifier<T> mapIID, T removed,
            final WriteTransaction wx) {
        SimpleIrdUtils.deleteConfigEntry(removed.getResourceId(), wx);
        String path = removed.getResourceId().getValue();
        AltoNbrNetworkmapUtils.deleteRecord(path, wx);

        ResourcepoolUtils.deleteResource(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT), removed.getResourceId(), wx);
        networkmapServiceReg.unregisterPath(ServiceContext.class,
                ResourcepoolUtils.getContextTagIID(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT),
                        removed.getResourceId(), removed.getTag()));
        removeMap(mapIID, wx);
    }

    private <T extends CostMap> void removeCostMap(InstanceIdentifier<T> mapIID, T removed,
            final WriteTransaction wx) {
        SimpleIrdUtils.deleteConfigEntry(removed.getResourceId(), wx);
        ManualMapsUtils.deleteResourceCostMap(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT), removed.getResourceId(), wx);
        costmapServiceReg.unregisterPath(ServiceContext.class,
                ResourcepoolUtils.getContextTagIID(new Uuid(ManualMapsUtils.DEFAULT_CONTEXT),
                        removed.getResourceId(), removed.getTag()));
        removeMap(mapIID, wx);
    }

    private void removeConfigContext(InstanceIdentifier<ConfigContext> iid, ConfigContext removed,
            final WriteTransaction wx) {
        ManualMapsUtils.deleteContext(removed.getContextId(), wx);
        removeMap(iid, wx);
        // TODO: Consistency with SimpleIrd and NrbRecord
    }

    private void removeMap(InstanceIdentifier<?> mapIID, final WriteTransaction wx) {
        wx.delete(LogicalDatastoreType.OPERATIONAL, mapIID);
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
            LOG.error("Fail to create config context!", e);
            return;
        }
        rwx.put(LogicalDatastoreType.OPERATIONAL, createdIID, created);
    }
}
