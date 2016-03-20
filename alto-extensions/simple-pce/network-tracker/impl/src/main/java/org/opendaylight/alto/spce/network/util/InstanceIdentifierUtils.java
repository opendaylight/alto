/*
 * Copyright (c) 2015 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.spce.network.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InstanceIdentifierUtils {
    public static final String TOPOLOGY_NAME = "flow:1";
    public static final InstanceIdentifier<Topology> TOPOLOGY = InstanceIdentifier
            .builder(NetworkTopology.class)
            .child(Topology.class, new TopologyKey(new TopologyId(TOPOLOGY_NAME)))
            .build();

    public static final InstanceIdentifier<HostNode> HOSTNODE = InstanceIdentifier
            .builder(NetworkTopology.class)
            .child(Topology.class, new TopologyKey(new TopologyId(TOPOLOGY_NAME)))
            .child(Node.class)
            .augmentation(HostNode.class)
            .build();

    public static final InstanceIdentifier<Link> LINK = InstanceIdentifier
            .builder(NetworkTopology.class)
            .child(Topology.class, new TopologyKey(new TopologyId(TOPOLOGY_NAME)))
            .child(Link.class)
            .build();

    public static final InstanceIdentifier<FlowCapableNodeConnectorStatisticsData> STATISTICS = InstanceIdentifier
            .builder(Nodes.class)
            .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class)
            .child(NodeConnector.class).augmentation(FlowCapableNodeConnectorStatisticsData.class).build();

    public static InstanceIdentifier<FlowCapableNode> flowCapableNode(String nodeId) {
        return InstanceIdentifier
                .builder(Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                        new NodeKey(new NodeId(nodeId)))
                .augmentation(FlowCapableNode.class)
                .build();
    }

    /**
     * @param nodeId
     * @param meterId
     * @return iid of {@link Meter}.
     */
    public static InstanceIdentifier<Meter> flowCapableNodeMeter(String nodeId, long meterId) {
        return InstanceIdentifier
                .builder(Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                        new NodeKey(new NodeId(nodeId)))
                .augmentation(FlowCapableNode.class)
                .child(Meter.class, new MeterKey(new MeterId(meterId)))
                .build();
    }

    /**
     * @param linkId
     * @return iid of {@link Link}
     */
    public static InstanceIdentifier<Link> linkPath(String linkId) {
        return InstanceIdentifier
                .builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(TOPOLOGY_NAME)))
                .child(Link.class, new LinkKey(new LinkId(linkId)))
                .build();
    }

    /**
     * @param nodeConnectorId
     * @return iid of {@link FlowCapableNodeConnector}.
     */
    public static InstanceIdentifier<FlowCapableNodeConnector> flowCapableNodeConnector(String nodeConnectorId) {
        String nodeId = extractNodeId(nodeConnectorId);
        return InstanceIdentifier
                .builder(Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                        new NodeKey(new NodeId(nodeId)))
                .child(NodeConnector.class, new NodeConnectorKey(new NodeConnectorId(nodeConnectorId)))
                .augmentation(FlowCapableNodeConnector.class)
                .build();
    }

    public static String extractNodeId(String nodeConnectorId) {
        return nodeConnectorId.replaceAll(":[0-9]+$", "");
    }

}

