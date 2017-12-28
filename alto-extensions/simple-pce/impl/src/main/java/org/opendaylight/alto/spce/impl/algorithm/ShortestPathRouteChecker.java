/*
 * Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl.algorithm;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.AltoSpceMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.setup.route.input.ConstraintMetric;

import java.util.LinkedList;
import java.util.List;

public class ShortestPathRouteChecker extends RouteChecker {
    private List<RouteViewerPath> result;
    private long hopcount = Long.MAX_VALUE;
    private List<ConstraintMetric> constraintMetrics = null;
    private RouteViewerPath finalPath = null;

    ShortestPathRouteChecker(RouteViewerPath finalPath, List<ConstraintMetric> constraintMetrics){
        this.finalPath = finalPath;
        this.constraintMetrics = constraintMetrics;
    }

    @Override
    public boolean isStop(List<RouteViewerPath> pathList) {
        LinkedList<RouteViewerPath> tmp = new LinkedList<>(pathList);
        tmp.add(finalPath);
        long hopcount = tmp.size();
        long bandwidth = getBandwidth(pathList);
        List<ConstraintMetric> constraintMetrics = this.constraintMetrics;
        if (constraintMetrics != null) {
            for (ConstraintMetric eachConstraint : constraintMetrics) {
                if (eachConstraint.getMetric() == null) continue;
                long max = (eachConstraint.getMax() != null) ?
                        eachConstraint.getMax().longValue() : Long.MAX_VALUE;
                long min = (eachConstraint.getMin() != null) ?
                        eachConstraint.getMin().longValue() : 0;
                long value = 0;
                if (eachConstraint.getMetric().equals(AltoSpceMetric.Bandwidth)) {
                    value = bandwidth;
                } else {
                    value = hopcount;
                }
                if (value < min || value > max) {
                    return false;
                }
            }
        }
        if (hopcount < this.hopcount) {
            this.hopcount = hopcount;
            result = tmp;
        }
        return false;
    }

    @Override
    public List<RouteViewerPath> getResult() {
        return result;
    }
}
