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

public class AltoNorthboundRouterImpl implements AltoNorthboundRouter {

    private ConcurrentHashMap<String, AltoNorthboundRoute> routes;

    private static AltoNorthboundRouterImpl singleton = new AltoNorthboundRouterImpl();

    protected AltoNorthboundRouterImpl() {
        routes = new ConcurrentHashMap<String, AltoNorthboundRoute>();
    }

    public static AltoNorthboundRouterImpl getInstance() {
        return singleton;
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
