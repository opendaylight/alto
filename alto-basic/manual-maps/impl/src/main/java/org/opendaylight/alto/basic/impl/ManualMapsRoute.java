/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.basic.impl;

import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/")
public class ManualMapsRoute implements AltoNorthboundRoute {

    public static final String ALTO_CONFIG = "application/alto-config+json";
    public static final String CONFIG_SUCCESS = "Config Resource Successfully!";

    private AltoManualMapsProvider altoManualMapsProvider = null;

    public ManualMapsRoute(AltoManualMapsProvider provider) {
        altoManualMapsProvider = provider;
    }

    @Path("{path:.+}")
    @POST
    @Produces({ ALTO_CONFIG, ALTO_ERROR })
    public Response modifyResourceRoute (@Context HttpServletRequest request, @PathParam("path") String path, String map) {
        return Response.ok(CONFIG_SUCCESS + "(POST: " + path + ")", ALTO_CONFIG).build();
    }

    @Path("{path:.+}")
    @PUT
    @Produces({ ALTO_CONFIG, ALTO_ERROR })
    public Response newResourceRoute (@Context HttpServletRequest request, @PathParam("path") String path, String map) {
        return Response.ok(CONFIG_SUCCESS + "(PUT: " + path + ")", ALTO_CONFIG).build();
    }

    @Path("{path:.+}")
    @DELETE
    @Produces({ ALTO_CONFIG, ALTO_ERROR })
    public Response removeResourceRoute (@Context HttpServletRequest request, @PathParam("path") String path, String map) {
        return Response.ok(CONFIG_SUCCESS + "(DELETE: " + path + ")", ALTO_CONFIG).build();
    }
}
