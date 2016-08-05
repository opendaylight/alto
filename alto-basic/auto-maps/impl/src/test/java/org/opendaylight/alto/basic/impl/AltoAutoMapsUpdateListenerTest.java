/*
 * Copyright © 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
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
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AltoAutoMapsUpdateListenerTest {

    private static final String TOPOLOGY_NAME = "flow:1";

    private DataBroker dataBroker = mock(DataBroker.class);
    private ListenerRegistration<DataChangeListener> registration = mock(ListenerRegistration.class);
    private AltoAutoMapsUpdateListener altoAutoMapsUpdateListener;
    private ReadWriteTransaction rwx = mock(ReadWriteTransaction.class);
    private InstanceIdentifier<Topology> iid = InstanceIdentifier
            .builder(NetworkTopology.class)
            .child(Topology.class,
                    new TopologyKey(new TopologyId(TOPOLOGY_NAME)))
            .build();

    private AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> mockDataChangeEvent =
            mock(AsyncDataChangeEvent.class);
    private Map<InstanceIdentifier<?>, DataObject> original = new HashMap<>();
    private Map<InstanceIdentifier<?>, DataObject> createdData = new HashMap<>();
    private Set<InstanceIdentifier<?>> removedPaths = new HashSet<>();
    private Map<InstanceIdentifier<?>, DataObject> updatedData = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        when(dataBroker.registerDataChangeListener(
                any(LogicalDatastoreType.class),
                any(InstanceIdentifier.class),
                any(DataChangeListener.class),
                any(AsyncDataBroker.DataChangeScope.class)
        )).thenReturn(registration);

        when(dataBroker.newReadWriteTransaction()).thenReturn(rwx);

        altoAutoMapsUpdateListener = new AltoAutoMapsUpdateListener(dataBroker);

        original.put(iid, new TopologyBuilder().build());
        removedPaths.add(iid);

        Topology testTopology = new TopologyBuilder()
                .setNode(Arrays.asList(new NodeBuilder().build()))
                .build();
        createdData.put(iid, testTopology);

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
        updatedData.put(iid, testTopologyWithHost);

        when(mockDataChangeEvent.getOriginalData()).thenReturn(original);
        when(mockDataChangeEvent.getCreatedData()).thenReturn(createdData);
        when(mockDataChangeEvent.getRemovedPaths()).thenReturn(removedPaths);
        when(mockDataChangeEvent.getUpdatedData()).thenReturn(updatedData);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOnNullDataChanged() throws Exception {
        altoAutoMapsUpdateListener.onDataChanged(null);
        verify(rwx, never()).submit();
    }

    @Test
    public void testOnDataChanged() throws Exception {
        altoAutoMapsUpdateListener.onDataChanged(mockDataChangeEvent);
        verify(rwx).submit();
    }

    @Test
    public void testClose() throws Exception {
        altoAutoMapsUpdateListener.close();
        verify(registration).close();
    }
}
