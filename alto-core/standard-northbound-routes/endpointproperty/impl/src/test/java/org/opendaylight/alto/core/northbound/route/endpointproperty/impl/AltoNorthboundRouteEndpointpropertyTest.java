package org.opendaylight.alto.core.northbound.route.endpointproperty.impl;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.alto.response.endpointproperty.response.EndpointpropertyResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.EndpointPropertyMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.EndpointProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.Source;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.SourceBuilder;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.properties.Property;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.properties.PropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.EndpointPropertymapDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.endpoint.propertymap.data.endpoint.property.map.endpoint.property.source.address.Ipv4Builder;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by shaw on 1/12/16.
 */
public class AltoNorthboundRouteEndpointpropertyTest {
    AltoNorthboundRouteEndpointproperty anre = new AltoNorthboundRouteEndpointproperty();

    @Test
    public void testJsonParser() {
        String filter = "  {\n" +
                "    \"properties\" : [ \"my-default-networkmap.pid\",\n" +
                "                     \"priv:ietf-example-prop\" ],\n" +
                "    \"endpoints\"  : [ \"ipv4:192.0.2.34\",\n" +
                "                     \"ipv4:203.0.113.129\" ]\n" +
                "  }";
        try {
            anre.getEndpointProperty(null, filter);
        } catch (Exception e) {

        }
    }

    @Test
    public void testInput() {
        LinkedList<String> properties = new LinkedList<String>();
        properties.add("my-default-networkmap.pid");
        properties.add("priv:ietf-example-prop");

        LinkedList<String> endpoints = new LinkedList<String>();
        endpoints.add("ipv4:192.0.2.34");
        endpoints.add("ipv4:203.0.113.129");

        anre.prepareInput(null, null, null);
    }

    @Test
    public void testOutput() {
        List<EndpointProperty> listEndpointProperties = new LinkedList<>();
        Source source1 = new SourceBuilder().setAddress(new Ipv4Builder().setIpv4(new Ipv4Address("192.0.2.34")).build()).build();
        Source source2 = new SourceBuilder().setAddress(new Ipv4Builder().setIpv4(new Ipv4Address("203.0.113.129")).build()).build();
        //Property property1 = new PropertyBuilder().setProperty().build();
        QueryOutput testoutput = new QueryOutputBuilder()
                .setResponse(
                        new EndpointpropertyResponseBuilder()
                                .setEndpointpropertyData(
                                        new EndpointPropertymapDataBuilder()
                                                .setEndpointPropertyMap(
                                                        new EndpointPropertyMapBuilder()
                                                                .setEndpointProperty(listEndpointProperties)
                                                                .build())
                                                .build())
                                .build())
                .build();
    }
}
