/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfiguration;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfigurationBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfigurationKey;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.entry.configuration.data.location.FixedUrlBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.configuration.IrdConfigurationEntry;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.configuration.IrdConfigurationEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.Context;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.ContextKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.ResourceBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.ResourceKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTagBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.Tag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.ResourceTypeFilteredCostmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.ResourceTypeEndpointcost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.ird.rev151021.ResourceTypeIrd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.ResourceTypeFilteredNetworkmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.ResourceTypeNetworkmap;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SimpleIrdEntryListenerTest {

    private static final String DEFAULT_CONTEXT_UUID = "00000000-0000-0000-0000-000000000001";
    private static final String DEFAULT_POOL_RESOURCE = "DEFAULT_RESOURCE";
    private static final String DEFAULT_IRD_CONFIGURATION_KEY = "DEFAULT_IRD_CONFIGURATION";

    private DataBroker m_dataBroker = mock(DataBroker.class);
    private  ListenerRegistration<DataChangeListener> m_reg = mock(ListenerRegistration.class);
    private InstanceIdentifier<Context> contextIId = InstanceIdentifier.builder(Context.class, new ContextKey(new Uuid(DEFAULT_CONTEXT_UUID))).build();
    private InstanceIdentifier<Resource> resourceIID = contextIId.child(Resource.class, new ResourceKey(new ResourceId(DEFAULT_POOL_RESOURCE)));
    private static final Uuid ecUuid = new Uuid(DEFAULT_CONTEXT_UUID);
    private SimpleIrdEntryListener simpleIrdEntryListener = new SimpleIrdEntryListener(resourceIID, ecUuid);
    private ReadTransaction rx = mock(ReadTransaction.class);
    private ReadWriteTransaction rwx = mock(ReadWriteTransaction.class);
    private MockDataChangedEvent dataChangedEvent = new MockDataChangedEvent();

    @Before
    public void setUp() {
        when(m_dataBroker.registerDataChangeListener(
                any(LogicalDatastoreType.class),
                any(InstanceIdentifier.class),
                any(DataChangeListener.class),
                any(AsyncDataBroker.DataChangeScope.class)))
                .thenReturn(m_reg);

        InstanceIdentifier<IrdConfigurationEntry> iiceIID = InstanceIdentifier.builder(IrdInstanceConfiguration.class
                , new IrdInstanceConfigurationKey(new ResourceId(DEFAULT_IRD_CONFIGURATION_KEY)))
                .child(IrdConfigurationEntry.class).build();
        simpleIrdEntryListener.register(m_dataBroker, iiceIID);
    }

    @Test
    public void register() throws Exception {
        when(m_dataBroker.registerDataChangeListener(
                any(LogicalDatastoreType.class),
                any(InstanceIdentifier.class),
                any(DataChangeListener.class),
                any(AsyncDataBroker.DataChangeScope.class)))
                .thenReturn(m_reg);

        InstanceIdentifier<IrdConfigurationEntry> iiceIID = InstanceIdentifier.builder(IrdInstanceConfiguration.class
                , new IrdInstanceConfigurationKey(new ResourceId(DEFAULT_IRD_CONFIGURATION_KEY)))
                .child(IrdConfigurationEntry.class).build();
        simpleIrdEntryListener.register(m_dataBroker, iiceIID);
        verify(m_dataBroker,times(2)).registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, iiceIID
                , this.simpleIrdEntryListener, AsyncDataBroker.DataChangeScope.SUBTREE);
    }

    @Test
    public void isAcceptableContext() throws Exception {
        boolean result = simpleIrdEntryListener.isAcceptableContext(ecUuid);
        assertEquals(result, true);
    }

    @Test
    public void getValidResource() throws Exception {
        ResourceId resourceId = new ResourceId(DEFAULT_POOL_RESOURCE);
        ResourceBuilder resourceBuilder = new ResourceBuilder().setResourceId(resourceId);
        Optional<Resource> opRe = Optional.of(resourceBuilder.build());
        final CheckedFuture<Optional<Resource>,  ReadFailedException> opReFuture;
        opReFuture = Futures.immediateCheckedFuture(opRe);
        when(rx.read(LogicalDatastoreType.OPERATIONAL, resourceIID)).thenReturn(opReFuture);
        Future<Optional<Resource>> result = simpleIrdEntryListener.getValidResource(resourceId, resourceIID, rx);
        assertEquals(opReFuture, result);
    }

    @Test
    public void onDataChanged() throws Exception {
        InstanceIdentifier<IrdConfigurationEntry> iiceIID = InstanceIdentifier.builder(IrdInstanceConfiguration.class
                , new IrdInstanceConfigurationKey(new ResourceId(DEFAULT_IRD_CONFIGURATION_KEY)))
                .child(IrdConfigurationEntry.class).build();
        simpleIrdEntryListener.register(m_dataBroker, iiceIID);

        InstanceIdentifier<IrdInstanceConfiguration> configIID = InstanceIdentifier.builder(
                IrdInstanceConfiguration.class, new IrdInstanceConfigurationKey(new ResourceId("DEFAULT_CONFIG"))
        ).build();
        when(m_dataBroker.newReadWriteTransaction()).thenReturn(rwx);

        Optional<IrdInstanceConfiguration> iicOp = Optional.of(new IrdInstanceConfigurationBuilder().build());
        final CheckedFuture<Optional<IrdInstanceConfiguration>,  ReadFailedException> iicOpFu;
        iicOpFu = Futures.immediateCheckedFuture(iicOp);

        when(rwx.read(LogicalDatastoreType.CONFIGURATION, configIID)).thenReturn(iicOpFu);
        simpleIrdEntryListener.onDataChanged(dataChangedEvent);

        verify(rwx).read(any(LogicalDatastoreType.class), any(InstanceIdentifier.class));
    }

    @Test
    public void isValidResource() throws Exception {
        ResourceBuilder rb = new ResourceBuilder()
                .setKey(new ResourceKey(new ResourceId("test")))
                .setType(null);
        assertEquals(simpleIrdEntryListener.isValidResource(rb.build()), false);
    }

    @Test
    public void readEntries() throws Exception {

        List<IrdConfigurationEntry> configs = new LinkedList<>();

        ContextKey ck = new ContextKey(new Uuid(DEFAULT_CONTEXT_UUID));
        ResourceKey rk = new ResourceKey(new ResourceId("FIRST_RESOURCE"));
        ResourceKey ik = new ResourceKey(new ResourceId("FIRST_INSTANCE"));
        InstanceIdentifier<Resource> resourceIID = InstanceIdentifier.builder(Context.class, ck)
                .child(Resource.class, rk).build();
        InstanceIdentifier<Resource> instanceIID = InstanceIdentifier.builder(Context.class, ck)
                .child(Resource.class, ik).build();
        IrdConfigurationEntryBuilder configBuilder = new IrdConfigurationEntryBuilder()
                .setEntryId(new ResourceId("THE_FIRST_ENTRY_ID"))
                .setInstance(instanceIID)
                .setLocation(new FixedUrlBuilder()
                    .setUri(new Uri("http://alto.example.com/first_instance")).build());
        configs.add(configBuilder.build());
        Map<IrdConfigurationEntry, Resource> result = simpleIrdEntryListener.readEntries(configs, rwx);
        assertEquals(result, null);
    }

    @Test
    public void resolveDependency() throws Exception {
        Map<IrdConfigurationEntry, Resource> resources = new HashMap<>();
        IrdConfigurationEntryBuilder iicb = new IrdConfigurationEntryBuilder();
        List<ContextTag> contextTagList = new LinkedList<>();
        contextTagList.add(new ContextTagBuilder().setTag(new Tag("DEFAULT_CONTEXT_TAG_1")).build());
        ResourceBuilder rb = new ResourceBuilder()
                .setDefaultTag(new Tag("DEFAULT_CONTEXT_TAG_1"))
                .setContextTag(contextTagList)
                .setKey(new ResourceKey(new ResourceId("RESOURCE_1")));
        resources.put(iicb.build(), rb.build());
        simpleIrdEntryListener.resolveDependency(resources);
    }

    @Test
    public void update() throws Exception {
        List<IrdConfigurationEntry> entryList = new LinkedList<>();
        entryList.add(new IrdConfigurationEntryBuilder().build());
        List<IrdConfigurationEntry> configs = new LinkedList<>();
        ContextKey ck = new ContextKey(new Uuid(DEFAULT_CONTEXT_UUID));
        ResourceKey rk = new ResourceKey(new ResourceId("FIRST_RESOURCE"));
        ResourceKey ik = new ResourceKey(new ResourceId("FIRST_INSTANCE"));
        InstanceIdentifier<Resource> resourceIID = InstanceIdentifier.builder(Context.class, ck)
                .child(Resource.class, rk).build();
        InstanceIdentifier<Resource> instanceIID = InstanceIdentifier.builder(Context.class, ck)
                .child(Resource.class, ik).build();
        IrdConfigurationEntryBuilder configBuilder = new IrdConfigurationEntryBuilder()
                .setEntryId(new ResourceId("THE_FIRST_ENTRY_ID"))
                .setInstance(instanceIID)
                .setLocation(new FixedUrlBuilder()
                    .setUri(new Uri("http://alto.example.com/first_instance")).build());
        configs.add(configBuilder.build());
        IrdInstanceConfigurationBuilder iicb = new IrdInstanceConfigurationBuilder()
                .setIrdConfigurationEntry(configs);
        simpleIrdEntryListener.update(iicb.build(), rwx);
    }

    @Test
    public void updateIrdInstance() throws Exception {
        IrdInstanceConfigurationBuilder iicb = new IrdInstanceConfigurationBuilder();
        Map<IrdConfigurationEntry, Resource> resources = new HashMap<>();
        ContextKey ck = new ContextKey(new Uuid(DEFAULT_CONTEXT_UUID));
        ResourceKey ik = new ResourceKey(new ResourceId("FIRST_INSTANCE"));
        InstanceIdentifier<Resource> instanceIID = InstanceIdentifier.builder(Context.class, ck)
                .child(Resource.class, ik).build();
        IrdConfigurationEntryBuilder iceb = new IrdConfigurationEntryBuilder()
                .setEntryId(new ResourceId("test-ird-configuration-entry"))
                .setInstance(instanceIID);
        ResourceBuilder rb = new ResourceBuilder()
                .setKey(new ResourceKey(new ResourceId("test-resource-id")))
                .setType(ResourceTypeIrd.class);
        resources.put(iceb.build(), rb.build());
        rb.setType(ResourceTypeNetworkmap.class);
        resources.put(iceb.build(), rb.build());
        rb.setType(ResourceTypeFilteredNetworkmap.class);
        resources.put(iceb.build(), rb.build());
        rb.setType(ResourceTypeEndpointcost.class);
        resources.put(iceb.build(), rb.build());
        rb.setType(ResourceTypeFilteredCostmap.class);
        resources.put(iceb.build(), rb.build());
        Map<ResourceId, List<ResourceId>> dependencyMap = new HashMap<>();
        simpleIrdEntryListener.updateIrdInstance(iicb.build(), resources, dependencyMap, rwx);
    }

    @Test
    public void cleanupIrdInstance() throws Exception {
        IrdInstanceConfigurationBuilder iicb = new IrdInstanceConfigurationBuilder();
        simpleIrdEntryListener.cleanupIrdInstance(iicb.build(), rwx);
    }

    @Test
    public void close() throws Exception {
        simpleIrdEntryListener.close();
        verify(m_reg).close();
    }

    static class MockDataChangedEvent implements AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> {
        Map<InstanceIdentifier<?>,DataObject> created = new HashMap<>();
        Map<InstanceIdentifier<?>,DataObject> updated = new HashMap<>();
        Set<InstanceIdentifier<?>> removed = new HashSet<>();

        @Override
        public Map<InstanceIdentifier<?>, DataObject> getCreatedData() {
            return created;
        }

        @Override
        public Map<InstanceIdentifier<?>, DataObject> getUpdatedData() {
            return updated;
        }

        @Override
        public Set<InstanceIdentifier<?>> getRemovedPaths() {
            return removed;
        }

        @Override
        public Map<InstanceIdentifier<?>, DataObject> getOriginalData() {
            throw new UnsupportedOperationException("Not implemented by mock");
        }

        @Override
        public DataObject getOriginalSubtree() {
            throw new UnsupportedOperationException("Not implemented by mock");
        }

        @Override
        public DataObject getUpdatedSubtree() {
            throw new UnsupportedOperationException("Not implemented by mock");
        }
    }

}