/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.route.example.impl;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;

@Path("/")
public class AltoNorthboundRouteExample implements AltoNorthboundRoute {

    @Path("{echo}")
    @GET
    @Produces("application/alto-test+json")
    public String route(@Context HttpServletRequest req, @PathParam("echo") String echo) {
        if (req == null) {
            System.out.println("We are screwed");
        }
        return "Hello world! " + echo
                + "\nFrom: " + req.getContextPath() + "/" + req.getPathTranslated()
                + "\nTo: " + req.getRemoteAddr();
    }

}
