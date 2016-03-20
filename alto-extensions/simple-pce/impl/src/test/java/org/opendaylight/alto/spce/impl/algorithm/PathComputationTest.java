/*
 * Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl.algorithm;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;

import java.util.LinkedList;
import java.util.List;

public class PathComputationTest {
    private PathComputation pathComputer;

    @Before
    public void prepare() {
        pathComputer = new PathComputation(null);
    }

    @Test
    public void onTestMaxBandwidth() {
        Graph<String, RouteViewer.Path> networkGraph = new SparseMultigraph<>();
        for (int i = 0; i < 5; ++i) {
            networkGraph.addVertex("openflow:"+i);
        }
        addEdge(networkGraph, getTp(0, 0), getTp(1, 0), (long) 10);
        addEdge(networkGraph, getTp(1, 1), getTp(2, 0), (long) 100);
        addEdge(networkGraph, getTp(2, 1), getTp(3, 0), (long) 100);
        addEdge(networkGraph, getTp(3, 1), getTp(4, 0), (long) 100);
        addEdge(networkGraph, getTp(4, 1), getTp(5, 0), (long) 10);
        addEdge(networkGraph, getTp(1, 2), getTp(6, 0), (long) 5);
        addEdge(networkGraph, getTp(6, 1), getTp(4, 2), (long) 5);
        List<RouteViewer.Path> output
                = pathComputer.maxBandwidth(networkGraph, getNode(0), getNode(5), (long) 4);
        LinkedList<String> result = new LinkedList<>();
        result.add(getTp(0, 0));
        result.add(getTp(1, 2));
        result.add(getTp(6, 1));
        result.add(getTp(4, 1));
        Assert.assertEquals(4, output.size());
        for (int i = 0; i < 4; ++i) {
            Assert.assertEquals(result.get(i), output.get(i).src.getValue());
        }
    }

    private <T> String getTp(T i, T j) {
        return "openflow:" + i + ":" + j;
    }

    private <T> String getNode(T i) {
        return "openflow:" + i;
    }

    private RouteViewer.Path addEdge (Graph<String, RouteViewer.Path> networkGraph,
                          String src, String dst, Long bw) {
        RouteViewer.Path p = new RouteViewer.Path();
        p.src = TpId.getDefaultInstance(src);
        p.dst = TpId.getDefaultInstance(dst);
        p.bandwidth = bw;
        networkGraph.addEdge(p, PathComputation.extractNodeId(src), PathComputation.extractNodeId(dst),
                EdgeType.DIRECTED);
        p = new RouteViewer.Path();
        p.src = TpId.getDefaultInstance(dst);
        p.dst = TpId.getDefaultInstance(src);
        p.bandwidth = bw;
        networkGraph.addEdge(p, PathComputation.extractNodeId(dst), PathComputation.extractNodeId(src),
                EdgeType.DIRECTED);
        return p;
    }

}
