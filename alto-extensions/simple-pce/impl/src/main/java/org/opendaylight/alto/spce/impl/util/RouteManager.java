/*
 * Copyright © 2015 Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl.util;

import com.google.common.base.Optional;
import org.opendaylight.alto.spce.impl.algorithm.PathComputation;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.AltoSpceMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.ErrorCodeType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.FlowType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.endpoints.group.Endpoints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.setup.route.input.ConstraintMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetMacByIpInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetMacByIpInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetMacByIpOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.NetworkTrackerService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RouteManager {
    private static final Logger LOG = LoggerFactory.getLogger(RouteManager.class);

    private FlowManager flowManager;
    private MeterManager meterManager;
    private PathComputation pathComputation;
    private DataBroker dataBroker;
    private InventoryReader inventoryReader;
    private NetworkTrackerService networkTrackerService;
    private Long NO_METER_SPECIFIED = -1L;

    private HashMap<RouteInfoKey, RouteInfoValue> routeInfo = new HashMap<>();

    public RouteManager(PathComputation pathComputation, InventoryReader inventoryReader
            , NetworkTrackerService networkTrackerService, FlowManager flowManager, MeterManager meterManager, DataBroker dataBroker) {
        this.pathComputation = pathComputation;
        this.inventoryReader = inventoryReader;
        this.networkTrackerService = networkTrackerService;
        this.flowManager = flowManager;
        this.meterManager = meterManager;
        this.dataBroker = dataBroker;
    }

    public List<TpId> getRoute(Endpoints endpoints) {
        RouteInfoKey routeInfoKey = new RouteInfoKey(endpoints.getSrc().getValue(), endpoints.getDst().getValue());
        RouteInfoValue routeInfoValue = this.routeInfo.get(routeInfoKey);
        if (null == routeInfoValue) {
            return null;
        } else {
            return routeInfoValue.getRoute();
        }
    }

    public List<TpId> computeRoute(Endpoints endpoint,
                                   List<AltoSpceMetric> altoSpceMetrics,
                                   List<ConstraintMetric> constraintMetrics) {
        List<TpId> route = null;
        TpId srcTpId = getAttachTp(endpoint.getSrc());
        TpId dstTpId = getAttachTp(endpoint.getDst());
        Topology topology = getTopology();

        try {
            if (altoSpceMetrics.get(0) == AltoSpceMetric.Bandwidth) {
                route = pathComputation.maxBandwidthPath(srcTpId, dstTpId, topology, constraintMetrics);
            } else if (altoSpceMetrics.get(0) == AltoSpceMetric.Hopcount) {
                route = pathComputation.shortestPath(srcTpId, dstTpId, topology, constraintMetrics);
            }
        } catch (Exception e) {
            LOG.info("Exception occurs when compute route: " + e.getMessage());
        }
        return route;
    }

    public ErrorCodeType removeRoute(Endpoints endpoints) {
        RouteInfoKey routeInfoKey = new RouteInfoKey(endpoints.getSrc().getValue(), endpoints.getDst().getValue());
        RouteInfoValue routeInfoValue = this.routeInfo.get(routeInfoKey);
        List<TpId> route = routeInfoValue.getRoute();
        if (route == null) {
            LOG.info("Remove Route: route is null.");
            return ErrorCodeType.OK;
        }
        try {
            Ipv4Address srcIp = endpoints.getSrc();
            Ipv4Address dstIp = endpoints.getDst();
            MacAddress srcMac = ipToMac(srcIp);
            MacAddress dstMac = ipToMac(dstIp);
            for (TpId tpid : route) {
                String nc_value = tpid.getValue();
                InstanceIdentifier<NodeConnector> ncid = InstanceIdentifier.builder(Nodes.class)
                        .child(
                                Node.class,
                                new NodeKey(new NodeId(nc_value.substring(0, nc_value.lastIndexOf(':')))))
                        .child(
                                NodeConnector.class,
                                new NodeConnectorKey(new NodeConnectorId(nc_value)))
                        .build();

                NodeConnectorRef ncr = new NodeConnectorRef(ncid);

                if (routeInfoValue.getFlowType() == FlowType.L3) {
                    LOG.info("Remove a flow from the switch" + InstanceIdentifierUtils.generateNodeInstanceIdentifier(ncr).firstKeyOf(Node.class).getId().getValue());
                    this.flowManager.removeFlow(srcIp, dstIp, new NodeConnectorRef(ncid));
                } else if (routeInfoValue.getFlowType() == FlowType.L2) {
                    LOG.info("Remove a flow from the switch" + InstanceIdentifierUtils.generateNodeInstanceIdentifier(ncr).firstKeyOf(Node.class).getId().getValue());
                    this.flowManager.removeFlow(srcMac, dstMac, new NodeConnectorRef(ncid));
                }
                if (! removeMeter(endpoints, routeInfoValue, ncr)) return ErrorCodeType.REMOVINGMETERERROR;
            }
        } catch (Exception e) {
            LOG.info("Exception occurs when setup a route: " + e.getMessage());
            return ErrorCodeType.REMOVINGROUTEERROR;
        }

        this.routeInfo.remove(routeInfoKey);
        return ErrorCodeType.OK;
    }

    private boolean removeMeter(Endpoints endpoints, RouteInfoValue routeInfoValue, NodeConnectorRef ncr) {
        if (routeInfoValue.getLimitedRate() > 0 && routeInfoValue.getBurstSize() > 0) {
            try {
                this.meterManager.removeMeterFromSwitch(endpoints, ncr, routeInfoValue.getLimitedRate(), routeInfoValue.getBurstSize());
            } catch (Exception e) {
                LOG.info("Exception occurs when remove the meter from " +
                        InstanceIdentifierUtils.generateNodeInstanceIdentifier(ncr).firstKeyOf(Node.class).getId().getValue());
                return false;
            }
        }
        return true;
    }

    public ErrorCodeType removeRateLimiting(Endpoints endpoints) {
        RouteInfoKey routeInfoKey = new RouteInfoKey(endpoints.getSrc().getValue(), endpoints.getDst().getValue());
        RouteInfoValue routeInfoValue = this.routeInfo.get(routeInfoKey);
        List<TpId> route = routeInfoValue.getRoute();
        if (route == null) {
            LOG.info("Remove rate limiting error: route is null");
        } else {
            if (routeInfoValue.getBurstSize() == -1 && routeInfoValue.getLimitedRate() == -1) {
                return ErrorCodeType.OK;
            }
            try {
                Ipv4Address srcIp = endpoints.getSrc();
                Ipv4Address dstIp = endpoints.getDst();
                MacAddress srcMac = ipToMac(srcIp);
                MacAddress dstMac = ipToMac(dstIp);
                FlowType flowLayer = routeInfoValue.getFlowType();
                if (flowLayer == FlowType.L3) {
                    LOG.info("Remove the rate limiting: srcIp=" + srcIp.getValue() + ", dstIp=" + dstIp.getValue());
                } else {
                    LOG.info("Remove the rate limiting: srcMac=" + srcMac.getValue() + ", dstMac=" + dstMac.getValue());
                }
                for (TpId tpid : route) {
                    String nc_value = tpid.getValue();
                    InstanceIdentifier<NodeConnector> ncid = InstanceIdentifier.builder(Nodes.class)
                            .child(
                                    Node.class,
                                    new NodeKey(new NodeId(nc_value.substring(0, nc_value.lastIndexOf(':')))))
                            .child(
                                    NodeConnector.class,
                                    new NodeConnectorKey(new NodeConnectorId(nc_value)))
                            .build();

                    NodeConnectorRef ncr = new NodeConnectorRef(ncid);
                    try {
                        this.meterManager.removeMeterFromSwitch(endpoints, ncr, routeInfoValue.getLimitedRate(), routeInfoValue.getBurstSize());
                    } catch (Exception e) {
                        LOG.info("Exception occurs when remove the meter from " +
                                InstanceIdentifierUtils.generateNodeInstanceIdentifier(ncr).firstKeyOf(Node.class).getId().getValue());
                        return ErrorCodeType.REMOVINGMETERERROR;
                    }

                    if (! writeFlow(srcIp, dstIp, srcMac, dstMac, flowLayer, ncid, ncr, NO_METER_SPECIFIED))
                        return ErrorCodeType.SETTINGUPROUTEERROR;
                }
            } catch (Exception e) {
                LOG.info("Exception occurs when remove a rate limiting: " + e.getMessage());
                return ErrorCodeType.SETTINGUPROUTEERROR;
            }
            RouteInfoValue routeInfoValueNew = new RouteInfoValue(routeInfoValue.getFlowType(), -1L, -1L, route);
            this.routeInfo.put(routeInfoKey, routeInfoValueNew);
            return ErrorCodeType.OK;
        }
        return ErrorCodeType.UNDEFINEDERROR;
    }

    public ErrorCodeType updateRateLimiting(Endpoints endpoints, long limitedRate, long burstSize) {
        RouteInfoKey routeInfoKey = new RouteInfoKey(endpoints.getSrc().getValue(), endpoints.getDst().getValue());
        RouteInfoValue routeInfoValue = this.routeInfo.get(routeInfoKey);
        if (routeInfoValue == null) {
            LOG.info("Update rate limiting error: route is null");
            return ErrorCodeType.MISSINGROUTEERROR;
        }
        List<TpId> route = routeInfoValue.getRoute();
        if (route == null) {
            LOG.info("Update rate limiting error: route is null");
            return ErrorCodeType.MISSINGROUTEERROR;
        } else {
            try {
                Ipv4Address srcIp = endpoints.getSrc();
                Ipv4Address dstIp = endpoints.getDst();
                MacAddress srcMac = ipToMac(srcIp);
                MacAddress dstMac = ipToMac(dstIp);
                FlowType flowLayer = routeInfoValue.getFlowType();
                if (flowLayer == FlowType.L3) {
                    LOG.info("Update the rate limiting: srcIp=" + srcIp.getValue() + ", dstIp=" + dstIp.getValue() + ", limitedRate=" + limitedRate + ", burstSize=" + burstSize);
                } else {
                    LOG.info("Update the rate limiting: srcMac=" + srcMac.getValue() + ", dstMac=" + dstMac.getValue() + ", limitedRate=" + limitedRate + ", burstSize=" + burstSize);
                }
                for (TpId tpid : route) {
                    String nc_value = tpid.getValue();
                    InstanceIdentifier<NodeConnector> ncid = InstanceIdentifier.builder(Nodes.class)
                            .child(
                                    Node.class,
                                    new NodeKey(new NodeId(nc_value.substring(0, nc_value.lastIndexOf(':')))))
                            .child(
                                    NodeConnector.class,
                                    new NodeConnectorKey(new NodeConnectorId(nc_value)))
                            .build();
                    NodeConnectorRef ncr = new NodeConnectorRef(ncid);
                    if (! removeMeter(endpoints, routeInfoValue, ncr)) return ErrorCodeType.REMOVINGMETERERROR;

                    long meterId = NO_METER_SPECIFIED;
                    if (limitedRate > 0 && burstSize > 0) {
                        try {
                            meterId = this.meterManager.addDropMeter(srcIp.getValue(), dstIp.getValue(), limitedRate, burstSize, ncr);
                        } catch (Exception e) {
                            LOG.info("Exception occurs when add the meter to " +
                                    InstanceIdentifierUtils.generateNodeInstanceIdentifier(ncr).firstKeyOf(Node.class).getId().getValue());
                            return ErrorCodeType.SETTINGUPMETERERROR;
                        }
                    }

                    if (! writeFlow(srcIp, dstIp, srcMac, dstMac, flowLayer, ncid, ncr, meterId))
                        return ErrorCodeType.SETTINGUPROUTEERROR;
                }
            } catch (Exception e) {
                LOG.info("Exception occurs when update a rate limiting: " + e.getMessage());
                return ErrorCodeType.UPDATINGRATELIMITINGERROR;
            }
            RouteInfoValue routeInfoValueNew = new RouteInfoValue(routeInfoValue.getFlowType(), limitedRate, burstSize, route);
            this.routeInfo.put(routeInfoKey, routeInfoValueNew);
            return ErrorCodeType.OK;
        }
    }

    private boolean writeFlow(Ipv4Address srcIp, Ipv4Address dstIp, MacAddress srcMac, MacAddress dstMac, FlowType flowLayer, InstanceIdentifier<NodeConnector> ncid, NodeConnectorRef ncr, long meterId) {
        try {
            if (flowLayer == FlowType.L3) {
                this.flowManager.writeFlow(srcIp, dstIp, new NodeConnectorRef(ncid), meterId);
            } else if (flowLayer == FlowType.L2) {
                this.flowManager.writeFlow(srcMac, dstMac, new NodeConnectorRef(ncid), meterId);
            }
        } catch (Exception e) {
            LOG.info("Exception occurs when update the flow in " +
                    InstanceIdentifierUtils.generateNodeInstanceIdentifier(ncr).firstKeyOf(Node.class).getId().getValue());
            return false;
        }
        return true;
    }


    public ErrorCodeType setupRoute(Endpoints endpoints, List<TpId> route, FlowType flowLayer, long limitedRate, long burstSize) {
        Ipv4Address srcIp = endpoints.getSrc();
        Ipv4Address dstIp = endpoints.getDst();
        RouteInfoKey routeInfoKey = new RouteInfoKey(srcIp.getValue(), dstIp.getValue());
        RouteInfoValue routeInfoValue = this.routeInfo.get(routeInfoKey);
        if (routeInfoValue!= null) {
            ErrorCodeType errorCodeType = removeRoute(endpoints);
            if (errorCodeType!= ErrorCodeType.OK) {
                return errorCodeType;
            }
        }

        if (route == null) {
            LOG.info("Set up the route error: route is null.");
            return ErrorCodeType.COMPUTINGROUTEERROR;
        }

        try {
            MacAddress srcMac = ipToMac(srcIp);
            MacAddress dstMac = ipToMac(dstIp);
            if (flowLayer == FlowType.L3) {
                LOG.info("Set up a route: srcIp=" + srcIp.getValue() + ", dstIp=" + dstIp.getValue() + ", limitedRate=" + limitedRate + ", burstSize=" + burstSize);
            } else {
                LOG.info("Set up a route: srcMac=" + srcMac.getValue() + ", dstMac=" + dstMac.getValue() + ", limitedRate=" + limitedRate + ", burstSize=" + burstSize);
            }
            for (TpId tpid : route) {
                String nc_value = tpid.getValue();
                InstanceIdentifier<NodeConnector> ncid = InstanceIdentifier.builder(Nodes.class)
                        .child(
                                Node.class,
                                new NodeKey(new NodeId(nc_value.substring(0, nc_value.lastIndexOf(':')))))
                        .child(
                                NodeConnector.class,
                                new NodeConnectorKey(new NodeConnectorId(nc_value)))
                        .build();
                long meterId = NO_METER_SPECIFIED;
                if (limitedRate > 0 && burstSize > 0) {
                    try {
                        meterId = this.meterManager.addDropMeter(srcIp.getValue(), dstIp.getValue(), limitedRate, burstSize, new NodeConnectorRef(ncid));
                    } catch (Exception e) {
                        LOG.info("Exception occurs when set up a meter: " + e.getMessage());
                        return ErrorCodeType.SETTINGUPMETERERROR;
                    }
                }
                if (flowLayer == FlowType.L3) {
                    this.flowManager.writeFlow(srcIp, dstIp, new NodeConnectorRef(ncid), meterId);
                } else if (flowLayer == FlowType.L2) {
                    this.flowManager.writeFlow(srcMac, dstMac, new NodeConnectorRef(ncid), meterId);
                }

            }
        } catch (Exception e) {
            LOG.info("Exception occurs when set up a route: " + e.getMessage());
            return ErrorCodeType.SETTINGUPROUTEERROR;
        }

        routeInfoValue = new RouteInfoValue(flowLayer, limitedRate, burstSize, route);
        this.routeInfo.put(routeInfoKey, routeInfoValue);
        return ErrorCodeType.OK;
    }

    private TpId getAttachTp(Ipv4Address src) {
        return this.inventoryReader.getNodeConnectorByMac(ipToMac(src));
    }

    private MacAddress ipToMac(Ipv4Address src) {
        MacAddress mac = null;
        AltoSpceGetMacByIpInput input = new AltoSpceGetMacByIpInputBuilder()
                .setIpAddress(src.getValue())
                .build();
        Future<RpcResult<AltoSpceGetMacByIpOutput>> result = this.networkTrackerService.altoSpceGetMacByIp(input);
        try {
            AltoSpceGetMacByIpOutput output = result.get().getResult();
            mac = new MacAddress(output.getMacAddress());
        } catch (InterruptedException | ExecutionException e) {
            LOG.info("Exception occurs when convert ip to mac: " + e.getMessage());
        }
        return mac;
    }

    private Topology getTopology() {
        try {
            ReadOnlyTransaction readTx = this.dataBroker.newReadOnlyTransaction();

            InstanceIdentifier<Topology> topologyInstanceIdentifier = InstanceIdentifier
                    .builder(NetworkTopology.class)
                    .child(Topology.class, new TopologyKey(new TopologyId("flow:1")))
                    .build();

            Optional<Topology> dataFuture = readTx.read(LogicalDatastoreType.OPERATIONAL,
                    topologyInstanceIdentifier).get();

            return dataFuture.get();
        } catch (Exception e) {
            LOG.info("Exception occurs when get topology: " + e.getMessage());
        }
        return null;
    }
}
