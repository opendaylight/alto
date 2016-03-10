/*
 * Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl.algorithm;


import edu.uci.ics.jung.graph.Graph;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RouteViewer {
    static public interface RouteChecker {
        boolean isStop(List<Path> pathList);
        List<Path> getResult();
    }

    static public class Path {
        TpId src;
        TpId dst;
        Long bandwidth;

        @Override
        public String toString() {
            return "" + src + "->" + dst + "@" + bandwidth;
        }
    }

    private RouteChecker routeChecker;
    private Map<String, Boolean> hasVisited;
    private Graph<String, Path> networkGraph;
    private LinkedList<Path> route;

    RouteViewer(Graph<String, Path> networkGraph, RouteChecker routeChecker) {
        this.networkGraph = networkGraph;
        this.routeChecker = routeChecker;
    }

    RouteChecker viewRoutes(String srcNode, String dstNode) {
        this.hasVisited = new HashMap<>();
        this.route = new LinkedList<>();
        for (String eachNode : networkGraph.getVertices()) {
            this.hasVisited.put(eachNode, false);
        }
        visitor(srcNode, dstNode);
        return this.routeChecker;
    }

    private boolean visitor(String srcNode, String dstNode) {
        if (hasVisited.get(srcNode)) {
            return false;
        } else if (Objects.equals(srcNode, dstNode)) {
            return this.routeChecker.isStop(this.route);
        }
        else {
            hasVisited.put(srcNode, true);
            for (Path eachPath : this.networkGraph.getOutEdges(srcNode)) {
                route.addLast(eachPath);
                if (visitor(extractNodeId(eachPath.dst), dstNode))
                    return true;
                route.removeLast();
            }
            hasVisited.put(srcNode, false);
        }
        return false;
    }

    public static String extractNodeId(TpId tpId) {
        String nodeConnectorId = tpId.getValue();
        return nodeConnectorId.replaceAll(":[0-9]+$", "");
    }
}
