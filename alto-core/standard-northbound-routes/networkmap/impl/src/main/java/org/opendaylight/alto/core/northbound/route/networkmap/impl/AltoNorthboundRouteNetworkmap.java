/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.route.networkmap.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.base.Optional;

import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;

import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285Endpoint.AddressGroup;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.networkmap.rev151021.Records;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.networkmap.rev151021.records.Record;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.networkmap.rev151021.records.RecordKey;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.networkmap.rev151021.records.record.AddressTypeMapping;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.networkmap.rev151021.records.record.AddressTypeMappingKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTagKey;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.PidName;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AddressTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AltoModelNetworkmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.QueryInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.ResourceTypeNetworkmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.alto.request.networkmap.request.NetworkmapRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.alto.response.networkmap.response.NetworkmapResponse;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.networkmap.request.data.NetworkmapFilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.networkmap.response.data.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.networkmap.response.data.network.map.Partition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rfc7285.rev151021.Ipv4PrefixList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rfc7285.rev151021.Ipv6PrefixList;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoNorthboundRouteNetworkmap implements BindingAwareProvider, AutoCloseable, AltoNorthboundRoute {
    public static final String NETWORKMAP_ROUTE = "networkmap";

    public static final String ALTO_NETWORKMAP_FILTER = "application/alto-networkmapfilter+json";
    public static final String ALTO_NETWORKMAP = "application/alto-networkmap+json";

    public static final String FIELD_PIDS = "pids";
    public static final String FIELD_ADDR_TYPES = "address-types";

    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(AltoNorthboundRouteNetworkmap.class);
    private static DataBroker m_dataBroker = null;
    private AltoNorthboundRouter m_router = null;

    private  static AltoModelNetworkmapService mapService = null;
    @Override
    public void onSessionInitiated(ProviderContext session) {
        m_dataBroker = session.getSALService(DataBroker.class);
        if (m_dataBroker == null) {
            LOG.error("Failed to init: data broker is null");
        }
        mapService = session.getRpcService(AltoModelNetworkmapService.class);
        LOG.info("AltoNorthboundRouteNetworkmap initiated");
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
    public Response getFullMap(@PathParam("path") String path) throws JsonProcessingException {
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
        return prepareInput(rid, new LinkedList<String>(), null);
    }

    protected QueryInput prepareInput(String path, List<String> pids, List<String> addressTypes) {
        //TODO
        QueryInputBuilder queryInputBuilder = new QueryInputBuilder();

        ReadOnlyTransaction rtx = m_dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<ContextTag> ctagIID = getResourceByPath(path, rtx);
        if(ctagIID == null){
            return null;
        }
        NetworkmapRequestBuilder networkmapRequestBuilder = new NetworkmapRequestBuilder();
        NetworkmapFilterBuilder networkmapFilterBuilder = new NetworkmapFilterBuilder();

        List<PidName> pidNames = new LinkedList<PidName>();
        for (String pid:pids){
            PidName p = new PidName(pid);
            pidNames.add(p);
        }
        networkmapFilterBuilder.setPid(pidNames);

        if (addressTypes != null) {
            List<Class<? extends AddressTypeBase>> addressTypeList = new LinkedList<>();
            for(String addressType : addressTypes){
                Class<? extends AddressTypeBase> type = getAddressTypeByName(addressType, path, rtx);
                addressTypeList.add(type);
            }
            networkmapFilterBuilder.setAddressType(addressTypeList);
        }

        networkmapRequestBuilder.setNetworkmapFilter(networkmapFilterBuilder.build());

        queryInputBuilder.setType(ResourceTypeNetworkmap.class);
        queryInputBuilder.setRequest(networkmapRequestBuilder.build());
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
        //grt resourceIID from nbr-networkmap.yang
        InstanceIdentifier<?> record2resourceIID = null;
        if(optional.isPresent()) {
            record2resourceIID = optional.get().getResourceIid();
        }
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

    public Class<? extends AddressTypeBase> getAddressTypeByName(String addressType,String path, ReadTransaction rtx){

        InstanceIdentifier<Record> resourceIID = InstanceIdentifier.builder(Records.class).child(Record.class , new RecordKey(new Uri(path))).build();
        InstanceIdentifier<AddressTypeMapping> atmIID = resourceIID.child(AddressTypeMapping.class, new AddressTypeMappingKey(addressType));
        Future<Optional<AddressTypeMapping>> future = rtx.read(LogicalDatastoreType.CONFIGURATION, atmIID);
        Optional<AddressTypeMapping> optional = null;
        try{
            optional = future.get();
        }catch (Exception e) {
            LOG.error("Reading AddressTypeMapping failed:",e);
            return null;
        }

        Class<? extends AddressTypeBase> addressClass = optional.get().getAddressType();
        return addressClass;

    }

    protected RFC7285NetworkMap.Meta buildMeta(InstanceIdentifier<?> iid) {
        RFC7285NetworkMap.Meta meta = new RFC7285NetworkMap.Meta();
        meta.vtag.rid = iid.firstKeyOf(Resource.class).getResourceId().getValue();
        meta.vtag.tag = iid.firstKeyOf(ContextTag.class).getTag().getValue();
        return meta;
    }

    protected Response buildOutput(QueryInput input, QueryOutput output) throws JsonProcessingException{
        //TODO
        NetworkmapResponse nmResponse = (NetworkmapResponse) output.getResponse();
        NetworkMap networkMap = nmResponse.getNetworkMap();

        RFC7285NetworkMap rfcnetworkmap = new RFC7285NetworkMap();
        List<Partition> partition = networkMap.getPartition();

        Map<String, AddressGroup> rfcNetowrkMap = new HashMap<String, AddressGroup>();
        for(int i = 0 ;i < partition.size(); i++){

            AddressGroup addressGroup = new AddressGroup();
            String pidName = partition.get(i).getPid().getValue();

            Ipv6PrefixList ipv6List = partition.get(i).getAugmentation(Ipv6PrefixList.class);
            List<String> ipv6ListString = new ArrayList<String>();

            Ipv4PrefixList ipv4List = partition.get(i).getAugmentation(Ipv4PrefixList.class);
            List<String> ipv4ListString = new ArrayList<String>();

            if(ipv6List != null) {
                for (Ipv6Prefix ipv6 : ipv6List.getIpv6()) {
                    ipv6ListString.add(ipv6.getValue());
                }
            }
            addressGroup.ipv6 = ipv6ListString;

            if(ipv4List != null){
                for (Ipv4Prefix ipv4 :ipv4List.getIpv4()){
                    ipv4ListString.add(ipv4.getValue());
                }
            }
            addressGroup.ipv4 = ipv4ListString;

            rfcNetowrkMap.put(pidName, addressGroup);
        }

        rfcnetworkmap.map = rfcNetowrkMap;

        rfcnetworkmap.meta = buildMeta(input.getServiceReference());

        String responseString = mapper.writeValueAsString(rfcnetworkmap);

        return Response.ok(responseString, ALTO_NETWORKMAP).build();
    }
}
