/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.route.endpointcost.impl;


import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Optional;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285CostType;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285Endpoint;

import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.endpointcost.rev151021.Records;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.endpointcost.rev151021.records.Record;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.endpointcost.rev151021.records.RecordKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTagKey;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.CostMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.AltoModelEndpointcostService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.ResourceTypeEndpointcost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.alto.request.endpointcost.request.EndpointcostRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.alto.request.endpointcost.request.EndpointcostRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.alto.response.endpointcost.response.EndpointcostResponse;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.cost.type.container.CostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.cost.type.container.CostTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.endpointcost.request.data.EndpointcostParams;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.endpointcost.request.data.EndpointcostParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.endpointcost.response.data.EndpointcostData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.Ipv4AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.Ipv6AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpoint.filter.data.EndpointFilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpoint.filter.data.endpoint.filter.Destination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpoint.filter.data.endpoint.filter.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpoint.filter.data.endpoint.filter.Source;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpoint.filter.data.endpoint.filter.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.EndpointCostMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.EndpointCost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.Cost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.EndpointFilterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.EndpointFilterDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.endpoint.filter.data.endpoint.filter.source.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.endpoint.filter.data.endpoint.filter.source.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.EndpointCostmapData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.typed.address.data.Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class AltoNorthboundRouteEndpointcost implements AltoNorthboundRoute {
    public static final String ENDPOINTCOST_ROUTE = "endpointcost";

    public static final String ALTO_ENDPOINTCOST_FILTER = "application/alto-endpointcostfilter+json";
    public static final String ALTO_ENDPOINTCOST = "application/alto-endpointcost+json";

    public static final String FIELD_ENDPOINTS = "endpoints";
    public static final String FIELD_COST_TYPE = "cost-type";
    public static final String FIELD_COST_MODE = "cost-mode";
    public static final String FIELD_COST_METRIC = "cost-metric";
    public static final String FIELD_SOURCE = "srcs";
    public static final String FIELD_DESTINSTION = "dsts";

    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(AltoNorthboundRouteEndpointcost.class);

    private DataBroker dataBroker = null;

    private AltoNorthboundRouter m_router = null;

    private  static AltoModelEndpointcostService mapService = null;
    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setMapService(final AltoModelEndpointcostService mapService) {
        this.mapService = mapService;
    }

    public void init() {

        if (dataBroker == null) {
            LOG.error("Failed to init: data broker is null");
        }


        LOG.info("AltoNorthboundRouteEndpointcost initiated");

    }

    public void register(AltoNorthboundRouter router) {
        m_router = router;
        m_router.addRoute("endpointcost", new AltoNorthboundRouteEndpointcost());
    }

    public void close() {
        m_router.removeRoute("endpointcost");
    }

    @Path("{path}")
    @GET
    @Produces({ALTO_ENDPOINTCOST, ALTO_ERROR})
    public Response getFullMap(@PathParam("path") String path) throws JsonProcessingException{
        QueryInput input = prepareDefaultInput(path);
        Future<RpcResult<QueryOutput>> outputFuture = mapService.query(input);
        QueryOutput output = null;
        try {
            output = outputFuture.get().getResult();
        } catch (Exception e) {
            LOG.warn("get output failed:" , e);
        }
        Response response = buildOutput(input, output);
        if(response != null)
            return response;
        else
            return Response.status(404).build();
    }

    @Path("{path}")
    @POST
    @Consumes({ALTO_ENDPOINTCOST_FILTER})
    @Produces({ALTO_ENDPOINTCOST, ALTO_ERROR})
    public Response getFilteredMap(@PathParam("path") String path, String filter) throws JsonProcessingException {
        Response error;

        String cost_mode = null;
        String cost_metric = null;
        List<String> endpoints_source = null;
        List<String> endpoints_destination = null;


        try {
            JsonNode filterNode = mapper.readTree(filter);

            JsonNode _endpoints = filterNode.get(FIELD_ENDPOINTS);

            error = EndpointcostRouteChecker.checkMissing(_endpoints, FIELD_ENDPOINTS, filter);
            if (error != null)
                return error;

            error = EndpointcostRouteChecker.checkList(_endpoints, FIELD_ENDPOINTS, filter);
            if (error != null)
                return error;

            //need check endpoints_source
            JsonNode _endpoints_source = _endpoints.get(FIELD_SOURCE);
            error = EndpointcostRouteChecker.checkList(_endpoints_source, FIELD_SOURCE, filter);
            if (error != null){
                return error;
            }
            endpoints_source = arrayNode2List(FIELD_SOURCE, (ArrayNode)_endpoints_source);

            //need check endpoints_destination
            JsonNode _endpoints_destination = _endpoints.get(FIELD_DESTINSTION);
            error = EndpointcostRouteChecker.checkList(_endpoints_destination, FIELD_DESTINSTION, filter);
            if (error != null){
                return error;
            }
            endpoints_destination = arrayNode2List(FIELD_DESTINSTION, (ArrayNode) _endpoints_destination);

            JsonNode _cost_type = filterNode.get(FIELD_COST_TYPE);
            if(_cost_type == null){
                error = null;
                return error;
            }else {
                cost_mode = _cost_type.get(FIELD_COST_MODE).asText();
                cost_metric = _cost_type.get(FIELD_COST_METRIC).asText();
            }


        } catch (JsonProcessingException e) {
            throw e;
        } catch (Exception e) {
            return Response.status(500).build();
        }

        //TODO
        QueryInput input = prepareInput(path, cost_mode, cost_metric, endpoints_source, endpoints_destination);
        Future<RpcResult<QueryOutput>> outputFuture = mapService.query(input);
        QueryOutput output = null;
        try {
            output = outputFuture.get().getResult();
        } catch (Exception e) {
            LOG.warn("get output failed:" , e);
        }
        Response response = buildOutput(input, output);
        if(response != null)
            return response;
        else
            return Response.status(404).build();
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
        return prepareInput(rid, null, null, new LinkedList<String>(), new LinkedList<String>());
    }

    protected QueryInput prepareInput(String path, String cost_mode, String cost_metric, List<String> endpoints_source, List<String> endpoints_destination) {
        //TODO
        QueryInputBuilder queryInputBuilder = new QueryInputBuilder();

        //set request
        EndpointcostRequestBuilder endpointcostRequestBuilder = new EndpointcostRequestBuilder();
        EndpointcostParamsBuilder endpointcostParamsBuilder = new EndpointcostParamsBuilder();

        CostTypeBuilder costTypeBuilder = new CostTypeBuilder();
        costTypeBuilder.setCostMetric(new CostMetric(cost_metric));
        costTypeBuilder.setCostMode(cost_mode);

        EndpointFilterBuilder endpointFilterBuilder = new EndpointFilterBuilder();

        List<Source> sources = new ArrayList<Source>();
        List<Destination> destinations = new ArrayList<Destination>();

        for(String source : endpoints_source){
            SourceBuilder sourceBuilder = new SourceBuilder();
            String[] tmp= source.split(":");
            Ipv4Builder ipv4 = new Ipv4Builder();
            Ipv6Builder ipv6 = new Ipv6Builder();
            if(tmp[0].equals("ipv4")){
                ipv4.setIpv4(new Ipv4Address(tmp[1]));
                sourceBuilder.setAddress(ipv4.build());
            }else if(tmp[0].equals("ipv6")){
                ipv6.setIpv6(new Ipv6Address(tmp[1]));
                sourceBuilder.setAddress(ipv6.build());
            }
            sources.add(sourceBuilder.build());
        }
        for(String destination : endpoints_destination){
            DestinationBuilder destinationBuilder = new DestinationBuilder();
            String[] tmp= destination.split(":");
            Ipv4Builder ipv4 = new Ipv4Builder();
            Ipv6Builder ipv6 = new Ipv6Builder();
            if(tmp[0].equals("ipv4")){
                ipv4.setIpv4(new Ipv4Address(tmp[1]));
                destinationBuilder.setAddress(ipv4.build());
            }else if(tmp[0].equals("ipv6")){
                ipv6.setIpv6(new Ipv6Address(tmp[1]));
                destinationBuilder.setAddress(ipv6.build());
            }else return null;
            destinations.add(destinationBuilder.build());
        }
        endpointFilterBuilder.setSource(sources);
        endpointFilterBuilder.setDestination(destinations);
        EndpointFilterDataBuilder endpointFilterDataBuilder = new EndpointFilterDataBuilder();
        endpointFilterDataBuilder.setEndpointFilter(endpointFilterBuilder.build());

        endpointcostParamsBuilder.setCostType(costTypeBuilder.build());
        endpointcostParamsBuilder.setFilter(endpointFilterDataBuilder.build());
        endpointcostRequestBuilder.setEndpointcostParams(endpointcostParamsBuilder.build());

        //create servicereference by getting contexttag from resourcepool
        ReadOnlyTransaction rtx = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<ContextTag> ctagIID = getResourceByPath(path, rtx);
        if(ctagIID == null){
            return null;
        }

        queryInputBuilder.setRequest(endpointcostRequestBuilder.build());
        queryInputBuilder.setServiceReference(ctagIID);
        queryInputBuilder.setType(ResourceTypeEndpointcost.class);
        return queryInputBuilder.build();
    }

    public InstanceIdentifier<ContextTag> getResourceByPath(String path, ReadTransaction transaction){
        //get iid from (list Records)
        InstanceIdentifier<Record> recordIID = InstanceIdentifier.builder(Records.class).child(Record.class, new RecordKey(new Uri(path))).build();

        Future<Optional<Record>> recordFuture = transaction.read(LogicalDatastoreType.CONFIGURATION, recordIID);
        Optional<Record> optional = null;
        try{
             optional = recordFuture.get();
        }catch(Exception e){
            LOG.error("Reading Record failed", e);
            return null;
        }
        //get resourceIID from nbr-costmap.yang
        InstanceIdentifier<?> record2resourceIID = null;
        if(optional.isPresent())
            record2resourceIID = optional.get().getResourceIid();
        InstanceIdentifier<Resource> resourceIID = (InstanceIdentifier<Resource>)record2resourceIID;
        Future<Optional<Resource>> resourceFuture = transaction.read(LogicalDatastoreType.OPERATIONAL, resourceIID);

        Optional<Resource> optional1 = null;
        try{
            optional1 = resourceFuture.get();
        }
        catch(Exception e){
            LOG.error("Read resource failed:", e);
            return null;
        }
        Resource resource = null;
        if(optional1.isPresent())
            resource = optional1.get();
        InstanceIdentifier<ContextTag> finalresourceIID = resourceIID.child(ContextTag.class, new ContextTagKey(resource.getDefaultTag()));
        return finalresourceIID;
    }



    protected RFC7285Endpoint.CostResponse.Meta buildMeta(RFC7285CostType rfccostType) {
        RFC7285Endpoint.CostResponse.Meta meta = new RFC7285Endpoint.CostResponse.Meta();
        meta.costType = rfccostType;
        return meta;
    }

    //Cost.toString is a String like Ordinal [_cost=2, augmentation=[]],and I need to extrat "_cost=2" from this String
    protected String getCostValue(Cost cost){
        String costValue = null;
        String costString = cost.toString();
        String[] a1 = costString.split(",");
        String[] a2 = a1[0].split("=");
        costValue = a2[1];
        return costValue;
    }
    protected  String getAddressValue(Address address){
        String addressValue = null;
        if(address instanceof Ipv4AddressData){
            Ipv4AddressData ipv4 = (Ipv4AddressData)address;
            addressValue ="ipv4:" + ipv4.getIpv4().getValue();
        }else if(address instanceof Ipv6AddressData){
            Ipv6AddressData ipv6 = (Ipv6AddressData)address;
            addressValue = "ipv6:"+ipv6.getIpv6().getValue();
        }
        return addressValue;
    }

    protected Response buildOutput(QueryInput input, QueryOutput output) throws JsonProcessingException{

        //get costtype and endpoint source
        EndpointcostRequest request = (EndpointcostRequest) input.getRequest();
        EndpointcostParams params = request.getEndpointcostParams();
        EndpointFilterData filterData = (EndpointFilterData) params.getFilter();

        CostType costType = params.getCostType();
        String cost_metric = costType.getCostMetric().getValue();
        String cost_mode = costType.getCostMode();

        List<Source> endpoints_source = filterData.getEndpointFilter().getSource();

        //translate costtype to rfccosttype
        RFC7285CostType rfccostType = new RFC7285CostType();
        rfccostType.metric = cost_metric;
        rfccostType.mode = cost_mode;

        EndpointcostResponse ecResponse = (EndpointcostResponse)output.getResponse();
        EndpointcostData ecData = ecResponse.getEndpointcostData();
        EndpointCostmapData ecmData = null;
        if (ecData instanceof EndpointCostmapData) {
            ecmData = (EndpointCostmapData) ecResponse.getEndpointcostData();
        }else return Response.ok("there is not endpointcostmap").build();

        EndpointCostMap endpointCostMap = ecmData.getEndpointCostMap();
        List<EndpointCost> endpointCostList =  endpointCostMap.getEndpointCost();

        Map<String, Map<String, Object>> answer = new LinkedHashMap<String, Map<String, Object>>();

        for(Source endpointsource : endpoints_source) {
            Map<String, Object> dst2cost = new HashMap<>();
            String endpointSourceAddressValue = getAddressValue(endpointsource.getAddress());
            for (EndpointCost endpointCost : endpointCostList) {
                String costSourceAddressValue = getAddressValue(endpointCost.getSource().getAddress());
                String costDestinactionAddressValue = getAddressValue(endpointCost.getDestination().getAddress());
                if(endpointSourceAddressValue.equals(costSourceAddressValue))
                    dst2cost.put(costDestinactionAddressValue, getCostValue(endpointCost.getCost()));
            }
            answer.put(endpointSourceAddressValue, dst2cost);
        }

        RFC7285Endpoint.CostResponse rfcEndpointcost = new RFC7285Endpoint.CostResponse();
        rfcEndpointcost.meta = buildMeta(rfccostType);
        rfcEndpointcost.answer = answer;

        String responseString = mapper.writeValueAsString(rfcEndpointcost);
        return Response.ok(responseString, ALTO_ENDPOINTCOST).build();
    }
}
