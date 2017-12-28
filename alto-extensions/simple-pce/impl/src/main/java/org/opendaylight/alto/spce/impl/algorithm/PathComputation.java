/*
 * Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl.algorithm;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
/*import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.AltoSpceMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.alto.spce.setup.input.ConstraintMetric;*/
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.AltoSpceMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.setup.route.input.ConstraintMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetTxBandwidthInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetTxBandwidthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetTxBandwidthOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.NetworkTrackerService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PathComputation {

    private NetworkTrackerService networkTrackerService;
    private static final Logger logger = LoggerFactory.getLogger(PathComputation.class);
    public PathComputation(NetworkTrackerService networkTrackerService) {
        this.networkTrackerService = networkTrackerService;
    }

    public List<TpId> shortestPath(TpId srcTpId, TpId dstTpId, Topology topology,
                                   final List<ConstraintMetric> constraintMetrics) {
        final RouteViewerPath finalPath = new RouteViewerPath();
        finalPath.src = dstTpId;
        finalPath.bandwidth = getBandwidthByTp(dstTpId.getValue()).longValue();
        RouteChecker checker = new ShortestPathRouteChecker(finalPath, constraintMetrics);
        RouteViewer rv = new RouteViewer(getGraphFromTopology(topology, (long) 0), checker);
        List<RouteViewerPath> result = rv.viewRoutes(
                    RouteViewer.extractNodeId(srcTpId),
                    RouteViewer.extractNodeId(dstTpId))
                .getResult();
        logger.info(result.toString());
        List<TpId> output = new LinkedList<>();
        for (RouteViewerPath eachPath : result) {
            output.add(eachPath.src);
        }
        return output;
    }

    public List<TpId> maxBandwidthPath(TpId srcTpId, TpId dstTpId, Topology topology,
                                       final List<ConstraintMetric> constraintMetrics) {
        final RouteViewerPath finalPath = new RouteViewerPath();
        finalPath.src = dstTpId;
        finalPath.bandwidth = getBandwidthByTp(dstTpId.getValue()).longValue();
        RouteChecker checker = new MaxBandwidthPathRouteChecker(finalPath, constraintMetrics);
        RouteViewer rv = new RouteViewer(getGraphFromTopology(topology, (long) 0), checker);
        List<RouteViewerPath> result = rv.viewRoutes(
                RouteViewer.extractNodeId(srcTpId),
                RouteViewer.extractNodeId(dstTpId))
                .getResult();

        List<TpId> output = new LinkedList<>();
        for (RouteViewerPath eachPath : result) {
            output.add(eachPath.src);
        }
        return output;
    }

    public List<TpId> shortestPathOpti(TpId srcTpId, TpId dstTpId, Topology topology, List<ConstraintMetric> constraintMetrics) {
        String src = srcTpId.getValue();
        String dst = dstTpId.getValue();
        Long minBw = (long) 0;
        for (ConstraintMetric eachConstraint : constraintMetrics) {
            if (AltoSpceMetric.Bandwidth == eachConstraint.getMetric() && eachConstraint.getMin() != null) {
                minBw = (minBw > eachConstraint.getMin().longValue()) ?
                        minBw : eachConstraint.getMin().longValue();
            }
        }
        Graph<String, RouteViewerPath> networkGraph = getGraphFromTopology(topology, minBw);
        DijkstraShortestPath<String, RouteViewerPath> shortestPath = new DijkstraShortestPath<>(networkGraph);
        List<RouteViewerPath> path = shortestPath.getPath(extractNodeId(src), extractNodeId(dst));
        List<TpId> output = new LinkedList<>();
        for (RouteViewerPath eachPath : path) {
            output.add(eachPath.src);
        }
        return output;
    }

    public List<TpId> maxBandwidthPathOpti(TpId srcTpId, TpId dstTpId, Topology topology, List<ConstraintMetric> constraintMetrics) {
        String src = srcTpId.getValue();
        String dst = dstTpId.getValue();
        Graph<String, RouteViewerPath> networkGraph = getGraphFromTopology(topology, null);
        Long maxHop = Long.MAX_VALUE;
        for (ConstraintMetric eachConstraint : constraintMetrics) {
            if (AltoSpceMetric.Hopcount == eachConstraint.getMetric() && eachConstraint.getMax() != null) {
                maxHop = (maxHop < eachConstraint.getMax().longValue()) ?
                        maxHop : eachConstraint.getMax().longValue();
            }
        }
        List<RouteViewerPath> path = maxBandwidth(networkGraph, extractNodeId(src), extractNodeId(dst), maxHop);
        List<TpId> output = new LinkedList<>();
        for (RouteViewerPath eachPath : path) {
            output.add(eachPath.src);
        }
        return output;
    }

    /** (1) add edges by the bandwidth, high bandwidth edge be added into the graph first;
     ** (2) if this adding create a route, which is not longer than max hopcount, between src and dst:
     **       return this route;
     ** (3) else: continue the adding.
     **/
    public List<RouteViewerPath> maxBandwidth(Graph<String, RouteViewerPath> networkGraph, String src, String dst, Long maxHop) {
        Map<String, Long> hopCount = new HashMap<>();
        Map<String, RouteViewerPath> pre = new HashMap<>();
        hopCount.put(src, (long) 0);
        List<RouteViewerPath> paths = new ArrayList<>(networkGraph.getEdges());
        Collections.sort(paths, new Comparator<RouteViewerPath>() {
            @Override
            public int compare(RouteViewerPath x, RouteViewerPath y) {
                return (Objects.equals(x.bandwidth, y.bandwidth) ? 0 : (x.bandwidth > y.bandwidth ? -1 : 1));
            }
        });
        Graph<String, RouteViewerPath> graph = new SparseMultigraph<>();
        // add every node into the graph
        for (String eachNode : networkGraph.getVertices())
            graph.addVertex(eachNode);
        for (RouteViewerPath eachPath : paths) {
            String srcNode = extractNodeId(eachPath.src.getValue());
            String dstNode = extractNodeId(eachPath.dst.getValue());
            graph.addEdge(eachPath, srcNode, dstNode, EdgeType.DIRECTED);
            //update hopcount
            if (hopCount.containsKey(srcNode)) {
                LinkedList<String> queue = new LinkedList<>();
                queue.add(srcNode);
                while (!queue.isEmpty()) {
                    srcNode = queue.pop();
                    if (graph.getOutEdges(srcNode) != null) {
                        for (RouteViewerPath outPath : graph.getOutEdges(srcNode)) {
                            dstNode = extractNodeId(outPath.dst.getValue());
                            if (!hopCount.containsKey(dstNode) ||
                                    (hopCount.get(dstNode) > hopCount.get(srcNode) + 1)) {
                                queue.push(dstNode);
                                hopCount.put(dstNode, hopCount.get(srcNode) + 1);
                                pre.put(dstNode, outPath);
                            }
                        }
                    }
                }
                if (hopCount.containsKey(dst) && hopCount.get(dst) <= maxHop) {
                    // finally, build the route
                    List<RouteViewerPath> output = new LinkedList<>();
                    output.add(0, pre.get(dst));
                    while (!extractNodeId(output.get(0).src.getValue()).equals(src)) {
                        dst = extractNodeId(output.get(0).src.getValue());
                        output.add(0, pre.get(dst));
                    }
                    return output;
                }
            }
        }
        return null;
    }

    private Graph<String, RouteViewerPath> getGraphFromTopology(Topology topology, Long minBw) {
        Graph<String, RouteViewerPath> networkGraph = new SparseMultigraph();
        if (minBw == null) {
            minBw = (long) 0;
        }
        for (Node eachNode : topology.getNode()) {
            networkGraph.addVertex(eachNode.getNodeId().getValue());
        }
        for (Link eachLink : topology.getLink()) {
            String linkSrcNode = extractNodeId(eachLink.getSource().getSourceNode().getValue());
            String linkDstNode = extractNodeId(eachLink.getDestination().getDestNode().getValue());
            if (linkSrcNode.contains("host") || linkDstNode.contains("host")) {
                continue;
            }
            TpId linkSrcTp = eachLink.getSource().getSourceTp();
            TpId linkDstTp = eachLink.getDestination().getDestTp();
            RouteViewerPath srcPath = new RouteViewerPath();
            srcPath.src = linkSrcTp;
            srcPath.dst = linkDstTp;
            srcPath.bandwidth = getBandwidthByTp(srcPath.src.getValue()).longValue();
            if (srcPath.bandwidth < minBw) {
                continue;
            }
            networkGraph.addEdge(srcPath, linkSrcNode, linkDstNode, EdgeType.DIRECTED);
        }
        return networkGraph;
    }

    private BigInteger getBandwidthByTp(String txTpId) {
        BigInteger availableBandwidth = null;
        AltoSpceGetTxBandwidthInput input = new AltoSpceGetTxBandwidthInputBuilder().setTpId(txTpId).build();
        Future<RpcResult<AltoSpceGetTxBandwidthOutput>> result = this.networkTrackerService.altoSpceGetTxBandwidth(input);
        try {
            AltoSpceGetTxBandwidthOutput output = result.get().getResult();
            availableBandwidth = output.getSpeed();
        } catch (InterruptedException | ExecutionException e) {
            return BigInteger.valueOf(0);
        }
        return availableBandwidth;
    }

    public static String extractNodeId(String nodeConnectorId) {
        String output =
            nodeConnectorId.split(":")[0] + ":" + nodeConnectorId.split(":")[1];
        return output;
    }

}
