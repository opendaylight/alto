/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.impl;

import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoNorthboundRouterImpl implements AltoNorthboundRouter {
    private static final Logger LOG = LoggerFactory.getLogger(AltoNorthboundRouterImpl.class);

    private final ConcurrentHashMap<String, AltoNorthboundRoute> routes = new ConcurrentHashMap<>();

    public AltoNorthboundRouterImpl() {
        LOG.info("AltoNorthboundRouterImpl created");
    }

    @Override
    public String addRoute(String routeName, AltoNorthboundRoute route) {
        if (routes.contains(routeName)) {
            return null;
        }
        routes.put(routeName, route);
        return "/alto/" + routeName;
    }

    @Override
    public AltoNorthboundRoute getRoute(String routeName) {
        return routes.get(routeName);
    }

    @Override
    public void removeRoute(String routeName) {
        if (!routes.contains(routeName)) {
            return;
        }
        routes.remove(routeName);
    }
}
