/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.northbound;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opendaylight.alto.commons.types.rfc7285.JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.FormatValidator;
import org.opendaylight.alto.commons.types.rfc7285.MediaType;
import org.opendaylight.alto.commons.types.rfc7285.NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.CostType;
import org.opendaylight.alto.commons.types.rfc7285.IRD;
import org.opendaylight.alto.commons.types.rfc7285.VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.CostMap;
import org.opendaylight.alto.commons.types.rfc7285.Endpoint;

import org.opendaylight.alto.services.api.rfc7285.AltoService;

import org.opendaylight.alto.services.ext.fake.FakeAltoService;

import org.opendaylight.alto.northbound.exception.AltoBasicException;
import org.opendaylight.alto.northbound.exception.AltoBadFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class AltoNorthbound {

    private static final Logger logger = LoggerFactory.getLogger(AltoNorthbound.class);

    private AltoService altoService = new FakeAltoService();
    private JSONMapper mapper = new JSONMapper();

    private void checkAltoService() throws Exception {
        if (altoService == null)
            throw new AltoBasicException(Status.SERVICE_UNAVAILABLE, null);
    }

    private void checkResourceId(String rid) throws AltoBadFormatException {
        if (!FormatValidator.validResourceId(rid))
            throw new AltoBadFormatException("resource-id", rid);
    }

    private void checkTag(String tag) throws AltoBadFormatException {
        if (!FormatValidator.validTag(tag))
            throw new AltoBadFormatException("tag", tag);
    }

    private Response fail(Response.Status status, Object data) {
        try {
            String output = (data == null ? "" : mapper.asJSON(data));
            return Response.status(status)
                        .entity(output)
                        .type(MediaType.ALTO_ERROR).build();
        } catch (Exception e) {
            logger.error("Failed to parse object to json: {}", data.toString());
            return Response.status(status)
                        .type(MediaType.ALTO_ERROR).build();
        }
    }

    private Response success(Object data, String mediaType) {
        try {
            String output = mapper.asJSON(data);
            return Response.ok(output, mediaType).build();
        } catch (Exception e) {
            logger.error("Failed to parse object to json: {}", data.toString());
            return fail(Status.INTERNAL_SERVER_ERROR, null);
        }
    }

    @GET
    @Produces({ MediaType.ALTO_DIRECTORY, MediaType.ALTO_ERROR })
    public Response retrieveIRD() throws Exception {
        checkAltoService();

        IRD ird = altoService.getDefaultIRD();
        if (ird == null)
            return fail(Status.NOT_FOUND, null);
        return success(ird, MediaType.ALTO_DIRECTORY);
    }

    @Path("/ird/{id}")
    @GET
    @Produces({ MediaType.ALTO_DIRECTORY, MediaType.ALTO_ERROR })
    public Response retrieveIRD(
            @PathParam("id") String id) throws Exception {
        checkAltoService();
        checkResourceId(id);

        IRD ird = altoService.getIRD(id);
        if (ird == null)
            return fail(Status.NOT_FOUND, id);
        return success(ird, MediaType.ALTO_DIRECTORY);
    }

    @Path("/networkmap")
    @GET
    @Produces({ MediaType.ALTO_NETWORKMAP, MediaType.ALTO_ERROR })
    public Response retrieveDefaultNetworkMap() throws Exception {
        checkAltoService();

        NetworkMap map = altoService.getDefaultNetworkMap();
        if (map == null)
            return fail(Status.NOT_FOUND, null);
        return success(map, MediaType.ALTO_NETWORKMAP);
    }

    @Path("/networkmap/{id}")
    @GET
    @Produces({ MediaType.ALTO_NETWORKMAP, MediaType.ALTO_ERROR })
    public Response retrieveNetworkMap(
            @PathParam("id") String id) throws Exception {
        checkAltoService();
        checkResourceId(id);

        NetworkMap map = altoService.getNetworkMap(id);
        if (map == null)
            return fail(Status.NOT_FOUND, id);
        return success(map, MediaType.ALTO_NETWORKMAP);
    }

    @Path("/networkmap/{id}/{tag}")
    @GET
    @Produces({ MediaType.ALTO_NETWORKMAP, MediaType.ALTO_ERROR })
    public Response retrieveNetworkMap(
            @PathParam("id") String id,
            @PathParam("tag") String tag) throws Exception {
        checkAltoService();
        checkResourceId(id);
        checkTag(tag);

        VersionTag vtag = new VersionTag(id, tag);
        NetworkMap map = altoService.getNetworkMap(vtag);
        if (map == null)
            return fail(Status.NOT_FOUND, vtag);
        return success(map, MediaType.ALTO_NETWORKMAP);
    }

    @Path("/costmap/{id}")
    @GET
    @Produces({ MediaType.ALTO_COSTMAP, MediaType.ALTO_ERROR})
    public Response retrieveCostMap(@PathParam("id") String id) throws Exception {
        checkAltoService();
        checkResourceId(id);

        CostMap map = altoService.getCostMap(id);
        if (map == null)
            return fail(Status.NOT_FOUND, id);
        return success(map, MediaType.ALTO_COSTMAP);
    }

    @Path("/costmap/{id}/{tag}")
    @GET
    @Produces({ MediaType.ALTO_COSTMAP, MediaType.ALTO_ERROR})
    public Response retrieveCostMap(
            @PathParam("id") String id,
            @PathParam("tag") String tag) throws Exception {
        checkAltoService();
        checkResourceId(id);
        checkTag(tag);

        VersionTag vtag = new VersionTag(id, tag);
        CostMap map = altoService.getCostMap(vtag);
        if (map == null)
            return fail(Status.NOT_FOUND, vtag);
        return success(map, MediaType.ALTO_COSTMAP);
    }

    @Path("/costmap/{id}/{mode}/{metric}")
    @GET
    @Produces({ MediaType.ALTO_COSTMAP, MediaType.ALTO_ERROR})
    public Response retrieveCostMap(
            @PathParam("id") String id,
            @PathParam("mode") String mode,
            @PathParam("metric") String metric) throws Exception {
        checkAltoService();
        checkResourceId(id);

        CostType costType = new CostType(mode, metric);
        if (!altoService.supportCostType(id, costType))
            return fail(Status.NOT_FOUND, costType);
        CostMap map = altoService.getCostMap(id, costType);
        if (map == null)
            return fail(Status.NOT_FOUND, id);
        return success(map, MediaType.ALTO_COSTMAP);
    }

    @Path("/costmap/{id}/{tag}/{mode}/{metric}")
    @GET
    @Produces({ MediaType.ALTO_COSTMAP, MediaType.ALTO_ERROR})
    public Response retrieveCostMap(
            @PathParam("id") String id,
            @PathParam("tag") String tag,
            @PathParam("mode") String mode,
            @PathParam("metric") String metric) throws Exception {
        checkAltoService();
        checkResourceId(id);
        checkTag(tag);

        VersionTag vtag = new VersionTag(id, tag);
        CostType costType = new CostType(mode, metric);
        if (!altoService.supportCostType(vtag, costType))
            return fail(Status.NOT_FOUND, costType);
        CostMap map = altoService.getCostMap(vtag, costType);
        if (map == null)
            return fail(Status.NOT_FOUND, vtag);
        return success(map, MediaType.ALTO_COSTMAP);
    }

    @Path("/filtered/networkmap/{id}")
    @POST
    @Consumes({ MediaType.ALTO_NETWORKMAP_FILTER})
    @Produces({ MediaType.ALTO_NETWORKMAP, MediaType.ALTO_ERROR})
    public Response retrieveFilteredNetworkMap(
            @PathParam("id") String id, String filterJSON) throws Exception {
        checkAltoService();
        checkResourceId(id);

        NetworkMap.Filter filter = mapper.asNetworkMapFilter(filterJSON);

        if (!altoService.validateNetworkMapFilter(id, filter))
            return fail(Status.BAD_REQUEST, filter);
        NetworkMap map = altoService.getNetworkMap(id, filter);
        if (map == null)
            return fail(Status.NOT_FOUND, id);
        return success(map, MediaType.ALTO_NETWORKMAP);
    }

    @Path("/filtered/networkmap/{id}/{tag}")
    @POST
    @Consumes({ MediaType.ALTO_NETWORKMAP_FILTER})
    @Produces({ MediaType.ALTO_NETWORKMAP, MediaType.ALTO_ERROR})
    public Response retrieveFilteredNetworkMap(
            @PathParam("id") String id,
            @PathParam("tag") String tag,
            String filterJSON) throws Exception {
        checkAltoService();
        checkResourceId(id);
        checkTag(tag);

        VersionTag vtag = new VersionTag(id, tag);
        NetworkMap.Filter filter = mapper.asNetworkMapFilter(filterJSON);
        if (!altoService.validateNetworkMapFilter(vtag, filter))
            return fail(Status.BAD_REQUEST, filter);

        NetworkMap map = altoService.getNetworkMap(vtag, filter);
        if (map == null)
            return fail(Status.NOT_FOUND, vtag);
        return success(map, MediaType.ALTO_NETWORKMAP);
    }

    @Path("/filtered/costmap/{id}")
    @POST
    @Consumes({ MediaType.ALTO_COSTMAP_FILTER })
    @Produces({ MediaType.ALTO_COSTMAP, MediaType.ALTO_ERROR})
    public Response retrieveFilteredCostMap(
            @PathParam("id") String id, String filterJSON) throws Exception {
        checkAltoService();
        checkResourceId(id);

        CostMap.Filter filter = mapper.asCostMapFilter(filterJSON);
        if (!altoService.validateCostMapFilter(id, filter))
            return fail(Status.BAD_REQUEST, filter);

        CostMap map = altoService.getCostMap(id, filter);
        if (map == null)
            return fail(Status.NOT_FOUND, id);
        return success(map, MediaType.ALTO_COSTMAP);
    }

    @Path("/filtered/costmap/{id}/{tag}")
    @POST
    @Consumes({ MediaType.ALTO_COSTMAP_FILTER })
    @Produces({ MediaType.ALTO_COSTMAP, MediaType.ALTO_ERROR})
    public Response retrieveFilteredCostMap(
            @PathParam("id") String id,
            @PathParam("tag") String tag, String filterJSON) throws Exception {
        checkAltoService();
        checkResourceId(id);
        checkTag(tag);

        VersionTag vtag = new VersionTag(id, tag);
        CostMap.Filter filter = mapper.asCostMapFilter(filterJSON);
        if (!altoService.validateCostMapFilter(vtag, filter))
            return fail(Status.BAD_REQUEST, filter);

        CostMap map = altoService.getCostMap(vtag, filter);
        if (map == null)
            return fail(Status.NOT_FOUND, vtag);
        return success(map, MediaType.ALTO_COSTMAP);
    }

    @Path("/endpointprop/lookup/{id}")
    @POST
    @Consumes({ MediaType.ALTO_ENDPOINT_PROPPARAMS })
    @Produces({ MediaType.ALTO_ENDPOINT_PROP, MediaType.ALTO_ERROR })
    public Response retrieveEndpointPropMap(
            @PathParam("id") String id,
            String params) throws Exception {
        checkAltoService();
        checkResourceId(id);

        Endpoint.PropertyRequest request = mapper.asPropertyRequest(params);
        Endpoint.PropertyResponse response = altoService.getEndpointProperty(id, request);
        if (response == null)
            return fail(Status.NOT_FOUND, request);
        return success(response, MediaType.ALTO_ENDPOINT_PROP);
    }

    @Path("/endpointprop/lookup/{id}/{tag}")
    @POST
    @Consumes({ MediaType.ALTO_ENDPOINT_PROPPARAMS })
    @Produces({ MediaType.ALTO_ENDPOINT_PROP, MediaType.ALTO_ERROR })
    public Response retrieveEndpointPropMap(
            @PathParam("id") String id,
            @PathParam("tag") String tag,
            String params) throws Exception {
        checkAltoService();
        checkResourceId(id);
        checkTag(tag);

        VersionTag vtag = new VersionTag(id, tag);
        Endpoint.PropertyRequest request = mapper.asPropertyRequest(params);
        Endpoint.PropertyResponse response = altoService.getEndpointProperty(vtag, request);
        if (response == null)
            return fail(Status.NOT_FOUND, request);
        return success(response, MediaType.ALTO_ENDPOINT_PROP);
    }

    @Path("/endpointcost/lookup/{id}")
    @POST
    @Consumes({ MediaType.ALTO_ENDPOINT_COSTPARAMS })
    @Produces({ MediaType.ALTO_ENDPOINT_COST, MediaType.ALTO_ERROR })
    public Response retrieveEndpointCostMap(
            @PathParam("id") String id,
            String params) throws Exception {
        checkAltoService();
        checkResourceId(id);

        Endpoint.CostRequest request = mapper.asCostRequest(params);
        Endpoint.CostResponse response = altoService.getEndpointCost(id, request);
        if (response == null)
            return fail(Status.NOT_FOUND, request);
        return success(response, MediaType.ALTO_ENDPOINT_COST);
    }

    @Path("/endpointcost/lookup/{id}/{tag}")
    @POST
    @Consumes({ MediaType.ALTO_ENDPOINT_COSTPARAMS })
    @Produces({ MediaType.ALTO_ENDPOINT_COST, MediaType.ALTO_ERROR })
    public Response retrieveEndpointCostMap(
            @PathParam("id") String id,
            @PathParam("tag") String tag,
            String params) throws Exception {
        checkAltoService();
        checkResourceId(id);
        checkTag(tag);

        VersionTag vtag = new VersionTag(id, tag);
        Endpoint.CostRequest request = mapper.asCostRequest(params);
        Endpoint.CostResponse response = altoService.getEndpointCost(vtag, request);
        if (response == null)
            return fail(Status.NOT_FOUND, request);
        return success(response, MediaType.ALTO_ENDPOINT_COST);
    }
}
