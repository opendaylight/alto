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

import org.opendaylight.alto.commons.helper.ServiceHelper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.FormatValidator;
import org.opendaylight.alto.commons.types.rfc7285.MediaType;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostType;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285IRD;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint;
import org.opendaylight.alto.services.api.rfc7285.AltoService;
import org.opendaylight.alto.services.api.rfc7285.IRDService;
import org.opendaylight.alto.services.api.rfc7285.NetworkMapService;
import org.opendaylight.alto.services.api.rfc7285.CostMapService;
import org.opendaylight.alto.services.api.rfc7285.EndpointCostService;
import org.opendaylight.alto.services.api.rfc7285.EndpointPropertyService;
import org.opendaylight.alto.northbound.exception.AltoBasicException;
import org.opendaylight.alto.northbound.exception.AltoBadFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class AltoNorthbound {

    private static final Logger logger = LoggerFactory.getLogger(AltoNorthbound.class);

    private RFC7285JSONMapper mapper = new RFC7285JSONMapper();

    @SuppressWarnings("unchecked")
    private <E> E getService(Class<E> clazz) {
        E service = (E)ServiceHelper.getGlobalInstance(clazz, this);
        if (service == null) {
            service = (E)ServiceHelper.getGlobalInstance(AltoService.class, this);
        }
        return service;
    }

    private void checkService(Object service) throws Exception {
        if (service == null)
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
        IRDService service = getService(IRDService.class);
        checkService(service);

        RFC7285IRD ird = service.getDefaultIRD();
        if (ird == null)
            return fail(Status.NOT_FOUND, null);
        return success(ird, MediaType.ALTO_DIRECTORY);
    }

    @Path("/ird/{id}")
    @GET
    @Produces({ MediaType.ALTO_DIRECTORY, MediaType.ALTO_ERROR })
    public Response retrieveIRD(
            @PathParam("id") String id) throws Exception {
        IRDService service = getService(IRDService.class);
        checkService(service);
        checkResourceId(id);

        RFC7285IRD ird = service.getIRD(id);
        if (ird == null)
            return fail(Status.NOT_FOUND, id);
        return success(ird, MediaType.ALTO_DIRECTORY);
    }

    @Path("/networkmap")
    @GET
    @Produces({ MediaType.ALTO_NETWORKMAP, MediaType.ALTO_ERROR })
    public Response retrieveDefaultNetworkMap() throws Exception {
        NetworkMapService service = getService(NetworkMapService.class);
        checkService(service);

        RFC7285NetworkMap map = service.getDefaultNetworkMap();
        if (map == null)
            return fail(Status.NOT_FOUND, null);
        return success(map, MediaType.ALTO_NETWORKMAP);
    }

    @Path("/networkmap/{id}")
    @GET
    @Produces({ MediaType.ALTO_NETWORKMAP, MediaType.ALTO_ERROR })
    public Response retrieveNetworkMap(
            @PathParam("id") String id) throws Exception {
        NetworkMapService service = getService(NetworkMapService.class);
        checkService(service);
        checkResourceId(id);

        RFC7285NetworkMap map = service.getNetworkMap(id);
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
        NetworkMapService service = getService(NetworkMapService.class);
        checkService(service);
        checkResourceId(id);
        checkTag(tag);

        RFC7285VersionTag vtag = new RFC7285VersionTag(id, tag);
        RFC7285NetworkMap map = service.getNetworkMap(vtag);
        if (map == null)
            return fail(Status.NOT_FOUND, vtag);
        return success(map, MediaType.ALTO_NETWORKMAP);
    }

    @Path("/costmap/{id}")
    @GET
    @Produces({ MediaType.ALTO_COSTMAP, MediaType.ALTO_ERROR})
    public Response retrieveCostMap(@PathParam("id") String id) throws Exception {
        CostMapService service = getService(CostMapService.class);
        checkService(service);
        checkResourceId(id);

        RFC7285CostMap map = service.getCostMap(id);
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
        CostMapService service = getService(CostMapService.class);
        checkService(service);
        checkResourceId(id);
        checkTag(tag);

        RFC7285VersionTag vtag = new RFC7285VersionTag(id, tag);
        RFC7285CostMap map = service.getCostMap(vtag);
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
        CostMapService service = getService(CostMapService.class);
        checkService(service);
        checkResourceId(id);

        RFC7285CostType costType = new RFC7285CostType(mode, metric);
        if (!service.supportCostType(id, costType))
            return fail(Status.NOT_FOUND, costType);
        RFC7285CostMap map = service.getCostMap(id, costType);
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
        CostMapService service = getService(CostMapService.class);
        checkService(service);
        checkResourceId(id);
        checkTag(tag);

        RFC7285VersionTag vtag = new RFC7285VersionTag(id, tag);
        RFC7285CostType costType = new RFC7285CostType(mode, metric);
        if (!service.supportCostType(vtag, costType))
            return fail(Status.NOT_FOUND, costType);
        RFC7285CostMap map = service.getCostMap(vtag, costType);
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
        NetworkMapService service = getService(NetworkMapService.class);
        checkService(service);
        checkResourceId(id);

        RFC7285NetworkMap.Filter filter = mapper.asNetworkMapFilter(filterJSON);

        if (!service.validateNetworkMapFilter(id, filter))
            return fail(Status.BAD_REQUEST, filter);
        RFC7285NetworkMap map = service.getNetworkMap(id, filter);
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
        NetworkMapService service = getService(NetworkMapService.class);
        checkService(service);
        checkResourceId(id);
        checkTag(tag);

        RFC7285VersionTag vtag = new RFC7285VersionTag(id, tag);
        RFC7285NetworkMap.Filter filter = mapper.asNetworkMapFilter(filterJSON);
        if (!service.validateNetworkMapFilter(vtag, filter))
            return fail(Status.BAD_REQUEST, filter);

        RFC7285NetworkMap map = service.getNetworkMap(vtag, filter);
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
        CostMapService service = getService(CostMapService.class);
        checkService(service);
        checkResourceId(id);

        RFC7285CostMap.Filter filter = mapper.asCostMapFilter(filterJSON);
        if (!service.validateCostMapFilter(id, filter))
            return fail(Status.BAD_REQUEST, filter);

        RFC7285CostMap map = service.getCostMap(id, filter);
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
        CostMapService service = getService(CostMapService.class);
        checkService(service);
        checkResourceId(id);
        checkTag(tag);

        RFC7285VersionTag vtag = new RFC7285VersionTag(id, tag);
        RFC7285CostMap.Filter filter = mapper.asCostMapFilter(filterJSON);
        if (!service.validateCostMapFilter(vtag, filter))
            return fail(Status.BAD_REQUEST, filter);

        RFC7285CostMap map = service.getCostMap(vtag, filter);
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
        EndpointPropertyService service = getService(EndpointPropertyService.class);
        checkService(service);
        checkResourceId(id);

        RFC7285Endpoint.PropertyRequest request = mapper.asPropertyRequest(params);
        RFC7285Endpoint.PropertyResponse response = service.getEndpointProperty(id, request);
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
        EndpointPropertyService service = getService(EndpointPropertyService.class);
        checkService(service);
        checkResourceId(id);
        checkTag(tag);

        RFC7285VersionTag vtag = new RFC7285VersionTag(id, tag);
        RFC7285Endpoint.PropertyRequest request = mapper.asPropertyRequest(params);
        RFC7285Endpoint.PropertyResponse response = service.getEndpointProperty(vtag, request);
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
        EndpointCostService service = getService(EndpointCostService.class);
        checkService(service);
        checkResourceId(id);

        RFC7285Endpoint.CostRequest request = mapper.asCostRequest(params);
        RFC7285Endpoint.CostResponse response = service.getEndpointCost(id, request);
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
        EndpointCostService service = getService(EndpointCostService.class);
        checkService(service);
        checkResourceId(id);
        checkTag(tag);

        RFC7285VersionTag vtag = new RFC7285VersionTag(id, tag);
        RFC7285Endpoint.CostRequest request = mapper.asCostRequest(params);
        RFC7285Endpoint.CostResponse response = service.getEndpointCost(vtag, request);
        if (response == null)
            return fail(Status.NOT_FOUND, request);
        return success(response, MediaType.ALTO_ENDPOINT_COST);
    }
}
