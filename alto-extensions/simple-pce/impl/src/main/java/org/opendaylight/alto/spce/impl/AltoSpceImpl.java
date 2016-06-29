/*
 * Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl;

import com.google.common.base.Optional;
import org.opendaylight.alto.spce.impl.algorithm.PathComputation;
import org.opendaylight.alto.spce.impl.util.FlowManager;
import org.opendaylight.alto.spce.impl.util.InventoryReader;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.AltoSpceMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.AltoSpceRemoveInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.AltoSpceRemoveOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.AltoSpceRemoveOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.AltoSpceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.AltoSpceSetupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.AltoSpceSetupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.AltoSpceSetupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.ErrorCodeType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.alto.spce.setup.input.ConstraintMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.alto.spce.setup.input.ConstraintMetricBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.alto.spce.setup.input.Endpoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
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
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AltoSpceImpl implements AltoSpceService {

    private static final Logger LOG = LoggerFactory.getLogger(FlowManager.class);
    private SalFlowService salFlowService;
    private NetworkTrackerService networkTrackerService;
    private DataBroker dataBroker;
    private FlowManager flowManager;
    private InventoryReader inventoryReader;
    private PathComputation pathComputation;

    public AltoSpceImpl(SalFlowService salFlowService,
                        NetworkTrackerService networkTrackerService,
                        DataBroker dataBroker) {
        this.salFlowService = salFlowService;
        this.networkTrackerService = networkTrackerService;
        this.dataBroker = dataBroker;
        this.flowManager = new FlowManager(salFlowService);
        this.inventoryReader = new InventoryReader(dataBroker);
        this.pathComputation = new PathComputation(networkTrackerService);
    }

    public Future<RpcResult<AltoSpceRemoveOutput>> altoSpceRemove(AltoSpceRemoveInput input) {
        String path = input.getPath();
        ErrorCodeType errorCode = removePath(path);

        AltoSpceRemoveOutput output = new AltoSpceRemoveOutputBuilder()
                .setErrorCode(errorCode)
                .build();
        return RpcResultBuilder.success(output).buildFuture();
    }

    public Future<RpcResult<AltoSpceSetupOutput>> altoSpceSetup(AltoSpceSetupInput input) {
        Endpoint endpoint = input.getEndpoint();
        List<AltoSpceMetric> altoSpceMetrics = input.getObjectiveMetrics();
        List<ConstraintMetric> constraintMetrics = compressConstraint(input.getConstraintMetric());
        List<TpId> path = null;
        ErrorCodeType errorCode = ErrorCodeType.ERROR;

        if (constraintMetrics != null) {
            path = computePath(endpoint, altoSpceMetrics, constraintMetrics);
            errorCode = setupPath(endpoint, path);
        }

        AltoSpceSetupOutput output = new AltoSpceSetupOutputBuilder()
                .setPath(pathToString(endpoint, path))
                .setErrorCode(errorCode)
                .build();
        return RpcResultBuilder.success(output).buildFuture();
    }

    private List<ConstraintMetric> compressConstraint(List<ConstraintMetric> constraintMetrics) {
        if (constraintMetrics == null)
            return null;
        List<ConstraintMetric> compressedConstraintMetrics = new LinkedList<>();
        BigInteger minHopcount = BigInteger.ZERO;
        BigInteger maxHopcount = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger minBandwidth = BigInteger.ZERO;
        BigInteger maxBandwidth = BigInteger.valueOf(Long.MAX_VALUE);
        for (ConstraintMetric constraintMetric : constraintMetrics) {
            if (constraintMetric.getMetric() == AltoSpceMetric.Hopcount) {
                minHopcount = minHopcount.max(constraintMetric.getMin());
                maxHopcount = maxHopcount.min(constraintMetric.getMax());
                if (minHopcount.compareTo(maxHopcount) == 1) {
                    return null;
                }
            } else if (constraintMetric.getMetric() == AltoSpceMetric.Bandwidth) {
                minBandwidth = minBandwidth.max(constraintMetric.getMin());
                maxBandwidth = maxBandwidth.min(constraintMetric.getMax());
                if (minBandwidth.compareTo(maxBandwidth) == 1) {
                    return null;
                }
            }
        }
        compressedConstraintMetrics.add(new ConstraintMetricBuilder()
                .setMetric(AltoSpceMetric.Hopcount)
                .setMin(minHopcount)
                .setMax(maxHopcount)
                .build());
        compressedConstraintMetrics.add(new ConstraintMetricBuilder()
                .setMetric(AltoSpceMetric.Bandwidth)
                .setMin(minBandwidth)
                .setMax(maxBandwidth)
                .build());
        return compressedConstraintMetrics;
    }

    private Match parseMacMatch(String path) {
        String[] tpList = path.split("\\|");
        MacAddress srcEth = ipToMac(new Ipv4Address(tpList[0]));
        MacAddress dstEth = ipToMac(new Ipv4Address(tpList[tpList.length - 1]));
        if (srcEth == null | dstEth == null) {
            return null;
        }
        return new MatchBuilder()
                .setEthernetMatch(new EthernetMatchBuilder()
                        .setEthernetSource(new EthernetSourceBuilder()
                                .setAddress(srcEth)
                                .build())
                        .setEthernetDestination(new EthernetDestinationBuilder()
                                .setAddress(dstEth)
                                .build())
                        .build())
                .build();
    }

    private Match parseIpMatch(String path) {
        String[] tpList = path.split("\\|");
        Ipv4Prefix srcIp = new Ipv4Prefix(tpList[0] + "/32");
        Ipv4Prefix dstIp = new Ipv4Prefix(tpList[tpList.length - 1] + "/32");
        if (srcIp == null | dstIp == null) {
            return null;
        }
        return new MatchBuilder()
                .setLayer3Match(new Ipv4MatchBuilder()
                        .setIpv4Source(srcIp)
                        .setIpv4Destination(dstIp)
                        .build())
                .setEthernetMatch(new EthernetMatchBuilder()
                        .setEthernetType(new EthernetTypeBuilder()
                                .setType(new EtherType(0x0800L))
                                .build())
                        .build())
                .build();
    }

    private List<TpId> parseTpIds(String path) {
        List<TpId> tpIds = new LinkedList<>();
        String[] tpList = path.split("\\|");
        for (int i = 1; i < tpList.length -1; i++) {
            tpIds.add(new TpId(tpList[i]));
        }
        return tpIds;
    }

    private ErrorCodeType removePath(String path) {
        List<TpId> tpIds = parseTpIds(path);
        Match macMatch = parseMacMatch(path);
        Match ipMatch = parseIpMatch(path);
        if (macMatch == null | ipMatch == null) {
            return ErrorCodeType.ERROR;
        }
        try {
            for (TpId tpId : tpIds) {
                NodeRef nodeRef =
                        new NodeRef(InstanceIdentifier.builder(Nodes.class)
                                .child(Node.class, new NodeKey(
                                        new NodeId(tpId.getValue().substring(0, tpId.getValue().lastIndexOf(":")))))
                                .build());
                this.salFlowService.removeFlow(new RemoveFlowInputBuilder()
                        .setMatch(macMatch)
                        .setNode(nodeRef)
                        .setTransactionUri(tpId)
                        .build()
                );
                this.salFlowService.removeFlow(new RemoveFlowInputBuilder()
                        .setMatch(ipMatch)
                        .setNode(nodeRef)
                        .build()
                );
            }
        } catch (Exception e) {
            LOG.info("Exception occurs when remove a path: " + e.getMessage());
            return ErrorCodeType.ERROR;
        }
        return ErrorCodeType.OK;
    }

    private List<TpId> computePath(Endpoint endpoint,
                               List<AltoSpceMetric> altoSpceMetrics,
                               List<ConstraintMetric> constraintMetrics) {
        List<TpId> path = null;
        TpId srcTpId = getAttachTp(endpoint.getSrc());
        TpId dstTpId = getAttachTp(endpoint.getDst());
        Topology topology = getTopology();

        try {
            if (altoSpceMetrics.get(0) == AltoSpceMetric.Bandwidth) {
                path = pathComputation.maxBandwidthPath(srcTpId, dstTpId, topology, constraintMetrics);
            } else if (altoSpceMetrics.get(0) == AltoSpceMetric.Hopcount) {
                path = pathComputation.shortestPath(srcTpId, dstTpId, topology, constraintMetrics);
            }
        } catch (Exception e) {
            LOG.info("Exception occurs when compute path: " + e.getMessage());
        }

        return path;
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

    private ErrorCodeType setupPath(Endpoint endpoint, List<TpId> path) {
        if (path == null) {
            LOG.info("Setup Error: path is null.");
            return ErrorCodeType.ERROR;
        }

        try {
            Ipv4Address srcIp = endpoint.getSrc();
            Ipv4Address dstIp = endpoint.getDst();
            MacAddress srcMac = ipToMac(srcIp);
            MacAddress dstMac = ipToMac(dstIp);
            List<NodeConnectorRef> nodeConnectorRefs = new LinkedList<>();
            for (TpId tpid : path) {
                String nc_value = tpid.getValue();
                InstanceIdentifier<NodeConnector> ncid = InstanceIdentifier.builder(Nodes.class)
                        .child(
                                Node.class,
                                new NodeKey(new NodeId(nc_value.substring(0, nc_value.lastIndexOf(':')))))
                        .child(
                                NodeConnector.class,
                                new NodeConnectorKey(new NodeConnectorId(nc_value)))
                        .build();
                nodeConnectorRefs.add(new NodeConnectorRef(ncid));
            }
            LOG.info("Setup a path: srcIp=" + srcIp.getValue() + ", dstIp=" + dstIp.getValue());
            LOG.info("Setup a path: srcMac=" + srcMac.getValue() + ", dstMac=" + dstMac.getValue());
            this.flowManager.addFlowByPath(srcIp, dstIp, nodeConnectorRefs);
            this.flowManager.addFlowByPath(srcMac, dstMac, nodeConnectorRefs);
        } catch (Exception e) {
            LOG.info("Exception occurs when setup a path: " + e.getMessage());
            return ErrorCodeType.ERROR;
        }
        return ErrorCodeType.OK;
    }

    private String pathToString(Endpoint endpoint, List<TpId> path) {
        String pathString = endpoint.getSrc().getValue();
        if (path != null) {
            for (TpId tpId : path) {
                pathString += "|" + tpId.getValue();
            }
        }
        pathString += "|" + endpoint.getDst().getValue();
        return pathString;
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
