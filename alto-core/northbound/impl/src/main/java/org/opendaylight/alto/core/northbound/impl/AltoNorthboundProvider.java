/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.northbound.impl;

import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoNorthboundProvider implements BindingAwareProvider, AutoCloseable, AltoNorthboundRouter{

    private static final Logger LOG = LoggerFactory.getLogger(AltoNorthboundProvider.class);

    private AltoNorthboundRouterImpl router = AltoNorthboundRouterImpl.getInstance();

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoNorthboundProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {
        LOG.info("AltoNorthboundProvider Closed");
    }

    @Override
    public String addRoute(String routeName, AltoNorthboundRoute route) {
        return router.addRoute(routeName, route);
    }

    @Override
    public void removeRoute(String routeName) {
        router.removeRoute(routeName);
    }

    @Override
    public AltoNorthboundRoute getRoute(String routeName) {
        return router.getRoute(routeName);
    }

}
