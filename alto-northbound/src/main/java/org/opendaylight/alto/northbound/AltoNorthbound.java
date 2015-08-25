/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.northbound;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opendaylight.alto.commons.helper.ServiceHelper;
import org.opendaylight.alto.commons.types.rfc7285.FormatValidator;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostType;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285QueryPairs;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285IRD;
import org.opendaylight.alto.commons.types.rfc7285.MediaType;
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

import java.util.Arrays;
import java.util.List;

@Path("/")
public class AltoNorthbound {

    private static final Logger logger = LoggerFactory.getLogger(AltoNorthbound.class);

    private RFC7285JSONMapper mapper = new RFC7285JSONMapper();

    private static final List<String> definedProperties = Arrays.asList(
            "my-alternate-network-map.pid",
            "my-default-network-map.pid",
            "priv:ietf-type");

    private static final List<String> definedCostMetrics = Arrays.asList(
                                                        "routingcost",
                                                        "hopcount");

    private static final List<String> definedCostModes = Arrays.asList(
                                                        "ordinal",
                                                        "numerical");

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

    private void checkPropertyName(List<String> properties) throws AltoBadFormatException {
        for (String name : properties) {
            if (!definedProperties.contains(name))
                throw new AltoBadFormatException("E_INVALID_FIELD_VALUE", "properties", name);
        }
    }

    private void checkEndpointAddress(List<String> endpoints) throws AltoBadFormatException {
        for (String address : endpoints) {
            if (!FormatValidator.validEndpointAddress(address))
                throw new AltoBadFormatException("E_INVALID_FIELD_VALUE", "endpoints", address);
        }
    }

    private void checkPropertyRequest(RFC7285Endpoint.PropertyRequest request) throws AltoBadFormatException {
        checkPropertyName(request.properties);
        checkEndpointAddress(request.endpoints);
    }

    private void checkCostMetric(String metric) throws AltoBadFormatException {
        if (!definedCostMetrics.contains(metric))
            throw new AltoBadFormatException("E_INVALID_FIELD_VALUE", "cost-type/cost-metric", metric);
    }

    private void checkCostMode(String mode) throws AltoBadFormatException {
        if (!definedCostModes.contains(mode))
            throw new AltoBadFormatException("E_INVALID_FIELD_VALUE", "cost-type/cost-mode", mode);
    }

    private void checkCostType(RFC7285CostType costType) throws AltoBadFormatException {
        checkCostMetric(costType.metric);
        checkCostMode(costType.mode);
    }

    private void checkConstraints(List<String> constraints) throws AltoBadFormatException {
        for (String constraint : constraints)
            if (!FormatValidator.validFilterConstraint(constraint))
                throw new AltoBadFormatException("E_INVALID_FIELD_VALUE", "constraints", constraint);
    }

    private void checkCostRequest(HttpServletRequest httpRequest, RFC7285Endpoint.CostRequest request) throws AltoBadFormatException {
        checkCostType(request.costType);
        checkEndponints(httpRequest, request.endpoints);
    }

    private void checkCostMapFilter(RFC7285CostMap.Filter filter) throws AltoBadFormatException {
        checkCostType(filter.costType);
        if (filter.constraints != null)
            checkConstraints(filter.constraints);
    }

    private void checkEndponints(HttpServletRequest httpRequest, RFC7285QueryPairs endpoints) {
        String ipAddress = getClientIpAddress(httpRequest);
        if (endpoints.src.size() == 0) {
            endpoints.src.add(ipAddress);
        }

        if (endpoints.dst.size() == 0) {
            endpoints.dst.add(ipAddress);
        }
    }

    private String getClientIpAddress(HttpServletRequest httpRequest) {
        String remoteAddress = httpRequest.getRemoteAddr();
        if (FormatValidator.validAddressIpv4("ipv4:" + remoteAddress)) {
            return "ipv4:" + remoteAddress;
        }

        if (FormatValidator.validAddressIpv6("ipv6:" + remoteAddress)) {
            return "ipv6:" + remoteAddress;
        }

        throw new AltoBadFormatException("E_INVALID_CLIENT_IP");
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
            logger.error(e.getMessage());
            return fail(Status.INTERNAL_SERVER_ERROR, null);
        }
    }

    @Path("/directory")
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
        checkCostMapFilter(filter);
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
        checkCostMapFilter(filter);
        if (!service.validateCostMapFilter(vtag, filter))
            return fail(Status.BAD_REQUEST, filter);

        RFC7285CostMap map = service.getCostMap(vtag, filter);
        if (map == null)
            return fail(Status.NOT_FOUND, vtag);
        return success(map, MediaType.ALTO_COSTMAP);
    }

    @Path("/endpointprop/lookup")
    @POST
    @Consumes({ MediaType.ALTO_ENDPOINT_PROPPARAMS })
    @Produces({ MediaType.ALTO_ENDPOINT_PROP, MediaType.ALTO_ERROR })
    public Response retrieveEndpointPropMap(
            String params) throws Exception {
        EndpointPropertyService service = getService(EndpointPropertyService.class);
        checkService(service);

        RFC7285Endpoint.PropertyRequest request = mapper.asPropertyRequest(params);
        checkPropertyRequest(request);
        RFC7285Endpoint.PropertyResponse response = service.getEndpointProperty(request);
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

    @Path("/endpointcost/lookup")
    @POST
    @Consumes({ MediaType.ALTO_ENDPOINT_COSTPARAMS })
    @Produces({ MediaType.ALTO_ENDPOINT_COST, MediaType.ALTO_ERROR })
    public Response retrieveEndpointCostMap(@Context HttpServletRequest httpRequest,
            String params) throws Exception {
        EndpointCostService service = getService(EndpointCostService.class);
        checkService(service);

        RFC7285Endpoint.CostRequest request = mapper.asCostRequest(params);
        checkCostRequest(httpRequest, request);
        RFC7285Endpoint.CostResponse response = service.getEndpointCost(request);
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
