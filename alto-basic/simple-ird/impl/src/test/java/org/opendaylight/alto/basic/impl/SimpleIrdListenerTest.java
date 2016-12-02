/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.basic.impl;


import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfiguration;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfigurationBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfigurationKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.Context;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.ContextKey;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SimpleIrdListenerTest {

    private DataBroker m_dataBroker = mock(DataBroker.class);
    private ListenerRegistration<DataChangeListener> m_reg = mock(ListenerRegistration.class);
    private SimpleIrdListener simpleIrdListener = new SimpleIrdListener(new Uuid(DEFAULT_UUID));
    private WriteTransaction wx = mock(WriteTransaction.class);
    private ReadWriteTransaction rwx = mock(ReadWriteTransaction.class);
    private MockDataChangedEvent dataChangedEvent = new MockDataChangedEvent();

    private static final String DEFAULT_UUID = "00000000-0000-0000-0000-000000000001";
    private static final String DEFAULT_CONTEXT_UUID = "00000000-0000-0000-0000-000000000001";
    private static String ORIGINAL_INSTANCE_ID="ORIGINAL_INSTANCE_ID";
    private static String UPDATED_INSTANCE_ID="UPDATED_INSTANCE_ID";
    private static String DEFAULT_INSTANCE_ID="DEFAULT_INSTANCE_ID";

    @Before
    public void setUp() {
        when(m_dataBroker.registerDataChangeListener(
                any(LogicalDatastoreType.class),
                any(InstanceIdentifier.class),
                any(DataChangeListener.class),
                any(AsyncDataBroker.DataChangeScope.class)))
                .thenReturn(m_reg);

        InstanceIdentifier<IrdInstanceConfiguration> iicIID = InstanceIdentifier.builder(IrdInstanceConfiguration.class).build();
        simpleIrdListener.register(m_dataBroker, iicIID);
    }

    @Test
    public void register() throws Exception {
        when(m_dataBroker.registerDataChangeListener(
                any(LogicalDatastoreType.class),
                any(InstanceIdentifier.class),
                any(DataChangeListener.class),
                any(AsyncDataBroker.DataChangeScope.class)))
                .thenReturn(m_reg);

        InstanceIdentifier<IrdInstanceConfiguration> iicIID = InstanceIdentifier.builder(IrdInstanceConfiguration.class).build();
        simpleIrdListener.register(m_dataBroker, iicIID);
        verify(m_dataBroker, times(2)).registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, iicIID
                , this.simpleIrdListener, AsyncDataBroker.DataChangeScope.ONE);

    }

    @Test
    public void onDataChanged() throws Exception {
        when(m_dataBroker.newWriteOnlyTransaction()).thenReturn(wx);
        simpleIrdListener.onDataChanged(dataChangedEvent);
        //TBD
    }

    @Test
    public void updateIrd() throws Exception {
        IrdInstanceConfigurationBuilder originalIICB = new IrdInstanceConfigurationBuilder()
                .setInstanceId(new ResourceId(ORIGINAL_INSTANCE_ID));
        IrdInstanceConfigurationBuilder updatedIICB = new IrdInstanceConfigurationBuilder()
                .setInstanceId(new ResourceId(UPDATED_INSTANCE_ID));
        simpleIrdListener.updateIrd(originalIICB.build(), updatedIICB.build(), wx);

    }

    @Test
    public void removeIrd() throws Exception {
        IrdInstanceConfigurationBuilder iicb = new IrdInstanceConfigurationBuilder()
                .setInstanceId(new ResourceId("test-instance-id"));
        simpleIrdListener.removeIrd(iicb.build(), wx);
    }

    @Test
    public void isValidEntryContext() throws Exception {
        InstanceIdentifier<Context> contextIID = InstanceIdentifier
                .builder(Context.class, new ContextKey(new Uuid(DEFAULT_CONTEXT_UUID))).build();
        assertEquals(simpleIrdListener.isValidEntryContext(contextIID), true);
    }

    @Test
    public void createIrd() throws Exception {
        InstanceIdentifier<Context> contextIId = InstanceIdentifier.builder(Context.class, new ContextKey(new Uuid(DEFAULT_CONTEXT_UUID))).build();
        IrdInstanceConfigurationBuilder iicb = new IrdInstanceConfigurationBuilder()
                .setInstanceId(new ResourceId(DEFAULT_INSTANCE_ID))
                .setEntryContext(contextIId);
        simpleIrdListener.createIrd(iicb.build(), wx);
    }

    @Test
    public void close() throws Exception {
        simpleIrdListener.close();
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
            Map<InstanceIdentifier<?>, DataObject> updated = new HashMap<>();
            InstanceIdentifier<IrdInstanceConfiguration> iicIID = InstanceIdentifier.builder(
                    IrdInstanceConfiguration.class, new IrdInstanceConfigurationKey(new ResourceId("UPDATED_CONFIG"))).build();
            IrdInstanceConfigurationBuilder iicb = new IrdInstanceConfigurationBuilder()
                    .setKey(new IrdInstanceConfigurationKey(new ResourceId("UPDATED_CONFIG")));
            updated.put(iicIID, iicb.build());

            return updated;
        }

        @Override
        public Set<InstanceIdentifier<?>> getRemovedPaths() {
            return removed;
        }

        @Override
        public Map<InstanceIdentifier<?>, DataObject> getOriginalData() {
            Map<InstanceIdentifier<?>, DataObject> originaled = new HashMap<>();
            InstanceIdentifier<IrdInstanceConfiguration> iicIID = InstanceIdentifier.builder(
                    IrdInstanceConfiguration.class, new IrdInstanceConfigurationKey(new ResourceId("ORIGINAL_CONFIG"))).build();
            IrdInstanceConfigurationBuilder iicb = new IrdInstanceConfigurationBuilder()
                    .setKey(new IrdInstanceConfigurationKey(new ResourceId("ORIGINAL_CONFIG")));
            originaled.put(iicIID, iicb.build());
            return originaled;
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