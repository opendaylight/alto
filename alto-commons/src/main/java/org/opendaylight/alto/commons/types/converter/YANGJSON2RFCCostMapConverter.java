/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.converter;

import org.opendaylight.alto.commons.helper.Converter;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostType;

import java.util.Map;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.databind.JsonNode;

public class YANGJSON2RFCCostMapConverter extends Converter<JsonNode, RFC7285CostMap> {
    private static final String NUMERICAL_MODE = "numerical";
    private static final String ORDINAL_MODE = "ordinal";
    private static final String ROUTING_COST_METRIC = "routingcost";
    private static final String HOP_COUNT_METRIC = "hopcount";

    public YANGJSON2RFCCostMapConverter() {
    }

    public YANGJSON2RFCCostMapConverter(JsonNode _in) {
        super(_in);
    }

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
        String mode = cost_type.get("costMode").asText().toLowerCase();
        String metric = cost_type.get("costMetric").get("value").asText().toLowerCase();
        cm.meta.costType = new RFC7285CostType(mode, metric);

        JsonNode map = node.get("map");
        assert map.isArray();
        for (JsonNode cost_map: map) {
            String src_pid = cost_map.get("src").get("value").asText();
            Map<String, Object> data = new LinkedHashMap<String, Object>();

            for (JsonNode dst: cost_map.get("dstCosts")) {
                String dst_pid = dst.get("dst").get("value").asText();

                JsonNode cost_node = dst.get("cost");
                if ((cost_node != null) && (!cost_node.isNull())) {
                    Object cost = cost(mode, metric, cost_node.asText());
                    data.put(dst_pid, cost);
                }
            }

            cm.map.put(src_pid, data);
        }
        return cm;
    }

    private Object cost(String mode, String metric, String cost) {
        if (ORDINAL_MODE.equals(mode.toLowerCase())) {
            return Integer.parseInt(cost);
        }

        if (NUMERICAL_MODE.equals(mode.toLowerCase())) {
            if (HOP_COUNT_METRIC.equals(metric.toLowerCase())) {
                return Integer.parseInt(cost);
            } else if (ROUTING_COST_METRIC.equals(metric.toLowerCase())) {
                return Double.parseDouble(cost);
            }
        }
        return cost;
    }
}
