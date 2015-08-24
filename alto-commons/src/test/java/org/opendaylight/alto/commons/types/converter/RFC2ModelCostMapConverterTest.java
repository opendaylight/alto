/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.converter;

import org.opendaylight.alto.commons.types.model150404.ModelCostMap;
import org.opendaylight.alto.commons.types.model150404.ModelJSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RFC2ModelCostMapConverterTest {
    String costMapJson;
    RFC2ModelCostMapConverter converter;
    ObjectMapper mapper;
    ModelJSONMapper model2Json;
    String yangModelString;

    @Before
    public void init() {
        costMapJson = "{\"meta\":{\"dependent-vtags\":[{\"resource-id\":\"my-default-network-map\",\"tag\":\"3ee2cb7e8d63d9fab71b9b34cbf764436315542e\"}],\"cost-type\":{\"cost-mode\":\"numerical\",\"cost-metric\":\"routingcost\"}},\"cost-map\":{\"PID1\":{\"PID1\":1,\"PID2\":5,\"PID3\":10},\"PID2\":{\"PID1\":5,\"PID2\":1,\"PID3\":15},\"PID3\":{\"PID1\":20,\"PID2\":15}}}";
        converter = new RFC2ModelCostMapConverter();
        mapper = new ObjectMapper();
        model2Json = new ModelJSONMapper();
        yangModelString = "{\"alto-service:resource-id\":\"my-default-network-map-routingcost-numerical\",\"alto-service:tag\":\"da65eca2eb7a10ce8b059740b0b2e3f8eb1d4786\",\"alto-service:meta\":{\"alto-service:dependent-vtags\":[{\"alto-service:resource-id\":\"my-default-network-map\",\"alto-service:tag\":\"3ee2cb7e8d63d9fab71b9b34cbf764436315542e\"}],\"alto-service:cost-type\":{\"alto-service:cost-mode\":\"numerical\",\"alto-service:cost-metric\":\"routingcost\"}},\"alto-service:map\":[{\"alto-service:src\":\"PID1\",\"alto-service:dst-costs\":[{\"alto-service:dst\":\"PID1\",\"alto-cost-default:cost-default\":\"1\"},{\"alto-service:dst\":\"PID2\",\"alto-cost-default:cost-default\":\"5\"},{\"alto-service:dst\":\"PID3\",\"alto-cost-default:cost-default\":\"10\"}]},{\"alto-service:src\":\"PID2\",\"alto-service:dst-costs\":[{\"alto-service:dst\":\"PID1\",\"alto-cost-default:cost-default\":\"5\"},{\"alto-service:dst\":\"PID2\",\"alto-cost-default:cost-default\":\"1\"},{\"alto-service:dst\":\"PID3\",\"alto-cost-default:cost-default\":\"15\"}]},{\"alto-service:src\":\"PID3\",\"alto-service:dst-costs\":[{\"alto-service:dst\":\"PID1\",\"alto-cost-default:cost-default\":\"20\"},{\"alto-service:dst\":\"PID2\",\"alto-cost-default:cost-default\":\"15\"}]}]}";
    }

    @Test
    public void onRFC2Model() throws Exception {
        RFC7285CostMap costMap = mapper.readValue(costMapJson,RFC7285CostMap.class);
        ModelCostMap model = converter.convert(costMap);
        String resultJson = model2Json.asJSON(model);
        Assert.assertEquals(resultJson, this.yangModelString);
    }
}
