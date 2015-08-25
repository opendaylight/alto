/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.converter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
//import org.opendaylight.alto.commons.types.model150404.ModelNetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class YANGJSON2RFCNetworkMapConverterTest {
    private RFC7285JSONMapper rfcMapper;
    private YANGJSON2RFCNetworkMapConverter ynmc;
    private ObjectMapper objectMapper;
    private RFC2ModelNetworkMapConverter nmconv;
    @Before
    public void init() {
        rfcMapper = new RFC7285JSONMapper();
        objectMapper = new ObjectMapper();
        ynmc = new YANGJSON2RFCNetworkMapConverter();
    }

    @Test
    public void onModel2RFCNetworkMap() throws Exception {
        String testInput = "{\"resourceId\":{\"value\":\"my-default-network-map\"},\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMap\",\"tag\":{\"value\":\"da65eca2eb7a10ce8b059740b0b2e3f8eb1d4785\"},\"key\":{\"resourceId\":{\"value\":\"my-default-network-map\"}},\"map\":[{\"endpointAddressGroup\":[{\"endpointPrefix\":[{\"ipv4Prefix\":null,\"ipv6Prefix\":{\"value\":\"::/0\"},\"value\":\"::/0\"}],\"addressType\":{\"value\":\"ipv6\",\"enumeration\":\"Ipv6\"},\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup\",\"key\":{\"addressType\":{\"value\":\"ipv6\",\"enumeration\":\"Ipv6\"}}},{\"endpointPrefix\":[{\"ipv4Prefix\":{\"value\":\"0.0.0.0/0\"},\"ipv6Prefix\":null,\"value\":\"0.0.0.0/0\"}],\"addressType\":{\"value\":\"ipv4\",\"enumeration\":\"Ipv4\"},\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup\",\"key\":{\"addressType\":{\"value\":\"ipv4\",\"enumeration\":\"Ipv4\"}}}],\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map\",\"key\":{\"pid\":{\"value\":\"PID3\"}},\"pid\":{\"value\":\"PID3\"}},{\"endpointAddressGroup\":[{\"endpointPrefix\":[{\"ipv4Prefix\":{\"value\":\"198.51.100.128/25\"},\"ipv6Prefix\":null,\"value\":\"198.51.100.128/25\"}],\"addressType\":{\"value\":\"ipv4\",\"enumeration\":\"Ipv4\"},\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup\",\"key\":{\"addressType\":{\"value\":\"ipv4\",\"enumeration\":\"Ipv4\"}}}],\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map\",\"key\":{\"pid\":{\"value\":\"PID2\"}},\"pid\":{\"value\":\"PID2\"}},{\"endpointAddressGroup\":[{\"endpointPrefix\":[{\"ipv4Prefix\":{\"value\":\"192.0.2.0/24\"},\"ipv6Prefix\":null,\"value\":\"192.0.2.0/24\"},{\"ipv4Prefix\":{\"value\":\"198.51.100.0/25\"},\"ipv6Prefix\":null,\"value\":\"198.51.100.0/25\"}],\"addressType\":{\"value\":\"ipv4\",\"enumeration\":\"Ipv4\"},\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup\",\"key\":{\"addressType\":{\"value\":\"ipv4\",\"enumeration\":\"Ipv4\"}}}],\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map\",\"key\":{\"pid\":{\"value\":\"PID1\"}},\"pid\":{\"value\":\"PID1\"}}]}";
        ObjectNode node = (ObjectNode) objectMapper.readTree(testInput);
        RFC7285NetworkMap rnm = ynmc.convert(node);
        String result = rfcMapper.asJSON(rnm);
        String testOutput = "{\"meta\":{\"vtag\":{\"resource-id\":\"my-default-network-map\",\"tag\":\"da65eca2eb7a10ce8b059740b0b2e3f8eb1d4785\"}},\"network-map\":{\"PID3\":{\"ipv4\":[\"0.0.0.0/0\"],\"ipv6\":[\"::/0\"]},\"PID2\":{\"ipv4\":[\"198.51.100.128/25\"]},\"PID1\":{\"ipv4\":[\"192.0.2.0/24\",\"198.51.100.0/25\"]}}}";
        Assert.assertEquals(testOutput, result);
    }
}
