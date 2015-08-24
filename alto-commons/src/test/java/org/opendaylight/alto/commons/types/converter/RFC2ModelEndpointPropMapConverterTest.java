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
import org.opendaylight.alto.commons.types.model150404.ModelEndpointPropertyMap;
import org.opendaylight.alto.commons.types.model150404.ModelJSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285EndpointPropertyMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RFC2ModelEndpointPropMapConverterTest {
    String endpointPropMapJson;
    RFC2ModelEndpointPropMapConverter converter;
    ObjectMapper mapper;
    ModelJSONMapper model2Json;
    String yangModelString;

    @Before
    public void init(){
        endpointPropMapJson = "{\"meta\":{\"dependent-vtags\":[{\"resource-id\":\"my-default-network-map\",\"tag\":\"7915dc0290c2705481c491a2b4ffbec482b3cf62\"}]},\"endpoint-properties\":{\"ipv4:192.0.2.34\":{\"my-default-network-map.pid\":\"PID1\",\"priv:ietf-example-prop\":\"1\"},\"ipv4:203.0.113.129\":{\"my-default-network-map.pid\":\"PID3\"}}}";
        converter = new RFC2ModelEndpointPropMapConverter();
        mapper = new ObjectMapper();
        model2Json = new ModelJSONMapper();
        yangModelString = "{\"alto-service:meta\":{\"alto-service:dependent-vtags\":[{\"alto-service:resource-id\":\"my-default-network-map\",\"alto-service:tag\":\"7915dc0290c2705481c491a2b4ffbec482b3cf62\"}]},\"alto-service:endpoint-properties\":[{\"alto-service:endpoint\":\"ipv4:192.0.2.34\",\"alto-service:properties\":[{\"alto-service:property-type\":\"my-default-network-map.pid\",\"alto-service:property\":\"PID1\"},{\"alto-service:property-type\":\"priv:ietf-example-prop\",\"alto-service:property\":\"1\"}]},{\"alto-service:endpoint\":\"ipv4:203.0.113.129\",\"alto-service:properties\":[{\"alto-service:property-type\":\"my-default-network-map.pid\",\"alto-service:property\":\"PID3\"}]}]}";
    }

    @Test
    public void onRFC2Model() throws Exception{
        RFC7285EndpointPropertyMap endpointPropertyMap = mapper.readValue(this.endpointPropMapJson, RFC7285EndpointPropertyMap.class);
        ModelEndpointPropertyMap model = converter.convert(endpointPropertyMap);
        String resultJson = model2Json.asJSON(model);
        Assert.assertEquals(resultJson, this.yangModelString);
    }
}
