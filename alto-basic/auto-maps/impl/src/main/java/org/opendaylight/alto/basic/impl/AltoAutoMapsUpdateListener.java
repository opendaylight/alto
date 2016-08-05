/*
 * Copyright © 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.opendaylight.alto.basic.manual.maps.ManualMapsUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.address.tracker.rev140617.address.node.connector.Addresses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.networkmap.rev151021.EndpointAddressType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.networkmap.rev151021.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.networkmap.rev151021.endpoint.address.group.EndpointAddressGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AltoAutoMapsUpdateListener implements DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AltoAutoMapsUpdateListener.class);

    private final DataBroker dataBroker;
    private ListenerRegistration<DataChangeListener> registration;

    private static final String TOPOLOGY_NAME = "flow:1";
    private static final String DEFAULT_AUTO_NETWORKMAP = "default-auto-networkmap";
    private static final String DEFAULT_AUTO_COSTMAP = "default-auto-costmap";
    private static final String DEFAULT_PID = "PID0";

    public AltoAutoMapsUpdateListener(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        InstanceIdentifier<Topology> iid = InstanceIdentifier
                .builder(NetworkTopology.class)
                .child(Topology.class,
                        new TopologyKey(new TopologyId(TOPOLOGY_NAME)))
                .build();
        this.registration = dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                iid, this, AsyncDataBroker.DataChangeScope.BASE);
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> event) {
        if (event == null) {
            return;
        }

        final ReadWriteTransaction rwx = dataBroker.newReadWriteTransaction();

        Map<InstanceIdentifier<?>, DataObject> original = event.getOriginalData();

        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : event.getCreatedData().entrySet()) {
            InstanceIdentifier<?> iid = entry.getKey();
            DataObject created = entry.getValue();

            if (created instanceof Topology) {
                createDefaultAutoNetworkMap((Topology) created, rwx);
                LOG.info("Create default auto networkmap");
            }
        }

        for (InstanceIdentifier<?> iid : event.getRemovedPaths()) {
            DataObject removed = original.get(iid);

            if (removed instanceof Topology) {
                emptyDefaultAutoNetworkMap(rwx);
                LOG.info("Empty default auto networkmap");
            }
        }

        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : event.getUpdatedData().entrySet()) {
            InstanceIdentifier<?> iid = entry.getKey();
            DataObject updated = entry.getValue();

            if (updated instanceof Topology) {
                updateDefaultAutoNetworkMap((Topology) updated, rwx);
                LOG.info("Update default auto networkmap");
            }
        }

        rwx.submit();
    }

    private void createDefaultAutoNetworkMap(Topology topology, final WriteTransaction wx) {
        for (Node node : topology.getNode()) {
            HostNode hostNode = node.getAugmentation(HostNode.class);
            if (hostNode != null) {
                List<Addresses> addressesList = hostNode.getAddresses();
                mergeAddressesListToDefaultNetworkMap(addressesList, wx);
            }
        }
    }

    private void emptyDefaultAutoNetworkMap(final WriteTransaction wx) {
        mergeAddressesListToDefaultNetworkMap(new LinkedList<Addresses>(), wx);
    }

    private void updateDefaultAutoNetworkMap(Topology topology, final WriteTransaction wx) {
        // TODO: can be more efficient
        emptyDefaultAutoNetworkMap(wx);
        createDefaultAutoNetworkMap(topology, wx);
    }

    private void mergeAddressesListToDefaultNetworkMap(List<Addresses> addressesList, final WriteTransaction wx) {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic
                .manual.maps.networkmap.rev151021.network.map.Map> networkMap = new LinkedList<>();
        org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic
                .manual.maps.networkmap.rev151021.network.map.MapBuilder builder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic
                        .manual.maps.networkmap.rev151021.network.map.MapBuilder();

        List<IpPrefix> prefixList = aggregateAddressesList(addressesList);

        List<EndpointAddressGroup> emptyEndpointAddressGroup = new LinkedList<>();
        emptyEndpointAddressGroup.add(new EndpointAddressGroupBuilder()
                .setAddressType(new EndpointAddressType(EndpointAddressType.Enumeration.Ipv4))
                .setEndpointPrefix(prefixList)
                .build());

        builder.setPid(new PidName("PID0"))
                .setEndpointAddressGroup(emptyEndpointAddressGroup);
        networkMap.add(builder.build());

        ManualMapsUtils.createResourceNetworkMap(DEFAULT_AUTO_NETWORKMAP, networkMap, wx);
    }

    private List<IpPrefix> aggregateAddressesList(List<Addresses> addressesList) {
        List<IpPrefix> prefixList = new LinkedList<>();
        for (Addresses addresses : addressesList) {
            if (addresses.getIp() == null)
                continue;
            String ipAddress = addresses.getIp().getIpv4Address().getValue();

            IpPrefix prefix = new IpPrefix(new Ipv4Prefix(ipAddress + "/32"));
            prefixList.add(prefix);
        }

        return prefixList;
    }

    @Override
    public void close() throws Exception {
        registration.close();
        LOG.info("AltoAutoMapsUpdateListener Closed");
    }
}
