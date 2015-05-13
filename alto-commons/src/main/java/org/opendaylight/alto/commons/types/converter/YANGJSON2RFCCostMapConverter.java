package org.opendaylight.alto.commons.types.converter;

import org.opendaylight.alto.commons.helper.Converter;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostType;

import java.util.Map;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;

public class YANGJSON2RFCCostMapConverter extends Converter<JsonNode, RFC7285CostMap> {
    public YANGJSON2RFCCostMapConverter() {
    }

    public YANGJSON2RFCCostMapConverter(JsonNode _in) {
        super(_in);
    }

    protected YANGJSON2RFCAddressGroupConverter group_converter
                = new YANGJSON2RFCAddressGroupConverter();

    @Override
    protected Object _convert() {
        JsonNode node = this.in();
        RFC7285CostMap cm = new RFC7285CostMap();

        JsonNode meta = node.get("meta");
        for (JsonNode vtag: meta.get("dependentVtags")) {
            String resource_id = vtag.get("resourceId").get("value").asText();
            String tag = vtag.get("tag").get("value").asText();
            cm.meta.netmap_tags.add(new RFC7285VersionTag(resource_id, tag));
        }

        JsonNode cost_type = meta.get("costType");
        String mode = cost_type.get("costMode").asText();
        String metric = cost_type.get("costMetric").get("value").asText();
        cm.meta.costType = new RFC7285CostType(mode, metric);

        JsonNode map = node.get("map");
        assert !map.isArray();
        for (JsonNode cost_map: map) {
            String src_pid = cost_map.get("src").get("value").asText();
            Map<String, Object> data = new LinkedHashMap<String, Object>();

            for (JsonNode dst: cost_map.get("dstCosts")) {
                String dst_pid = dst.get("dst").get("value").asText();
                //TODO Don't know how to get data...

                data.put(dst_pid, new Integer(1));
            }

            cm.map.put(src_pid, data);
        }
        return cm;
    }
}
