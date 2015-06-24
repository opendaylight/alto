/**
 * Copyright (c) 2014 André Martins, Colin Dixon, Evan Zeller and others. All
 * rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.commons.types.converter;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.model150404.ModelJSONMapper;
import org.opendaylight.alto.commons.types.model150404.ModelNetworkMap;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import java.io.FileWriter;
// import java.io.Writer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class RFC2ModelNetworkMapConverterTest {

    private RFC2ModelNetworkMapConverter nmconv;
    private DataBroker dataBroker;
    private RFC7285JSONMapper rfcMapper;
    private ModelJSONMapper modelMapper;
    // private ObjectMapper mapper;

    @Before
    public void init() {
        dataBroker = mock(DataBroker.class);
        nmconv = new RFC2ModelNetworkMapConverter();
        rfcMapper = new RFC7285JSONMapper();
        modelMapper = new ModelJSONMapper();
        // mapper = new ObjectMapper();
    }

    @Test
    public void onRFC2ModelNetworkMap() throws Exception {
        // Define Test Input String
        String testInput = "{\"network-map\": {\"PID1\": {\"ipv4\": [\"192.0.2.0/24\", \"198.51.100.0/25\"]}, \"PID2\": {\"ipv4\": [\"198.51.100.128/25\"]}, \"PID3\": {\"ipv6\": [\"::/0\"], \"ipv4\": [\"0.0.0.0/0\"]}}, \"meta\": {\"vtag\": {\"tag\": \"da65eca2eb7a10ce8b059740b0b2e3f8eb1d4785\", \"resource-id\": \"my-default-network-map\"}}}";

        // Map RFC7285JSON to Object
        RFC7285NetworkMap rfcMap = rfcMapper.asNetworkMap(testInput);
        // RFC7285NetworkMap rfcMap = mapper.readValue(testInput, RFC7285NetworkMap.class);
        // Convert RFC7285 Object to YANG Model Object
        ModelNetworkMap modelMap = nmconv.convert(rfcMap);

        // Map YANG Model to JSON String
        String result = modelMapper.asJSON(modelMap);
        // Writer writer = new FileWriter("/home/fno/testResult.json");
        // writer.write(result);

        // Assert the result equal to our desert
        String testOutput = "{\"alto-service:resource-id\":\"my-default-network-map\",\"alto-service:tag\":\"da65eca2eb7a10ce8b059740b0b2e3f8eb1d4785\",\"alto-service:map\":[{\"alto-service:pid\":\"PID1\",\"alto-service:endpoint-address-group\":[{\"alto-service:address-type\":\"ipv4\",\"alto-service:endpoint-prefix\":[\"192.0.2.0/24\",\"198.51.100.0/25\"]}]},{\"alto-service:pid\":\"PID2\",\"alto-service:endpoint-address-group\":[{\"alto-service:address-type\":\"ipv4\",\"alto-service:endpoint-prefix\":[\"198.51.100.128/25\"]}]},{\"alto-service:pid\":\"PID3\",\"alto-service:endpoint-address-group\":[{\"alto-service:address-type\":\"ipv4\",\"alto-service:endpoint-prefix\":[\"0.0.0.0/0\"]},{\"alto-service:address-type\":\"ipv6\",\"alto-service:endpoint-prefix\":[\"::/0\"]}]}]}";
        Assert.assertEquals(testOutput, result);
    }
}
