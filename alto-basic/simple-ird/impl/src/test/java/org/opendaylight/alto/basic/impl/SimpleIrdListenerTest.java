/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.basic.impl;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.Context;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.ContextKey;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfiguration;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfigurationBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfigurationKey;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SimpleIrdListenerTest {

    private final DataBroker m_dataBroker = mock(DataBroker.class);
    private final ListenerRegistration<?> m_reg = mock(ListenerRegistration.class);
    private final SimpleIrdListener simpleIrdListener = new SimpleIrdListener(new Uuid(DEFAULT_UUID));
    private final WriteTransaction wx = mock(WriteTransaction.class);

    private static final String DEFAULT_UUID = "00000000-0000-0000-0000-000000000001";
    private static final String DEFAULT_CONTEXT_UUID = "00000000-0000-0000-0000-000000000001";
    private static String ORIGINAL_INSTANCE_ID="ORIGINAL_INSTANCE_ID";
    private static String UPDATED_INSTANCE_ID="UPDATED_INSTANCE_ID";
    private static String DEFAULT_INSTANCE_ID="DEFAULT_INSTANCE_ID";

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        when(m_dataBroker.registerDataTreeChangeListener(
                any(DataTreeIdentifier.class),
                any(DataTreeChangeListener.class)))
                .thenReturn(m_reg);

        InstanceIdentifier<IrdInstanceConfiguration> iicIID = InstanceIdentifier.builder(IrdInstanceConfiguration.class).build();
        simpleIrdListener.register(m_dataBroker, iicIID);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void register() throws Exception {
        InstanceIdentifier<IrdInstanceConfiguration> iicIID = InstanceIdentifier.builder(IrdInstanceConfiguration.class).build();
        simpleIrdListener.register(m_dataBroker, iicIID);
        verify(m_dataBroker, times(2)).registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, iicIID), this.simpleIrdListener);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void onDataChanged() throws Exception {
        DataTreeModification<IrdInstanceConfiguration> mockDataTreeModification = mock(DataTreeModification.class);
        DataObjectModification<IrdInstanceConfiguration> mockModification = mock(DataObjectModification.class);
        doReturn(mockModification).when(mockDataTreeModification).getRootNode();

        IrdInstanceConfiguration iicb = new IrdInstanceConfigurationBuilder()
                .setKey(new IrdInstanceConfigurationKey(new ResourceId("ORIGINAL_CONFIG"))).build();
        doReturn(iicb).when(mockModification).getDataAfter();
        doReturn(DataObjectModification.ModificationType.WRITE).when(mockModification).getModificationType();

        when(m_dataBroker.newWriteOnlyTransaction()).thenReturn(wx);
        simpleIrdListener.onDataTreeChanged(Collections.singletonList(mockDataTreeModification));
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
}
