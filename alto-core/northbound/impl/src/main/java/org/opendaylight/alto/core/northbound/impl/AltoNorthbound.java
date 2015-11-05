/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.impl;

import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/")
public class AltoNorthbound {

    private AltoNorthboundRouterImpl router = AltoNorthboundRouterImpl.getInstance();

    @Path("{routeName}")
    public AltoNorthboundRoute route(@PathParam("routeName") String routeName) {
        return router.getRoute(routeName);
    }

}
