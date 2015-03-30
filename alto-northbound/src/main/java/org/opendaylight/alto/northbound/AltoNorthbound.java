/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.northbound;

import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class AltoNorthbound {

    private static final Logger mLogger = LoggerFactory.getLogger(AltoNorthbound.class);

    /* TODO
     *
     * Move them to AltoServiceAPI
     *
     * */

    /** The media types defined in [RFC7285]
     * */
    public static final class AltoMediaType {
        public static final String ALTO_DIRECTORY
                                = "application/alto-directory+json";
        public static final MediaType ALTO_DIRECTORY_TYPE
                                = MediaType.valueOf(ALTO_DIRECTORY);
        public static final String ALTO_NETWORKMAP
                                = "application/alto-networkmap+json";
        public static final MediaType ALTO_NETWORKMAP_TYPE
                                = MediaType.valueOf(ALTO_NETWORKMAP);
        public static final String ALTO_NETWORKMAP_FILTER
                                = "application/alto-networkmapfilter+json";
        public static final MediaType ALTO_NETWORKMAP_FILTER_TYPE
                                = MediaType.valueOf(ALTO_NETWORKMAP_FILTER);
        public static final String ALTO_COSTMAP
                                = "application/alto-costmap+json";
        public static final MediaType ALTO_COSTMAP_TYPE
                                = MediaType.valueOf(ALTO_COSTMAP);
        public static final String ALTO_COSTMAP_FILTER
                                = "application/alto-costmapfilter+json";
        public static final MediaType ALTO_COSTMAP_FILTER_TYPE
                                = MediaType.valueOf(ALTO_COSTMAP_FILTER);
        public static final String ALTO_ENDPOINT_PROP
                                = "application/alto-endpointprop+json";
        public static final MediaType ALTO_ENDPOINT_PROP_TYPE
                                = MediaType.valueOf(ALTO_ENDPOINT_PROP);
        public static final String ALTO_ENDPOINT_PROPPARAMS
                                = "application/alto-endpointpropparams+json";
        public static final MediaType ALTO_ENDPOINT_PROPPARAMS_TYPE
                                = MediaType.valueOf(ALTO_ENDPOINT_PROPPARAMS);
        public static final String ALTO_ENDPOINT_COST
                                = "application/alto-endpointcost+json";
        public static final MediaType ALTO_ENDPOINT_COST_TYPE
                                = MediaType.valueOf(ALTO_ENDPOINT_COST);
        public static final String ALTO_ENDPOINT_COSTPARAMS
                                = "application/alto-endpointcostparams+json";
        public static final MediaType ALTO_ENDPOINT_COSTPARAMS_TYPE
                                = MediaType.valueOf(ALTO_ENDPOINT_COSTPARAMS);
        public static final String ALTO_ERROR
                                = "application/alto-error+json";
        public static final MediaType ALTO_ERROR_TYPE
                                = MediaType.valueOf(ALTO_ERROR);
    }

    public class AltoError {
        public static final String E_SYNTAX = "syntax-error";
        public static final String E_MISSING_FIELD = "missing-field";
        public static final String E_INVALID_FIELD_TYPE = "invalid-type";
        public static final String E_INVALID_FIELD_VALUE = "invalid-value";
    }

    @GET
    @Produces({AltoMediaType.ALTO_DIRECTORY, AltoMediaType.ALTO_ERROR})
    public Response retrieveIRD() {
        JSONObject ird = new JSONObject();
        try {
            ird.put("test", "ok");
        } catch (Exception e) {
        }
        return Response.ok(ird.toString(), AltoMediaType.ALTO_ERROR).build();
    }

    @Path("/hello")
    @GET
    public Response sayHello() {
        return Response.ok(new String("hello alto")).build();
    }
}
