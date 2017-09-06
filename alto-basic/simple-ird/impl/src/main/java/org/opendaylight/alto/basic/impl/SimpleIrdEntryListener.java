/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.opendaylight.alto.basic.simpleird.SimpleIrdUtils;
import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.Context;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.ContextKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.ResourceKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTagKey;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstance;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfiguration;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.IrdEntry;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.IrdEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.configuration.IrdConfigurationEntry;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.Tag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.ResourceTypeCostmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.ResourceTypeFilteredCostmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.ResourceTypeEndpointcost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.ird.rev151021.ResourceTypeIrd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.ResourceTypeFilteredNetworkmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.ResourceTypeNetworkmap;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SimpleIrdEntryListener listens only to creation/deletion/update of the
 * top-level container for simple Ird.
 *
 * When a new instance is created this listener would register a resource
 * listener to it's resource list.
 *
 * */
public class SimpleIrdEntryListener implements AutoCloseable, DataTreeChangeListener<IrdConfigurationEntry> {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleIrdEntryListener.class);

    private DataBroker m_dataBroker = null;
    private ListenerRegistration<?> m_reg = null;
    private InstanceIdentifier<IrdConfigurationEntry> m_iid = null;

    private Uuid m_context = null;
    private ResourceId m_resource = null;

    private Uuid m_entryContext = null;

    public SimpleIrdEntryListener(InstanceIdentifier<Resource> instance, Uuid entryContext) {
        m_context = instance.firstKeyOf(Context.class).getContextId();
        m_resource = instance.firstKeyOf(Resource.class).getResourceId();
        m_entryContext = entryContext;
    }

    public void register(DataBroker dataBroker, InstanceIdentifier<IrdConfigurationEntry> iid) {
        m_dataBroker = dataBroker;
        m_iid = iid;

        m_reg = m_dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, m_iid), this);

        LOG.info("SimpleIrdEntryListener registered");
    }

    protected boolean isAcceptableContext(Uuid context) {
        if (m_entryContext.equals(context) || m_context.equals(context)) {
            return true;
        }
        return false;
    }

    protected Future<Optional<Resource>> getValidResource(ResourceId rid,
                                                            InstanceIdentifier<Resource> iid,
                                                            ReadTransaction rx) {
        ContextKey contextKey = new ContextKey(m_context);
        ContextKey entryContextKey = new ContextKey(m_entryContext);
        ResourceKey resourceKey = new ResourceKey(rid);

        ContextKey iidContextKey = iid.firstKeyOf(Context.class);
        if (contextKey.equals(iidContextKey) || entryContextKey.equals(iidContextKey)) {
            if (resourceKey.equals(iid.firstKeyOf(Resource.class))) {
                return rx.read(LogicalDatastoreType.OPERATIONAL, iid);
            }
        }

        SettableFuture<Optional<Resource>> result = SettableFuture.create();
        result.set(null);

        return result;
    }

    @Override
    public synchronized void onDataTreeChanged(Collection<DataTreeModification<IrdConfigurationEntry>> changes) {
        /*
         * We examine the dependencies and check whether the services are self-contained
         */

        ReadWriteTransaction rwx = m_dataBroker.newReadWriteTransaction();

        InstanceIdentifier<IrdInstanceConfiguration> configIID = SimpleIrdUtils.getInstanceConfigurationIID(m_resource);

        Optional<IrdInstanceConfiguration> config;
        try {
            config = rwx.read(LogicalDatastoreType.CONFIGURATION, configIID).get();
        } catch (Exception e) {
            LOG.error("Failed to read configuration data. Aborting");
            return;
        }

        LOG.info("Finished reading data for {}", m_resource.getValue());

        if (config.isPresent()) {
            IrdInstanceConfiguration cfg = config.get();

            update(cfg, rwx);
        }

        rwx.submit();
    }

    protected boolean isValidResource(Resource resource) {
        if (resource.getType() == null) {
            LOG.error("Resource {} missing 'type' field", resource.getResourceId().getValue());
            return false;
        }

        //TODO resource-specific validation

        return true;
    }

    protected Map<IrdConfigurationEntry, Resource> readEntries(
                    List<IrdConfigurationEntry> configs, ReadWriteTransaction rwx) {
        Map<IrdConfigurationEntry, Future<Optional<Resource>>> futures;
        futures = new HashMap<>();

        for (IrdConfigurationEntry entry: configs) {
            ResourceId expected = entry.getEntryId();
            try {
                InstanceIdentifier<?> iid = entry.getInstance();
                if (iid == null || !iid.getTargetType().equals(Resource.class)) {
                    LOG.error("Invalid instance identifier for {}", expected.getValue());
                    return null;
                }

                if (entry.getLocation() == null) {
                    LOG.error("Entry {} missing URI information", expected.getValue());
                    return null;
                }

                InstanceIdentifier<Resource> resourceIID = (InstanceIdentifier<Resource>)iid;
                futures.put(entry, getValidResource(expected, resourceIID, rwx));
            } catch (Exception e) {
                LOG.error("Failed to read data for {}. Aborting", expected.getValue());
                return null;
            }
        }

        Map<IrdConfigurationEntry, Resource> resources;
        resources = new HashMap<>();

        for (Map.Entry<IrdConfigurationEntry, Future<Optional<Resource>>> entry: futures.entrySet()) {
            try {
                Optional<Resource> _resource = entry.getValue().get();
                if (_resource.isPresent()) {
                    Resource resource = _resource.get();

                    if (isValidResource(resource)) {
                        resources.put(entry.getKey(), resource);
                        continue;
                    }
                }
                LOG.error("Contains invalid data. Aborting.");
                return null;
            } catch (Exception e) {
                LOG.error("Failed to read data. Aborting");
                return null;
            }
        }

        return resources;
    }

    protected Map<ResourceId, List<ResourceId>> resolveDependency(
                    Map<IrdConfigurationEntry, Resource> resources) {
        Map<ResourceId, Tag> resourceMap = new HashMap<>();
        for (Resource resource: resources.values()) {
            resourceMap.put(resource.getResourceId(), resource.getDefaultTag());
        }

        Map<ResourceId, List<ResourceId>> dependencyMap;
        dependencyMap = new HashMap<>();

        for (Resource resource: resources.values()) {
            if (resource.getContextTag() == null || resource.getContextTag().isEmpty()) {
                continue;
            }

            Tag tag = resource.getDefaultTag();
            if (tag == null) {
                LOG.error("Error while reading resource {}: no default tag found!",
                                resource.getResourceId().getValue());
                return null;
            }

            List<InstanceIdentifier<?>> dependencies = null;
            for (ContextTag ctxTag: resource.getContextTag()) {
                if (tag.equals(ctxTag.getTag())) {
                    dependencies = ctxTag.getDependency();
                }
            }

            if (dependencies == null) {
                LOG.error("Error while reading resource {}: cannot find context tag matching {}",
                                resource.getResourceId().getValue(), tag.getValue());
                return null;
            }

            List<ResourceId> resourceDependencies = new LinkedList<>();
            for (InstanceIdentifier<?> iid: dependencies) {
                if (!iid.getTargetType().equals(ContextTag.class)) {
                    LOG.error("Dependencies must point to a context tag");
                    return null;
                }
                Uuid contextId = iid.firstKeyOf(Context.class).getContextId();
                ResourceId resourceId = iid.firstKeyOf(Resource.class).getResourceId();
                Tag contextTag = iid.firstKeyOf(ContextTag.class).getTag();

                if (contextId == null || resourceId == null || contextTag == null) {
                    LOG.error("Depends on a resource that doesn't exist!");
                    return null;
                }

                if (!isAcceptableContext(contextId)) {
                    LOG.error("Dependes on a resource that is not in the proper context");
                    return null;
                }

                if (!contextTag.equals(resourceMap.get(resourceId))) {
                    LOG.error("Depends on a mismatched context tag");
                    return null;
                }
                resourceDependencies.add(resourceId);
            }

            dependencyMap.put(resource.getResourceId(), resourceDependencies);
        }

        return dependencyMap;
    }

    protected void update(IrdInstanceConfiguration cfg, ReadWriteTransaction rwx) {
        List<IrdConfigurationEntry> filtered = new LinkedList<>(cfg.getIrdConfigurationEntry());

        Map<IrdConfigurationEntry, Resource> resources;
        resources = readEntries(filtered, rwx);

        try {
            if (resources == null) {
                cleanupIrdInstance(cfg, rwx);
            } else {
                Map<ResourceId, List<ResourceId>> dependencyMap = resolveDependency(resources);
                updateIrdInstance(cfg, resources, dependencyMap, rwx);
            }
        } catch (Exception e) {
            cleanupIrdInstance(cfg, rwx);
        }
    }

    protected void updateIrdInstance(IrdInstanceConfiguration cfg,
                                        Map<IrdConfigurationEntry, Resource> resources,
                                        Map<ResourceId, List<ResourceId>> dependencyMap,
                                        ReadWriteTransaction rwx) {
        List<IrdEntry> entries = new LinkedList<>();
        List<InstanceIdentifier<?>> dependencies = new LinkedList();

        for (Map.Entry<IrdConfigurationEntry, Resource> entry: resources.entrySet()) {
            IrdConfigurationEntry config = entry.getKey();
            Resource resource = entry.getValue();

            IrdEntryBuilder entryBuilder = new IrdEntryBuilder();
            entryBuilder.fieldsFrom(config);
            entryBuilder.setUses(dependencyMap.get(config.getEntryId()));

            //TODO customized conversion

            if (resource.getType().equals(ResourceTypeIrd.class)) {
                entryBuilder.setMediaType(SimpleIrdRoute.ALTO_IRD);
            } else if (resource.getType().equals(ResourceTypeNetworkmap.class)) {
                entryBuilder.setMediaType("application/alto-networkmap+json");
            } else if (resource.getType().equals(ResourceTypeFilteredNetworkmap.class)) {
                entryBuilder.setAccepts("application/alto-networkmapfilter+json");
                entryBuilder.setMediaType("application/alto-networkmap+json");
            } else if (resource.getType().equals(ResourceTypeEndpointcost.class)) {
                entryBuilder.setAccepts("application/alto-endpointcostparams+json");
                entryBuilder.setMediaType("application/alto-endpointcost+json");
            } else if (resource.getType().equals(ResourceTypeCostmap.class)) {
                entryBuilder.setMediaType("application/alto-costmap+json");
            } else if (resource.getType().equals(ResourceTypeFilteredCostmap.class)) {
                entryBuilder.setAccepts("application/alto-costmapfilter+json");
                entryBuilder.setMediaType("application/alto-costmap+json");
            } else {
                LOG.warn("Haven't implemented yet, skipping");
                continue;
            }

            entries.add(entryBuilder.build());

            InstanceIdentifier<Resource> resourceIID;
            InstanceIdentifier<ContextTag> dependencyIID;

            ContextTagKey key = new ContextTagKey(resource.getDefaultTag());
            resourceIID = (InstanceIdentifier<Resource>)config.getInstance();
            dependencyIID = resourceIID.child(ContextTag.class, key);

            dependencies.add(dependencyIID);
        }

        IrdInstanceBuilder builder = new IrdInstanceBuilder();
        builder.fieldsFrom(cfg);
        builder.setIrdEntry(entries);

        InstanceIdentifier<IrdInstance> iid = SimpleIrdUtils.getInstanceIID(m_resource);
        rwx.put(LogicalDatastoreType.OPERATIONAL, iid, builder.build());

        ResourcepoolUtils.updateResource(m_context, m_resource, dependencies, rwx);
    }

    protected void cleanupIrdInstance(IrdInstanceConfiguration cfg,
                                        ReadWriteTransaction rwx) {
        IrdInstanceBuilder builder = new IrdInstanceBuilder();
        builder.fieldsFrom(cfg);
        builder.setIrdEntry(new LinkedList<IrdEntry>());

        InstanceIdentifier<IrdInstance> iid;
        iid = SimpleIrdUtils.getInstanceIID(cfg.getInstanceId());

        rwx.put(LogicalDatastoreType.OPERATIONAL, iid, builder.build());

        ResourcepoolUtils.updateResource(m_context, m_resource, null, rwx);
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

        LOG.info("SimpleIrdEntryListener closed");
    }
}
