/*
 * Copyright © 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.address.tracker.rev140617.address.node.connector.AddressesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AltoAutoMapsUpdateListenerTest {

    private static final String TOPOLOGY_NAME = "flow:1";

    private final DataBroker dataBroker = mock(DataBroker.class);
    private final ListenerRegistration<?> registration = mock(ListenerRegistration.class);
    private AltoAutoMapsUpdateListener altoAutoMapsUpdateListener;
    private final WriteTransaction rwx = mock(WriteTransaction.class);
    private final InstanceIdentifier<Topology> iid = InstanceIdentifier
            .builder(NetworkTopology.class)
            .child(Topology.class,
                    new TopologyKey(new TopologyId(TOPOLOGY_NAME)))
            .build();

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        when(dataBroker.registerDataTreeChangeListener(
                any(DataTreeIdentifier.class),
                any(AltoAutoMapsUpdateListener.class)
        )).thenReturn(registration);

        when(dataBroker.newWriteOnlyTransaction()).thenReturn(rwx);

        altoAutoMapsUpdateListener = new AltoAutoMapsUpdateListener(dataBroker);
    }

    @After
    public void tearDown() throws Exception {

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOnDataChanged() throws Exception {
        DataTreeModification<Topology> mockDataTreeModification = mock(DataTreeModification.class);
        DataObjectModification<Topology> mockModification = mock(DataObjectModification.class);
        doReturn(mockModification).when(mockDataTreeModification).getRootNode();

        // Created
        Topology testTopology = new TopologyBuilder().setNode(Arrays.asList(new NodeBuilder().build())).build();
        doReturn(testTopology).when(mockModification).getDataAfter();
        doReturn(DataObjectModification.ModificationType.WRITE).when(mockModification).getModificationType();
        altoAutoMapsUpdateListener.onDataTreeChanged(Collections.singletonList(mockDataTreeModification));
        verify(rwx).submit();
        reset(rwx);

        // Updated
        doReturn(testTopology).when(mockModification).getDataBefore();

        Topology testTopologyWithHost = new TopologyBuilder()
                .setNode(Arrays.asList(new NodeBuilder()
                        .addAugmentation(HostNode.class,
                                new HostNodeBuilder()
                                        .setAddresses(Arrays.asList(new AddressesBuilder()
                                                .setIp(new IpAddress(new Ipv4Address("192.168.1.100")))
                                                .build(),
                                                new AddressesBuilder().build()))
                                        .build())
                        .build()))
                .build();
        doReturn(testTopologyWithHost).when(mockModification).getDataAfter();
        doReturn(DataObjectModification.ModificationType.WRITE).when(mockModification).getModificationType();
        altoAutoMapsUpdateListener.onDataTreeChanged(Collections.singletonList(mockDataTreeModification));
        verify(rwx).submit();
        reset(rwx);

        // Deleted
        doReturn(testTopologyWithHost).when(mockModification).getDataBefore();
        doReturn(null).when(mockModification).getDataAfter();
        doReturn(DataObjectModification.ModificationType.DELETE).when(mockModification).getModificationType();
        altoAutoMapsUpdateListener.onDataTreeChanged(Collections.singletonList(mockDataTreeModification));
        verify(rwx).submit();
        reset(rwx, dataBroker);

        // No-op
        doReturn(DataObjectModification.ModificationType.SUBTREE_MODIFIED)
            .when(mockModification).getModificationType();
        altoAutoMapsUpdateListener.onDataTreeChanged(Collections.singletonList(mockDataTreeModification));
        verifyNoMoreInteractions(dataBroker);
    }

    @Test
    public void testClose() throws Exception {
        altoAutoMapsUpdateListener.close();
        verify(registration).close();
    }
}
