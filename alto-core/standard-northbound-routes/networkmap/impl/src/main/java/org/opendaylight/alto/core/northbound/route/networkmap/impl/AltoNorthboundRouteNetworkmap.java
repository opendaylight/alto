/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.route.networkmap.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;

import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.networkmap.rev151021.resources.Resource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AddressTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.ResourceTypeNetworkmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.networkmap.request.data.NetworkmapFilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryOutput;

import org.opendaylight.yangtools.yang.common.RpcResult;

public class AltoNorthboundRouteNetworkmap implements BindingAwareConsumer, AutoCloseable, AltoNorthboundRoute {
    public static final String NETWORKMAP_ROUTE = "networkmap";

    public static final String ALTO_NETWORKMAP_FILTER = "application/alto-networkmapfilter+json";
    public static final String ALTO_NETWORKMAP = "application/alto-networkmap+json";

    public static final String FIELD_PIDS = "pids";
    public static final String FIELD_ADDR_TYPES = "address-types";

    private ObjectMapper mapper = new ObjectMapper();

    private DataBroker m_dataBroker = null;
    private AltoNorthboundRouter m_router = null;

    @Override
    public void onSessionInitialized(ConsumerContext session) {
        m_dataBroker = session.getSALService(DataBroker.class);
    }

    public void register(AltoNorthboundRouter router) {
        m_router = router;
        m_router.addRoute("networkmap", new AltoNorthboundRouteNetworkmap());
    }

    @Override
    public void close() {
        m_router.removeRoute("networkmap");
    }

    @Path("{path}")
    @GET
    @Produces({ALTO_NETWORKMAP, ALTO_ERROR})
    public Response getFullMap(@PathParam("path") String path) {
        //TODO
        return null;
    }

    @Path("{path}")
    @POST
    @Consumes({ALTO_NETWORKMAP_FILTER})
    @Produces({ALTO_NETWORKMAP, ALTO_ERROR})
    public Response getFilteredMap(@PathParam("path") String path, String filter) throws JsonProcessingException {
        Response error;

        List<String> pids = null;
        List<String> addressTypes = null;

        try {
            JsonNode filterNode = mapper.readTree(filter);

            JsonNode _pids = filterNode.get(FIELD_PIDS);

            error = NetworkmapRouteChecker.checkMissing(_pids, FIELD_PIDS, filter);
            if (error != null)
                return error;

            error = NetworkmapRouteChecker.checkList(_pids, FIELD_PIDS, filter);
            if (error != null)
                return error;

            pids = arrayNode2List(FIELD_PIDS, (ArrayNode)_pids);

            JsonNode _addressTypes = filterNode.get(FIELD_ADDR_TYPES);
            error = NetworkmapRouteChecker.checkMissing(_addressTypes, FIELD_ADDR_TYPES, filter);
            if (error == null) {
                error = NetworkmapRouteChecker.checkList(_addressTypes, FIELD_ADDR_TYPES, filter);

                if (error != null)
                    return error;

                addressTypes = arrayNode2List(FIELD_ADDR_TYPES, (ArrayNode)_addressTypes);
            }
        } catch (JsonProcessingException e) {
            throw e;
        } catch (Exception e) {
            return Response.status(500).build();
        }

        //TODO
        QueryInput input = prepareInput(path, pids, addressTypes);
        QueryOutput output = null;

        return buildOutput(input, output);
    }

    protected List<String> arrayNode2List(String field, ArrayNode node) {
        HashSet<String> retval = new HashSet<String>();

        for (Iterator<JsonNode> itr = node.elements(); itr.hasNext(); ) {
            JsonNode data = itr.next();

            retval.add(data.asText());
        }
        return new LinkedList<String>(retval);
    }

    protected QueryInput prepareDefaultInput(String rid) {
        /*
         * Set pids as empty so all PID should be returned.
         *
         * Set address-types as missing so all address types should be returned.
         *
         * See https://tools.ietf.org/html/rfc7285#section-11.3.1.3
         *
         * */

        return prepareInput(rid, new LinkedList<String>(), null);
    }

    protected QueryInput prepareInput(String path, List<String> pids, List<String> addressTypes) {
        //TODO
        return null;
    }

    protected Response buildOutput(QueryInput input, QueryOutput output) {
        //TODO
        return null;
    }
}
