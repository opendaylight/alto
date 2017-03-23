/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.basic.simpleird;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstance;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfiguration;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfigurationKey;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceKey;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.IrdEntry;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.IrdEntryKey;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.configuration.IrdConfigurationEntry;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.configuration.IrdConfigurationEntryKey;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;

public class SimpleIrdUtilsTest {
    ResourceId instanceId = new ResourceId("test-instance-id");
    ResourceId resourceId = new ResourceId("test-resource-id");

    @Test
    public void getInstanceConfigurationListIID() {
        InstanceIdentifier<IrdInstanceConfiguration> icIID = InstanceIdentifier.builder(IrdInstanceConfiguration.class).build();
        assertEquals(icIID, SimpleIrdUtils.getInstanceConfigurationListIID());
    }

    @Test
    public void getInstanceListIID() {
        InstanceIdentifier<IrdInstance> iiIID = InstanceIdentifier.builder(IrdInstance.class).build();
        assertEquals(iiIID, SimpleIrdUtils.getInstanceListIID());
    }

    @Test
    public void getInstanceIID() {
        InstanceIdentifier<IrdInstance> isIID = InstanceIdentifier.builder(IrdInstance.class,
                new IrdInstanceKey(new ResourceId("test-resource-id"))).build();
        assertEquals(isIID, SimpleIrdUtils.getInstanceIID("test-resource-id"));
    }

    @Test
    public void getEntryListIID() {
        InstanceIdentifier<IrdInstance> iiIID = InstanceIdentifier.builder(IrdInstance.class, new IrdInstanceKey(instanceId)).build();
        InstanceIdentifier<IrdEntry> ieIID = iiIID.child(IrdEntry.class);
        assertEquals(ieIID, SimpleIrdUtils.getEntryListIID(instanceId));
    }

    @Test
    public void getEntryIID() {
        IrdEntryKey ieKey = new IrdEntryKey(resourceId);
        IrdInstanceKey  iiKey = new IrdInstanceKey(instanceId);
        InstanceIdentifier<IrdEntry> ieIID = InstanceIdentifier
                .builder(IrdInstance.class, iiKey).build().child(IrdEntry.class, ieKey);
        assertEquals(ieIID, SimpleIrdUtils.getEntryIID(instanceId, resourceId));
    }

    @Test
    public void getConfigEntryListIID() {
        IrdInstanceConfigurationKey key = new IrdInstanceConfigurationKey(resourceId);
        InstanceIdentifier<IrdConfigurationEntry> ice = (InstanceIdentifier.builder(IrdInstanceConfiguration.class, key).build()).child(IrdConfigurationEntry.class);
        assertEquals(ice, SimpleIrdUtils.getConfigEntryListIID(resourceId));
    }
    @Test
    public void getConfigEntryIID(){
        IrdInstanceConfigurationKey configKey = new IrdInstanceConfigurationKey(instanceId);
        InstanceIdentifier<IrdInstanceConfiguration> iic = InstanceIdentifier.builder(IrdInstanceConfiguration.class, configKey).build();
        IrdConfigurationEntryKey icek = new IrdConfigurationEntryKey(resourceId);
        InstanceIdentifier<IrdConfigurationEntry> icfe = iic.child(IrdConfigurationEntry.class, icek);
        assertEquals(SimpleIrdUtils.getConfigEntryIID(instanceId, resourceId), icfe);
    }

    @Test
    public void testReadInstance() throws Exception {
        IrdInstance expectedIrdInstance = new IrdInstanceBuilder()
                .setInstanceId(new ResourceId(resourceId)).build();
        ReadTransaction mockRT = mock(ReadTransaction.class);
        InstanceIdentifier<IrdInstance> irdIID = InstanceIdentifier
                .builder(IrdInstance.class, new IrdInstanceKey(resourceId)).build();

        Optional<IrdInstance> returnCacheValue = Optional.of(expectedIrdInstance);
        final CheckedFuture<Optional<IrdInstance>,  org.opendaylight.controller.md.sal.common.api.data.ReadFailedException> irdInstanceFuture;
        irdInstanceFuture = Futures.immediateCheckedFuture(returnCacheValue);

        when(mockRT.read(LogicalDatastoreType.OPERATIONAL, irdIID)).thenReturn(irdInstanceFuture);
        IrdInstance resultIrdInstance = SimpleIrdUtils.readInstance(resourceId, mockRT);
        assertEquals(resultIrdInstance, expectedIrdInstance);

        final CheckedFuture<Optional<IrdInstance>,  org.opendaylight.controller.md.sal.common.api.data.ReadFailedException> nullFuture= Futures.immediateCheckedFuture(Optional.<IrdInstance>absent());
        when(mockRT.read(LogicalDatastoreType.OPERATIONAL, irdIID)).thenReturn(nullFuture);
        resultIrdInstance = SimpleIrdUtils.readInstance(resourceId, mockRT);
        assertEquals(null, resultIrdInstance);
    }
}