/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl.rfc7285;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.opendaylight.alto.basic.simpleird.SimpleIrdUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rev151021.IrdInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rev151021.ird.entry.data.EntryCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rev151021.ird.instance.IrdEntry;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rfc7285.rev151021.Rfc7285CostTypeCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rfc7285.rev151021.Rfc7285CostTypeCapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rfc7285.rev151021.Rfc7285IrdMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rfc7285.rev151021.Rfc7285IrdMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rfc7285.rev151021.ird.instance.MetaBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rfc7285.rev151021.rfc7285.ird.meta.CostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rfc7285.rev151021.rfc7285.ird.meta.CostTypeBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.context.Resource;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.CostTypeData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.ResourceTypeCostmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.ResourceTypeFilteredCostmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.ResourceTypeEndpointcost;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.CapabilitiesCostType;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SimpleIrdRfc7285CostTypeListener listens only to creation/deletion/update of the
 * top-level container for simple Ird.
 *
 * When a new instance is created this listener would register a resource
 * listener to it's resource list.
 *
 * */
public final class SimpleIrdRfc7285CostTypeListener
                    implements AutoCloseable, DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleIrdRfc7285CostTypeListener.class);

    private DataBroker m_dataBroker = null;
    private ListenerRegistration<DataChangeListener> m_reg = null;
    private InstanceIdentifier<IrdEntry> m_iid = null;
    private ResourceId m_instance = null;

    public void register(DataBroker dataBroker, ResourceId instanceId) {
        m_dataBroker = dataBroker;
        m_instance = instanceId;
        m_iid = SimpleIrdUtils.getInstanceIID(instanceId).child(IrdEntry.class);

        m_reg = m_dataBroker.registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL, m_iid,
                this, DataChangeScope.SUBTREE
        );

        LOG.info("SimpleIrdRfc7285CostTypeListener registered");
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        final ReadWriteTransaction rwx = m_dataBroker.newReadWriteTransaction();

        try {
            Map<String, CostType> costTypes = onEntryChanged(rwx);
            List<CostType> costTypeList = new LinkedList<>(costTypes.values());
            setCostTypes(m_instance, costTypeList, rwx);

            rwx.submit();
        } catch (Exception e) {
            LOG.error("Failed to update cost-types {}", m_instance);
            e.printStackTrace();
        }
    }

    protected Map<String, CostType> onEntryChanged(final ReadWriteTransaction rwx)
                throws InterruptedException, ExecutionException {
        InstanceIdentifier<IrdInstance> iid = SimpleIrdUtils.getInstanceIID(m_instance);
        IrdInstance instance = rwx.read(LogicalDatastoreType.OPERATIONAL, iid).get().get();

        Map<String, CostType> result = new HashMap<String, CostType>();
        for (IrdEntry entry: instance.getIrdEntry()) {
            InstanceIdentifier<Resource> resourceIID;
            resourceIID = (InstanceIdentifier<Resource>)entry.getInstance();

            Resource resource = rwx.read(LogicalDatastoreType.OPERATIONAL, resourceIID).get().get();
            if (resource.getType().equals(ResourceTypeEndpointcost.class)
                    || resource.getType().equals(ResourceTypeCostmap.class)
                    || resource.getType().equals(ResourceTypeFilteredCostmap.class)) {
                CapabilitiesCostType capabilities;
                capabilities = resource.getCapabilities()
                    .getAugmentation(CapabilitiesCostType.class);

                if ((capabilities == null) || (capabilities.getCostType() == null)
                        || (capabilities.getCostType().isEmpty())) {
                    LOG.warn("Missing cost-type information in {}", resource.getResourceId());
                    continue;
                }

                List<String> costTypeNames = new LinkedList<>();
                for (CostTypeData costType: capabilities.getCostType()) {
                    String name = costType.getCostMetric().getValue() + "-" + costType.getCostMode();

                    costTypeNames.add(name);

                    if (result.containsKey(name)) {
                        continue;
                    }

                    CostTypeBuilder builder = new CostTypeBuilder();
                    builder.fieldsFrom(costType);
                    builder.setName(name);

                    result.put(name, builder.build());
                }

                boolean supportConstraint = false;

                if (capabilities.isConstraintSupport() != null) {
                    supportConstraint = capabilities.isConstraintSupport();
                }
                setCostTypeNames(m_instance, entry.getEntryId(),
                        costTypeNames, supportConstraint, rwx);
            } else {
                continue;
            }
        }
        return result;
    }

    public static void setCostTypeNames(ResourceId instance,
                                        ResourceId resource, List<String> names,
                                        boolean supportConstraint, final WriteTransaction wx) {
        InstanceIdentifier<Rfc7285CostTypeCapabilities> iid;
        iid = SimpleIrdUtils.getEntryIID(instance, resource)
                            .child(EntryCapabilities.class)
                            .augmentation(Rfc7285CostTypeCapabilities.class);

        Rfc7285CostTypeCapabilitiesBuilder builder = new Rfc7285CostTypeCapabilitiesBuilder();
        builder.setCostTypeNames(names);
        builder.setCostConstraints(supportConstraint);

        wx.put(LogicalDatastoreType.OPERATIONAL, iid, builder.build());
    }

    public static void setCostTypes(ResourceId instance, List<CostType> costTypes,
                                        final ReadWriteTransaction rwx)
                                        throws InterruptedException, ExecutionException {
        InstanceIdentifier<Rfc7285IrdMetadata> iid;
        iid = SimpleIrdUtils.getInstanceIID(instance).augmentation(Rfc7285IrdMetadata.class);

        ResourceId defaultNetworkMap;
        defaultNetworkMap = SimpleIrdRfc7285DefaultNetworkMapListener.getDefaultNetworkMap(instance, rwx);

        MetaBuilder metaBuilder = new MetaBuilder();
        metaBuilder.setDefaultNetworkMap(defaultNetworkMap);
        metaBuilder.setCostType(costTypes);

        Rfc7285IrdMetadataBuilder builder = new Rfc7285IrdMetadataBuilder();
        builder.setMeta(metaBuilder.build());

        rwx.put(LogicalDatastoreType.OPERATIONAL, iid, builder.build());
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
        LOG.info("SimpleIrdRfc7285CostTypeListener closed");
    }
}
