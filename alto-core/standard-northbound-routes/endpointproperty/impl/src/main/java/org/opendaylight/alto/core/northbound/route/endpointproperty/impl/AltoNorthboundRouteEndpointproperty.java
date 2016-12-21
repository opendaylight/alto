/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.route.endpointproperty.impl;


import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.opendaylight.alto.core.northbound.api.exception.AltoErrorInvalidFieldValue;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285EndpointPropertyMap;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285VersionTag;

import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.endpointproperty.rev151021.records.Record;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.endpointproperty.rev151021.records.RecordKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTagKey;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.SpecificEndpointProperty;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.GlobalEndpointProperty;
import org.opendaylight.yang.gen.v1.urn.alto.northbound.route.endpointproperty.rev151021.Records;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.AltoModelEndpointpropertyService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.QueryInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.ResourceTypeEndpointproperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.alto.request.endpointproperty.request.EndpointpropertyRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.alto.response.endpointproperty.response.EndpointpropertyResponse;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.endpointproperty.request.data.EndpointpropertyParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.Ipv4AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.Ipv6AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointproperty.filter.data.EndpointpropertyFilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointproperty.filter.data.endpointproperty.filter.EndpointFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointproperty.filter.data.endpointproperty.filter.EndpointFilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointproperty.filter.data.endpointproperty.filter.PropertyFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointproperty.filter.data.endpointproperty.filter.PropertyFilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.EndpointPropertyMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.EndpointProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.Properties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.Source;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.properties.PropertyValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.input.request.endpointproperty.request.endpointproperty.params.filter.EndpointpropertyFilterDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.input.request.endpointproperty.request.endpointproperty.params.filter.endpointproperty.filter.data.endpointproperty.filter.endpoint.filter.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.input.request.endpointproperty.request.endpointproperty.params.filter.endpointproperty.filter.data.endpointproperty.filter.endpoint.filter.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.input.request.endpointproperty.request.endpointproperty.params.filter.endpointproperty.filter.data.endpointproperty.filter.property.filter.property.InputGlobalPropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.input.request.endpointproperty.request.endpointproperty.params.filter.endpointproperty.filter.data.endpointproperty.filter.property.filter.property.InputResourceSpecificPropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.EndpointPropertymapData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.endpoint.propertymap.data.endpoint.property.map.endpoint.property.properties.property.container.property.OutputGlobalProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.endpoint.propertymap.data.endpoint.property.map.endpoint.property.properties.property.container.property.OutputResourceSpecificProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.endpoint.propertymap.data.endpoint.property.map.endpoint.property.properties.property.value.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.typed.address.data.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.typed.property.data.Property;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Future;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public class AltoNorthboundRouteEndpointproperty implements AltoNorthboundRoute {
    public static final String ALTO_ENDPOINTPROPERTY_FILTER = "application/alto-endpointpropparams+json";
    public static final String ALTO_ENDPOINTPROPERTY = "application/alto-endpointprop+json";
    public static final String FIELD_PROPERTIES = "properties";
    public static final String FIELD_ENDPOINTS = "endpoints";
    public static final String IPv4_PREFIX = "ipv4:";
    public static final String IPv6_PREFIX = "ipv6:";
    public static final String PRIV_PREFIX = "priv:";

    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(AltoNorthboundRouteEndpointproperty.class);

    private DataBroker dataBroker = null;

    private AltoNorthboundRouter m_router = null;

    private  static AltoModelEndpointpropertyService mapService = null;
    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setMapService(final AltoModelEndpointpropertyService mapService) {
        this.mapService = mapService;
    }

    public void init() {

        if (dataBroker == null) {
            LOG.error("Failed to init: data broker is null");
        }

        LOG.info("AltoNorthboundRouteEndpointProperty initiated");
    }

    public void register(AltoNorthboundRouter router) {
        m_router = router;
        m_router.addRoute("endpointproperty", new AltoNorthboundRouteEndpointproperty());
    }

    public void close() {
        m_router.removeRoute("endpointproperty");
    }

    @Path("{path}")
    @POST
    @Consumes({ALTO_ENDPOINTPROPERTY_FILTER})
    @Produces({ALTO_ENDPOINTPROPERTY, ALTO_ERROR})
    public Response getEndpointProperty(@PathParam("path") String path, String content) {
        JsonNode filterNode = EndpointpropertyRouteChecker.checkJsonSyntax(content);
        JsonNode _properties = filterNode.get(FIELD_PROPERTIES);
        EndpointpropertyRouteChecker.checkMissing(_properties, FIELD_PROPERTIES);
        EndpointpropertyRouteChecker.checkList(_properties, FIELD_PROPERTIES);

        JsonNode _endpoints = filterNode.get(FIELD_ENDPOINTS);
        EndpointpropertyRouteChecker.checkMissing(_endpoints, FIELD_ENDPOINTS);
        EndpointpropertyRouteChecker.checkList(_endpoints, FIELD_ENDPOINTS);

        QueryInput input = prepareInput(path, _properties.elements(), _endpoints.elements());
        LOG.info(input.toString());

        Future<RpcResult<QueryOutput>> outputFuture = mapService.query(input);
        QueryOutput output = null;
        try {
            output = outputFuture.get().getResult();
        } catch (Exception e) {
            LOG.warn("get output failed:" , e);
        }
        Response response = null;
        try {
            response = buildOutput(input, output);
        }
        catch (Exception E){

        }
        if(response != null)
            return response;
        else
            return Response.status(404).build();
    }

    private Property constructProperty (String property) {
        try {
            if (property.contains(".")) {
                return new InputResourceSpecificPropertyBuilder()
                        .setResourceSpecificProperty(
                                new SpecificEndpointProperty(property)).build();
            } else if (property.equals(PRIV_PREFIX)) {
                throw new AltoErrorInvalidFieldValue(FIELD_PROPERTIES, property);
            } else {
                return new InputGlobalPropertyBuilder()
                        .setGlobalProperty(
                                new GlobalEndpointProperty(property)).build();
            }
        }
        catch (IllegalArgumentException E) {
            throw new AltoErrorInvalidFieldValue(FIELD_PROPERTIES, property);
        }
    }

    private Address constructAddress (String address) {
        try {
            if (address.contains(IPv4_PREFIX)) {
                return new Ipv4Builder().setIpv4(
                    new Ipv4Address(address.replace(IPv4_PREFIX, ""))).build();
            } else if (address.contains(IPv6_PREFIX)) {
                return new Ipv6Builder().setIpv6(
                    new Ipv6Address(address.replace(IPv6_PREFIX, ""))).build();
            } else {
                throw new AltoErrorInvalidFieldValue(FIELD_ENDPOINTS, address);
            }
        }
        catch (IllegalArgumentException E) {
            throw new AltoErrorInvalidFieldValue(FIELD_PROPERTIES, address);
        }
    }

    protected List<PropertyFilter> constructListPropertyFilter(Iterator<JsonNode> properties) {
        List<PropertyFilter> listPropertyFilter = new LinkedList<>();
        while (properties.hasNext()) {
            listPropertyFilter.add(
                    new PropertyFilterBuilder()
                            .setProperty(constructProperty(properties.next().asText())).build());
        }
        return listPropertyFilter;
    }

    protected List<EndpointFilter> constructListEndpointFilter(Iterator<JsonNode> endpoints) {
        List<EndpointFilter> listEndpointFilter = new LinkedList<>();
        while (endpoints.hasNext()) {
            listEndpointFilter.add(
                    new EndpointFilterBuilder()
                            .setAddress(constructAddress(endpoints.next().asText())).build());
        }
        return listEndpointFilter;
    }

    protected QueryInput buildQueryInput(InstanceIdentifier<ContextTag> ctagIID,
                                         List<PropertyFilter> listPropertyFilter,
                                         List<EndpointFilter> listEndpointFilter ) {
        return new QueryInputBuilder()
                .setType(ResourceTypeEndpointproperty.class)
                .setServiceReference(ctagIID)
                .setRequest(
                        new EndpointpropertyRequestBuilder()
                                .setEndpointpropertyParams(
                                        new EndpointpropertyParamsBuilder()
                                                .setFilter(
                                                        new EndpointpropertyFilterDataBuilder()
                                                                .setEndpointpropertyFilter(
                                                                        new EndpointpropertyFilterBuilder()
                                                                                .setEndpointFilter(listEndpointFilter)
                                                                                .setPropertyFilter(listPropertyFilter)
                                                                                .build())
                                                                .build())
                                                .build())
                                .build())
                .build();
    }

    protected QueryInput prepareInput(String path, Iterator<JsonNode> properties, Iterator<JsonNode> endpoints) {
        ReadOnlyTransaction rtx = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<ContextTag> ctagIID = getResourceByPath(path, rtx);
        if(ctagIID == null){
            return null;
        }
        List<PropertyFilter> listPropertyFilter = constructListPropertyFilter(properties);
        List<EndpointFilter> listEndpointFilter = constructListEndpointFilter(endpoints);
        return buildQueryInput(ctagIID, listPropertyFilter, listEndpointFilter);
    }

    public InstanceIdentifier<ContextTag> getResourceByPath(String path, ReadOnlyTransaction transaction){
        //get iid from (list Records)
        InstanceIdentifier<Record> recordIID = InstanceIdentifier.builder(Records.class)
                .child(Record.class, new RecordKey(new Uri(path))).build();
        Future<Optional<Record>> recordFuture = transaction.read(LogicalDatastoreType.CONFIGURATION, recordIID);
        Optional<Record> recordOptional = null;
        try {
            recordOptional = recordFuture.get();
        } catch(Exception e){
            LOG.error("Reading Record failed", e);
            return null;
        }
        //get resourceIID from nbr-endpointproperty.yang
        InstanceIdentifier<?> record2resourceIID = null;
        if(recordOptional.isPresent()) {
            record2resourceIID = recordOptional.get().getResourceIid();
        }
        InstanceIdentifier<Resource> resourceIID = (InstanceIdentifier<Resource>)record2resourceIID;
        Future<Optional<Resource>> resourceFuture = transaction.read(LogicalDatastoreType.OPERATIONAL, resourceIID);
        Optional<Resource> resourceOptional = null;
        try{
            resourceOptional = resourceFuture.get();
        }
        catch(Exception e){
            LOG.error("Read resource failed:", e);
            return null;
        }
        Resource resource = null;
        if(resourceOptional.isPresent())
            resource = resourceOptional.get();
        InstanceIdentifier<ContextTag> finalresourceIID = resourceIID
                .child(ContextTag.class, new ContextTagKey(resource.getDefaultTag()));
        return finalresourceIID;
    }

    protected  String getAddressValue(Address address){
        String addressValue = null;
        if(address instanceof Ipv4AddressData){
            Ipv4AddressData ipv4 = (Ipv4AddressData)address;
            addressValue = IPv4_PREFIX + ipv4.getIpv4().getValue();
        }else if(address instanceof Ipv6AddressData){
            Ipv6AddressData ipv6 = (Ipv6AddressData)address;
            addressValue = IPv6_PREFIX + ipv6.getIpv6().getValue();
        }
        return addressValue;
    }

    protected Response buildOutput(QueryInput input, QueryOutput output) throws JsonProcessingException {
        EndpointpropertyResponse epResponse = (EndpointpropertyResponse) output.getResponse();
        EndpointPropertymapData epMapData = (EndpointPropertymapData) epResponse.getEndpointpropertyData();
        EndpointPropertyMap epMap = epMapData.getEndpointPropertyMap();
        List<EndpointProperty> linkEndpointProperty = epMap.getEndpointProperty();
        Map<String, Map<String, String>> endpointpropertyMap = new HashMap<>();
        for (EndpointProperty ep: linkEndpointProperty) {
            Map<String, String> propertyMap = new HashMap<>();
            Source source = ep.getSource();
            String endpointSourceAddress = getAddressValue(source.getAddress());
            List<Properties>
                    properties = ep.getProperties();
            for (Properties property: properties) {
                PropertyValue propertyValue = property.getPropertyValue();
                if (property.getPropertyContainer().getProperty() instanceof OutputResourceSpecificProperty)
                    propertyMap.put(((OutputResourceSpecificProperty)property.getPropertyContainer().getProperty()).getResourceSpecificProperty().getValue(), ((PidName)propertyValue).getValue().getValue());
                else {
                    propertyMap.put(((OutputGlobalProperty)property.getPropertyContainer().getProperty()).getGlobalProperty().getValue(), ((PidName)propertyValue).getValue().getValue());
                }
            }

            endpointpropertyMap.put(endpointSourceAddress, propertyMap);
        }

        RFC7285EndpointPropertyMap rfcEPM= new RFC7285EndpointPropertyMap();
        rfcEPM.map = endpointpropertyMap;
        rfcEPM.meta = buildMeta(input.getServiceReference());
        String responseString = mapper.writeValueAsString(rfcEPM);

        return Response.ok(responseString, ALTO_ENDPOINTPROPERTY).build();
    }

    protected RFC7285EndpointPropertyMap.Meta buildMeta(InstanceIdentifier<?> iid) {
        RFC7285EndpointPropertyMap.Meta meta = new RFC7285EndpointPropertyMap.Meta();
        RFC7285VersionTag vtag = new RFC7285VersionTag();
        vtag.rid = iid.firstKeyOf(Resource.class).getResourceId().getValue();
        vtag.tag = iid.firstKeyOf(ContextTag.class).getTag().getValue();
        meta.netmap_tags = new LinkedList<>();
        meta.netmap_tags.add(vtag);
        return meta;
    }
}
