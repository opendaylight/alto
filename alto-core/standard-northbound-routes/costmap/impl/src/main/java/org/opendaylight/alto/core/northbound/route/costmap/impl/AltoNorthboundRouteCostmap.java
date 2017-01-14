/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.route.costmap.impl;


import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Optional;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285CostType;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285VersionTag;

import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.costmap.rev151021.Records;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.costmap.rev151021.records.Record;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.costmap.rev151021.records.RecordKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTagKey;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.CostMetric;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.AltoModelCostmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.QueryInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.ResourceTypeCostmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.alto.request.costmap.request.CostmapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.alto.response.costmap.response.CostmapResponse;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.cost.type.data.CostTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.request.data.CostmapParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.CostmapResponseData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.CostmapSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.costmap.source.CostmapDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.costmap.source.costmap.destination.Cost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.costmap.filter.data.CostmapFilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.query.input.request.costmap.request.costmap.params.filter.CostmapFilterDataBuilder;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class AltoNorthboundRouteCostmap implements AltoNorthboundRoute {
    public static final String COSTMAP_ROUTE = "costmap";

    public static final String ALTO_COSTMAP_FILTER = "application/alto-costmapfilter+json";
    public static final String ALTO_COSTMAP = "application/alto-costmap+json";

    public static final String FIELD_PIDS = "pids";
    public static final String FIELD_COST_TYPE = "cost-type";
    public static final String FIELD_COST_MODE = "cost-mode";
    public static final String FIELD_COST_METRIC = "cost-metric";
    public static final String FIELD_PID_SOURCE = "srcs";
    public static final String FIELD_PID_DESTINSTION = "dsts";

    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(AltoNorthboundRouteCostmap.class);

    private DataBroker dataBroker = null;

    private AltoNorthboundRouter m_router = null;

    private  static AltoModelCostmapService mapService = null;
    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setMapService(final AltoModelCostmapService mapService) {
        this.mapService = mapService;
    }

    public void init() {

        if (dataBroker == null) {
            LOG.error("Failed to init: data broker is null");
        }


        LOG.info("AltoNorthboundRouteCostmap initiated");

    }

    public void register(AltoNorthboundRouter router) {
        m_router = router;
        m_router.addRoute("costmap", new AltoNorthboundRouteCostmap());
    }

    public void close() {
        m_router.removeRoute("costmap");
    }

    @Path("{path}")
    @GET
    @Produces({ALTO_COSTMAP, ALTO_ERROR})
    public Response getFullMap(@PathParam("path") String path) throws JsonProcessingException{
        QueryInput input = prepareDefaultInput(path, "numerical", "hopcount");
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

    @Path("{path}/{mode}")
    @GET
    @Produces({ALTO_COSTMAP, ALTO_ERROR})
    public Response getFullMap(@PathParam("path") String path,
                               @PathParam("mode") String mode) throws JsonProcessingException{
        QueryInput input = prepareDefaultInput(path, mode, "hopcount");
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

    @Path("{path}/{mode}/{metric}")
    @GET
    @Produces({ALTO_COSTMAP, ALTO_ERROR})
    public Response getFullMap(@PathParam("path") String path,
                               @PathParam("mode") String mode,
                               @PathParam("metric") String metric) throws JsonProcessingException{
        QueryInput input = prepareDefaultInput(path, mode, metric);
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
    @Consumes({ALTO_COSTMAP_FILTER})
    @Produces({ALTO_COSTMAP, ALTO_ERROR})
    public Response getFilteredMap(@PathParam("path") String path, String filter) throws JsonProcessingException {
        Response error;

        String cost_mode = null;
        String cost_metric = null;
        List<String> pid_source = null;
        List<String> pid_destination = null;


        try {
            JsonNode filterNode = mapper.readTree(filter);

            JsonNode _pids = filterNode.get(FIELD_PIDS);

            error = CostmapRouteChecker.checkMissing(_pids, FIELD_PIDS, filter);
            if (error != null)
                return error;

            error = CostmapRouteChecker.checkList(_pids, FIELD_PIDS, filter);
            if (error != null)
                return error;

            //need check pid_source
            JsonNode _pid_source = _pids.get(FIELD_PID_SOURCE);
            error = CostmapRouteChecker.checkList(_pid_source, FIELD_PID_SOURCE, filter);
            if (error != null){
                return error;
            }
            pid_source = arrayNode2List(FIELD_PID_SOURCE, (ArrayNode)_pid_source);

            //need check pid_destination
            JsonNode _pid_destination = _pids.get(FIELD_PID_DESTINSTION);
            error = CostmapRouteChecker.checkList(_pid_destination, FIELD_PID_DESTINSTION, filter);
            if (error != null){
                return error;
            }
            pid_destination = arrayNode2List(FIELD_PID_DESTINSTION, (ArrayNode) _pid_destination);

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
        QueryInput input = prepareInput(path, cost_mode, cost_metric, pid_source, pid_destination);
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

    protected QueryInput prepareDefaultInput(String rid, String cost_mode, String cost_metric) {
        /*
         * Set source and destination pids as empty so all PID pairs should be returned.
         *
         * See https://tools.ietf.org/html/rfc7285#section-11.3.2.3
         *
         * */
        return prepareInput(rid, cost_mode, cost_metric, new LinkedList<String>(), new LinkedList<String>());
    }

    protected QueryInput prepareInput(String path, String cost_mode, String cost_metric, List<String> pid_source, List<String> pid_destination) {
        //TODO
        QueryInputBuilder queryInputBuilder = new QueryInputBuilder();

        CostmapRequestBuilder costmapRequestBuilder = new CostmapRequestBuilder();
        CostmapParamsBuilder costmapParamsBuilder = new CostmapParamsBuilder();

        CostTypeBuilder costTypeBuilder = new CostTypeBuilder();
        costTypeBuilder.setCostMetric(new CostMetric(cost_metric));
        costTypeBuilder.setCostMode(cost_mode);

        CostmapFilterBuilder costmapFilterBuilder = new CostmapFilterBuilder();

        List<PidName> pidNames1 = new LinkedList<PidName>();
        for (String pid:pid_source){
            PidName p = new PidName(pid);
            pidNames1.add(p);
        }
        costmapFilterBuilder.setPidSource(pidNames1);

        List<PidName> pidNames2 = new LinkedList<PidName>();
        for (String pid:pid_destination){
            PidName p = new PidName(pid);
            pidNames2.add(p);
        }
        costmapFilterBuilder.setPidDestination(pidNames2);

        CostmapFilterDataBuilder filterdata = new CostmapFilterDataBuilder();
        filterdata.setCostmapFilter(costmapFilterBuilder.build());

        costmapParamsBuilder.setFilter(filterdata.build());
        costmapParamsBuilder.setCostType(costTypeBuilder.build());

        costmapRequestBuilder.setCostmapParams(costmapParamsBuilder.build());

        ReadOnlyTransaction rtx = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<ContextTag> ctagIID = getResourceByPath(path, rtx);
        if(ctagIID == null){
            return null;
        }

        queryInputBuilder.setRequest(costmapRequestBuilder.build());
        queryInputBuilder.setType(ResourceTypeCostmap.class);
        queryInputBuilder.setServiceReference(ctagIID);
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



    protected RFC7285CostMap.Meta buildMeta(InstanceIdentifier<?> iid, RFC7285CostType costtype) {
        RFC7285CostMap.Meta meta = new RFC7285CostMap.Meta();
        RFC7285VersionTag vtag = new RFC7285VersionTag();
        vtag.rid = iid.firstKeyOf(Resource.class).getResourceId().getValue();
        vtag.tag = iid.firstKeyOf(ContextTag.class).getTag().getValue();
        meta.netmap_tags.add(vtag);
        meta.costType = costtype;
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

    protected Response buildOutput(QueryInput input, QueryOutput output) throws JsonProcessingException{

        CostmapResponse costmapResponse = (CostmapResponse)output.getResponse();
        CostmapResponseData responseData = costmapResponse.getCostmapResponseData();

        RFC7285CostMap rfccostmap = new RFC7285CostMap();
        RFC7285CostType rfccosttype = new RFC7285CostType();

        rfccosttype.mode = responseData.getCostType().getCostMode();
        rfccosttype.metric = responseData.getCostType().getCostMetric().getValue();

        Map<String, Map<String, Object>> costmapsource
                = new LinkedHashMap<String, Map<String, Object>>();

        for(CostmapSource source:responseData.getCostmapSource()){

            Map<String, Object> dst2cost = new HashMap<String, Object>();

            for(CostmapDestination destination:source.getCostmapDestination()){
                String destinationName = destination.getPidDestination().getValue();
                Cost pidcost = destination.getCost();
                dst2cost.put(destinationName, getCostValue(pidcost));
            }

            costmapsource.put(source.getPidSource().getValue(), dst2cost);

        }

        rfccostmap.meta = buildMeta(input.getServiceReference(), rfccosttype);
        rfccostmap.map = costmapsource;

        String responseString = mapper.writeValueAsString(rfccostmap);

        return Response.ok(responseString, ALTO_COSTMAP).build();

    }
}
