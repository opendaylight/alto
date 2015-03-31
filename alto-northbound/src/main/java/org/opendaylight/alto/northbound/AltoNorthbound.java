/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.northbound;

import org.opendaylight.alto.commons.RFC7285MediaType;

import org.codehaus.jettison.json.JSONObject;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class AltoNorthbound {

    private static final Logger mLogger = LoggerFactory.getLogger(AltoNorthbound.class);

    public class AltoError {
        public static final String E_SYNTAX = "syntax-error";
        public static final String E_MISSING_FIELD = "missing-field";
        public static final String E_INVALID_FIELD_TYPE = "invalid-type";
        public static final String E_INVALID_FIELD_VALUE = "invalid-value";
    }

    @GET
    @Produces({RFC7285MediaType.ALTO_DIRECTORY, RFC7285MediaType.ALTO_ERROR})
    public Response retrieveIRD() {
        JSONObject ird = new JSONObject();
        try {
            ird.put("test", "ok");
        } catch (Exception e) {
        }
        return Response.ok(ird.toString(), RFC7285MediaType.ALTO_ERROR).build();
    }

    @Path("/hello")
    @GET
    public Response sayHello() {
        return Response.ok(new String("hello alto")).build();
    }
}
