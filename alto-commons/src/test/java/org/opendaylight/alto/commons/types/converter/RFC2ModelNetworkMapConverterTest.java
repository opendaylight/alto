/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.converter;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.model150404.ModelJSONMapper;
import org.opendaylight.alto.commons.types.model150404.ModelNetworkMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RFC2ModelNetworkMapConverterTest {

    private RFC2ModelNetworkMapConverter nmconv;
    private RFC7285JSONMapper rfcMapper;
    private ModelJSONMapper modelMapper;

    @Before
    public void init() {
        nmconv = new RFC2ModelNetworkMapConverter();
        rfcMapper = new RFC7285JSONMapper();
        modelMapper = new ModelJSONMapper();
    }

    @Test
    public void onRFC2ModelNetworkMap() throws Exception {
        String testInput = "{\"network-map\": {\"PID1\": {\"ipv4\": [\"192.0.2.0/24\", \"198.51.100.0/25\"]}, \"PID2\": {\"ipv4\": [\"198.51.100.128/25\"]}, \"PID3\": {\"ipv6\": [\"::/0\"], \"ipv4\": [\"0.0.0.0/0\"]}}, \"meta\": {\"vtag\": {\"tag\": \"da65eca2eb7a10ce8b059740b0b2e3f8eb1d4785\", \"resource-id\": \"my-default-network-map\"}}}";
        RFC7285NetworkMap rfcMap = rfcMapper.asNetworkMap(testInput);
        ModelNetworkMap modelMap = nmconv.convert(rfcMap);
        String result = modelMapper.asJSON(modelMap);
        String testOutput = "{\"alto-service:resource-id\":\"my-default-network-map\",\"alto-service:tag\":\"da65eca2eb7a10ce8b059740b0b2e3f8eb1d4785\",\"alto-service:map\":[{\"alto-service:pid\":\"PID1\",\"alto-service:endpoint-address-group\":[{\"alto-service:address-type\":\"ipv4\",\"alto-service:endpoint-prefix\":[\"192.0.2.0/24\",\"198.51.100.0/25\"]}]},{\"alto-service:pid\":\"PID2\",\"alto-service:endpoint-address-group\":[{\"alto-service:address-type\":\"ipv4\",\"alto-service:endpoint-prefix\":[\"198.51.100.128/25\"]}]},{\"alto-service:pid\":\"PID3\",\"alto-service:endpoint-address-group\":[{\"alto-service:address-type\":\"ipv4\",\"alto-service:endpoint-prefix\":[\"0.0.0.0/0\"]},{\"alto-service:address-type\":\"ipv6\",\"alto-service:endpoint-prefix\":[\"::/0\"]}]}]}";
        Assert.assertEquals(testOutput, result);
    }
}
