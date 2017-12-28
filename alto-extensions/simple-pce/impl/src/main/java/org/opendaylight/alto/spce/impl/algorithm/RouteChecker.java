/*
 * Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl.algorithm;

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
}
