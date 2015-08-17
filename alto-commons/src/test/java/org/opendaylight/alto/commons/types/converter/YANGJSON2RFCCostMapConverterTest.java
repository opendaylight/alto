package org.opendaylight.alto.commons.types.converter;

import org.opendaylight.alto.commons.types.model150404.ModelJSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class YANGJSON2RFCCostMapConverterTest {
    String costMapJson;
    YANGJSON2RFCCostMapConverter converter;
    ObjectMapper mapper;
    ModelJSONMapper model2Json;
    String yangModelString;

    @Before
    public void init() {
        yangModelString = "{\"key\":{\"resourceId\":{\"value\":\"my-default-network-map-routingcost-numerical\"}},\"map\":[{\"key\":{\"src\":{\"value\":\"PID3\"}},\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Map\",\"dstCosts\":[{\"dst\":{\"value\":\"PID2\"},\"cost\":\"15\"},{\"dst\":{\"value\":\"PID1\"},\"cost\":\"20\"}],\"src\":{\"value\":\"PID3\"}},{\"key\":{\"src\":{\"value\":\"PID2\"}},\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Map\",\"dstCosts\":[{\"dst\":{\"value\":\"PID2\"},\"cost\":\"1\"},{\"dst\":{\"value\":\"PID1\"},\"cost\":\"5\"},{\"dst\":{\"value\":\"PID3\"},\"cost\":\"15\"}],\"src\":{\"value\":\"PID2\"}},{\"key\":{\"src\":{\"value\":\"PID1\"}},\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Map\",\"dstCosts\":[{\"dst\":{\"value\":\"PID2\"},\"cost\":\"5\"},{\"dst\":{\"value\":\"PID1\"},\"cost\":\"1\"},{\"dst\":{\"value\":\"PID3\"},\"cost\":\"10\"}],\"src\":{\"value\":\"PID1\"}}],\"tag\":{\"value\":\"da65eca2eb7a10ce8b059740b0b2e3f8eb1d4786\"},\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.cost.maps.CostMap\",\"resourceId\":{\"value\":\"my-default-network-map-routingcost-numerical\"},\"meta\":{\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Meta\",\"costType\":{\"description\":null,\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.cost.map.meta.CostType\",\"costMetric\":{\"value\":\"routingcost\",\"enumeration\":null,\"string\":\"routingcost\"},\"costMode\":\"Numerical\"},\"dependentVtags\":[{\"key\":{\"resourceId\":{\"value\":\"my-default-network-map\"}},\"tag\":{\"value\":\"3ee2cb7e8d63d9fab71b9b34cbf764436315542e\"},\"implementedInterface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.dependent.vtags.DependentVtags\",\"resourceId\":{\"value\":\"my-default-network-map\"}}]}}";
        converter = new YANGJSON2RFCCostMapConverter();
        mapper = new ObjectMapper();
        model2Json = new ModelJSONMapper();
        costMapJson = "{\"meta\":{\"dependent-vtags\":[{\"resource-id\":\"my-default-network-map\",\"tag\":\"3ee2cb7e8d63d9fab71b9b34cbf764436315542e\"}],\"cost-type\":{\"cost-mode\":\"Numerical\",\"cost-metric\":\"routingcost\"}},\"cost-map\":{\"PID3\":{\"PID2\":\"15\",\"PID1\":\"20\"},\"PID2\":{\"PID2\":\"1\",\"PID1\":\"5\",\"PID3\":\"15\"},\"PID1\":{\"PID2\":\"5\",\"PID1\":\"1\",\"PID3\":\"10\"}}}";
    }

    @Test
    public void onYANGJSON2RFC() throws Exception {
        JsonNode node = mapper.readTree(yangModelString);
        RFC7285CostMap costMap = converter.convert(node);
        String resultJson = model2Json.asJSON(costMap);
        Assert.assertEquals(resultJson, this.costMapJson);
    }
}
