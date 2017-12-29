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

import java.util.List;

public abstract class RouteChecker {
    abstract boolean isStop(List<RouteViewerPath> pathList);
    abstract List<RouteViewerPath> getResult();

    protected long getBandwidth(List<RouteViewerPath> pathList) {
        Long result = Long.MAX_VALUE;
        for (RouteViewerPath eachPath : pathList) {
            result = (result < eachPath.bandwidth) ? result : eachPath.bandwidth;
        }
        return result;
    }

    protected boolean checkConstraint(List<ConstraintMetric> constraintMetrics, long bandwidth, long hopcount){
        if (constraintMetrics != null) {
            for (ConstraintMetric eachConstraint : constraintMetrics) {
                if (eachConstraint.getMetric() == null) continue;
                long max = (eachConstraint.getMax() != null) ?
                        eachConstraint.getMax().longValue() : Long.MAX_VALUE;
                long min = (eachConstraint.getMin() != null) ?
                        eachConstraint.getMin().longValue() : 0;
                long value = 0;
                if (eachConstraint.getMetric().equals(AltoSpceMetric.Bandwidth))
                    value = bandwidth;
                else
                    value = hopcount;
                if (value < min || value > max) {
                    return false;
                }
            }
        }
        return true;
    }
}
