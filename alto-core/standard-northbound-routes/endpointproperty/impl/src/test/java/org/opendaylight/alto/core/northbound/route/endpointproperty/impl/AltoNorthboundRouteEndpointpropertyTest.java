/*
 * Copyright © 2015 Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.northbound.route.endpointproperty.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285EndpointPropertyMap;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285VersionTag;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.GlobalEndpointProperty;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.PidName;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.SpecificEndpointProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.AltoModelEndpointpropertyService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.ResourceTypeEndpointproperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.alto.request.endpointproperty.request.EndpointpropertyRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.alto.response.endpointproperty.response.EndpointpropertyResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.endpointproperty.request.data.EndpointpropertyParams;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointproperty.filter.data.endpointproperty.filter.EndpointFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointproperty.filter.data.endpointproperty.filter.EndpointFilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointproperty.filter.data.endpointproperty.filter.PropertyFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointproperty.filter.data.endpointproperty.filter.PropertyFilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.EndpointPropertyMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.EndpointProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.EndpointPropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.Properties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.PropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.Source;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.properties.PropertyContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.input.request.endpointproperty.request.endpointproperty.params.filter.EndpointpropertyFilterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.input.request.endpointproperty.request.endpointproperty.params.filter.endpointproperty.filter.data.endpointproperty.filter.endpoint.filter.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.input.request.endpointproperty.request.endpointproperty.params.filter.endpointproperty.filter.data.endpointproperty.filter.property.filter.property.InputGlobalPropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.input.request.endpointproperty.request.endpointproperty.params.filter.endpointproperty.filter.data.endpointproperty.filter.property.filter.property.InputResourceSpecificPropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.EndpointPropertymapDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.endpoint.propertymap.data.endpoint.property.map.endpoint.property.properties.property.container.property.OutputGlobalPropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.endpoint.propertymap.data.endpoint.property.map.endpoint.property.properties.property.container.property.OutputResourceSpecificPropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.endpoint.propertymap.data.endpoint.property.map.endpoint.property.properties.property.value.PidNameBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AltoNorthboundRouteEndpointpropertyTest {
    static String ENDPOINTPROPERTY_FILTER = "{\"properties\" : [ \"my-default-networkmap.pid\","
                  + "                   \"priv:ietf-example-prop\" ],"
                  + "  \"endpoints\"  : [ \"ipv4:192.0.2.34\","
                  + "                   \"ipv4:203.0.113.129\" ]" + "}";
    static String PATH = "test-model-endpointproperty";
    static String RESOURCE_SPECIFIC_PROPERTY = "my-default-networkmap.pid";
    static String GLOBAL_PROPERTY = "priv:ietf-example-prop";
    static String THE_FIRST_IPv4_ADDRESS = "192.0.2.34";
    static String THE_SECOND_IPv4_ADDRESS = "203.0.113.129";
    static String DEFAULT_NETWORKMAP = "my-default-network-map.pid";
    static String DEFAULT_GLOBAL_PROPERTY_NAME = "priv:ietf-example-prop";
    static String TARGET_ENDPOINT_PREOPERTIES =
          "{\"ipv4:192.0.2.34\": {\"my-default-network-map.pid\": \"PID1\","
        + "                      \"priv:ietf-example-prop\": \"1\" },"
        + "\"ipv4:203.0.113.129\": {\"my-default-network-map.pid\": \"PID3\" }}";
    static String FIELD_ENDPOINT_PROPERTIES = "endpoint-properties";
    static String DEFAULT_RID = "test-model-endpointproperty";
    static String DEFAULT_TAG = "75ed013b3cb58f896e839582504f622838ce670f";

    final AltoNorthboundRouteEndpointproperty anbre= new AltoNorthboundRouteEndpointproperty();

    @Test
    public void testPrepareInput() {

        AltoNorthboundRouteEndpointproperty anbreSpy = spy(anbre);
        BindingAwareBroker.ProviderContext session = mock(BindingAwareBroker.ProviderContext.class);
        InstanceIdentifier<ContextTag> ctagIID = InstanceIdentifier.create(ContextTag.class);
        JsonNode filterNode = EndpointpropertyRouteChecker.checkJsonSyntax(ENDPOINTPROPERTY_FILTER);
        JsonNode _properties = filterNode.get(AltoNorthboundRouteEndpointproperty.FIELD_PROPERTIES);
        JsonNode _endpoints = filterNode.get(AltoNorthboundRouteEndpointproperty.FIELD_ENDPOINTS);

        doReturn(ctagIID)
            .when(anbreSpy)
            .getResourceByPath(eq(PATH), (ReadOnlyTransaction) anyObject());
        when(session.getSALService(DataBroker.class)).thenReturn(new DataBroker() {
            @Override public ReadOnlyTransaction newReadOnlyTransaction() {
                return null;
            }

            @Override public ReadWriteTransaction newReadWriteTransaction() {
                return null;
            }

            @Override public WriteTransaction newWriteOnlyTransaction() {
                return null;
            }

            @Override public ListenerRegistration<DataChangeListener> registerDataChangeListener(
                LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<?> instanceIdentifier,
                DataChangeListener dataChangeListener, DataChangeScope dataChangeScope) {
                return null;
            }

            @Override public BindingTransactionChain createTransactionChain(
                TransactionChainListener transactionChainListener) {
                return null;
            }

            @Nonnull @Override
            public <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L> registerDataTreeChangeListener(
                @Nonnull DataTreeIdentifier<T> dataTreeIdentifier, @Nonnull L l) {
                return null;
            }
        });

        anbreSpy.onSessionInitiated(session);
        QueryInput input = anbreSpy.prepareInput(PATH, _properties.elements(), _endpoints.elements());
        EndpointpropertyRequest request = (EndpointpropertyRequest)input.getRequest();
        EndpointpropertyParams params = request.getEndpointpropertyParams();
        List<PropertyFilter> listProperty = ((EndpointpropertyFilterData) params.getFilter()).getEndpointpropertyFilter().getPropertyFilter();
        List<EndpointFilter> listEndpoint = ((EndpointpropertyFilterData) params.getFilter()).getEndpointpropertyFilter().getEndpointFilter();
        List<PropertyFilter> expectedListProperty = new LinkedList<>();
        List<EndpointFilter> expectedListEndpoint = new LinkedList<>();

        expectedListProperty.add(new PropertyFilterBuilder()
            .setProperty(new InputResourceSpecificPropertyBuilder()
                .setResourceSpecificProperty(
                    new SpecificEndpointProperty(RESOURCE_SPECIFIC_PROPERTY))
                .build())
            .build()
        );
        expectedListProperty.add(new PropertyFilterBuilder()
            .setProperty(new InputGlobalPropertyBuilder()
                .setGlobalProperty(
                    new GlobalEndpointProperty(GLOBAL_PROPERTY))
                .build())
            .build()
        );

        expectedListEndpoint.add(new EndpointFilterBuilder()
            .setAddress(
                new Ipv4Builder()
                    .setIpv4(new Ipv4Address(THE_FIRST_IPv4_ADDRESS))
                    .build())
            .build()
        );
        expectedListEndpoint.add(new EndpointFilterBuilder()
            .setAddress(
                new Ipv4Builder()
                    .setIpv4(new Ipv4Address(THE_SECOND_IPv4_ADDRESS))
                    .build())
            .build()
        );

        assertEquals(expectedListProperty, listProperty);
        assertEquals(expectedListEndpoint, listEndpoint);
    }

    @Test
    public void testBuildOutput() throws IOException, ExecutionException, InterruptedException{
        AltoNorthboundRouteEndpointproperty anbreSpy = spy(anbre);
        BindingAwareBroker.ProviderContext session = mock(BindingAwareBroker.ProviderContext.class);
        InstanceIdentifier<ContextTag> ctagIID = InstanceIdentifier.create(ContextTag.class);

        AltoModelEndpointpropertyService epService = mock(AltoModelEndpointpropertyService.class);
        Future<RpcResult<QueryOutput>> future = mock(Future.class);
        RpcResult<QueryOutput> rpcResult =mock(RpcResult.class);

        LinkedList<EndpointProperty> eppist = new LinkedList<>();

        SourceBuilder sourceBuilder = new SourceBuilder();
        Source THE_FIRST_SOURCE = sourceBuilder.setAddress(
            new Ipv4Builder()
                .setIpv4(new Ipv4Address(THE_FIRST_IPv4_ADDRESS))
                .build())
            .build();
        Source THE_SECOND_SOURCE = sourceBuilder.setAddress(
            new Ipv4Builder()
                .setIpv4(new Ipv4Address(THE_SECOND_IPv4_ADDRESS))
                .build())
            .build();

        EndpointPropertyBuilder epBuilder = new EndpointPropertyBuilder();
        List<Properties> THE_FIRST_LIST_PROPERTIES = new LinkedList<>();
        List<Properties> THE_SECOND_LIST_PROPERTIES = new LinkedList<>();

        PropertiesBuilder propertiesBuilder = new PropertiesBuilder();
        PropertyContainerBuilder pcBuilder = new PropertyContainerBuilder();

        PidName pidName = new PidName("PID1");
        PidNameBuilder pidNameBuilder = new PidNameBuilder();
        pidNameBuilder.setValue(pidName);
        pcBuilder.setProperty(
            new OutputResourceSpecificPropertyBuilder()
                .setResourceSpecificProperty(new SpecificEndpointProperty(DEFAULT_NETWORKMAP)).build());
        propertiesBuilder.setPropertyContainer(pcBuilder.build()).setPropertyValue(pidNameBuilder.build());
        THE_FIRST_LIST_PROPERTIES.add(propertiesBuilder.build());

        pcBuilder.setProperty(
            new OutputGlobalPropertyBuilder()
                .setGlobalProperty(new GlobalEndpointProperty(DEFAULT_GLOBAL_PROPERTY_NAME))
                .build()
        );
        PidName fackGlobalProperty = new PidName("1");
        pidNameBuilder.setValue(fackGlobalProperty);
        propertiesBuilder.setPropertyContainer(pcBuilder.build()).setPropertyValue(pidNameBuilder.build());
        THE_FIRST_LIST_PROPERTIES.add(propertiesBuilder.build());

        pidName = new PidName("PID3");
        pidNameBuilder.setValue(pidName);
        pcBuilder.setProperty(
            new OutputResourceSpecificPropertyBuilder().
                setResourceSpecificProperty(new SpecificEndpointProperty(DEFAULT_NETWORKMAP)).build());
        propertiesBuilder.setPropertyContainer(pcBuilder.build()).setPropertyValue(pidNameBuilder.build());
        THE_SECOND_LIST_PROPERTIES.add(propertiesBuilder.build());

        eppist.add(
            epBuilder
                .setSource(THE_FIRST_SOURCE)
                .setProperties(THE_FIRST_LIST_PROPERTIES)
                .build());
        eppist.add(
            epBuilder
                .setSource(THE_SECOND_SOURCE)
                .setProperties(THE_SECOND_LIST_PROPERTIES).build());

        EndpointPropertyMapBuilder endpointPropertyMapBuilder = new EndpointPropertyMapBuilder();
        endpointPropertyMapBuilder.setEndpointProperty(eppist);

        EndpointPropertymapDataBuilder endpointPropertymapDataBuilder = new EndpointPropertymapDataBuilder();
        endpointPropertymapDataBuilder.setEndpointPropertyMap(endpointPropertyMapBuilder.build());

        EndpointpropertyResponseBuilder endpointpropertyResponseBuilder = new EndpointpropertyResponseBuilder();
        endpointpropertyResponseBuilder.setEndpointpropertyData(endpointPropertymapDataBuilder.build());

        QueryOutputBuilder queryOutputBuilder = new QueryOutputBuilder();
        queryOutputBuilder.setType(ResourceTypeEndpointproperty.class).setResponse(endpointpropertyResponseBuilder.build());
        when(rpcResult.getResult()).thenReturn(queryOutputBuilder.build());
        when(future.get()).thenReturn(rpcResult);
        when(epService.query((QueryInput) anyObject())).thenReturn(future);
        when(session.getRpcService(AltoModelEndpointpropertyService.class)).thenReturn(epService);
        when(session.getSALService(DataBroker.class)).thenReturn(new DataBroker() {
            @Override public ReadOnlyTransaction newReadOnlyTransaction() {
                return null;
            }

            @Override public ReadWriteTransaction newReadWriteTransaction() {
                return null;
            }

            @Override public WriteTransaction newWriteOnlyTransaction() {
                return null;
            }

            @Override public ListenerRegistration<DataChangeListener> registerDataChangeListener(
                LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<?> instanceIdentifier,
                DataChangeListener dataChangeListener, DataChangeScope dataChangeScope) {
                return null;
            }

            @Override public BindingTransactionChain createTransactionChain(
                TransactionChainListener transactionChainListener) {
                return null;
            }

            @Nonnull @Override
            public <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L> registerDataTreeChangeListener(
                @Nonnull DataTreeIdentifier<T> dataTreeIdentifier, @Nonnull L l) {
                return null;
            }
        });
        doReturn(ctagIID).when(anbreSpy).getResourceByPath(eq(PATH), (ReadOnlyTransaction) anyObject());
        RFC7285EndpointPropertyMap.Meta meta = new RFC7285EndpointPropertyMap.Meta();
        RFC7285VersionTag vtag = new RFC7285VersionTag();
        vtag.rid = DEFAULT_RID;
        vtag.tag = DEFAULT_TAG;
        meta.netmap_tags = new LinkedList<>();
        meta.netmap_tags.add(vtag);
        doReturn(meta).when(anbreSpy).buildMeta((InstanceIdentifier<?>) anyObject());

        anbreSpy.onSessionInitiated(session);
        Response response = anbreSpy.getEndpointProperty(PATH, ENDPOINTPROPERTY_FILTER);
        String stringResponse = response.getEntity().toString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseNode = mapper.readTree(stringResponse);
        JsonNode endpointProperties = responseNode.get(FIELD_ENDPOINT_PROPERTIES);
        JsonNode expectedEndpointProperties = mapper.readTree(TARGET_ENDPOINT_PREOPERTIES);

        assertEquals(expectedEndpointProperties, endpointProperties);
    }
}
