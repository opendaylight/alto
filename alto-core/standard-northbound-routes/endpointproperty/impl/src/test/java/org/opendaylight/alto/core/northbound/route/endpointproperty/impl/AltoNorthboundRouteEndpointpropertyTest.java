package org.opendaylight.alto.core.northbound.route.endpointproperty.impl;

import org.junit.Test;

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

    /*
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
    }*/
}
