/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.converter;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.alto.commons.helper.Converter;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostType;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.CostResponse;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.costdefault.rev150507.DstCosts1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.EndpointCostServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.EndpointCostService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.EndpointCostMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.endpoint.cost.map.DstCosts;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.meta.CostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TypedEndpointAddress;

public class EndpointCostServiceOutput2CostResponseConverter extends
    Converter<EndpointCostServiceOutput, CostResponse> {

    private static final String NUMERICAL_MODE = "numerical";
    private static final String ORDINAL_MODE = "ordinal";
    private static final String ROUTING_COST_METRIC = "routingcost";
    private static final String HOP_COUNT_METRIC = "hopcount";

    public EndpointCostServiceOutput2CostResponseConverter() {
    }

    public EndpointCostServiceOutput2CostResponseConverter(EndpointCostServiceOutput _in) {
        super(_in);
    }

    @Override
    protected Object _convert() {
        CostResponse resp = new CostResponse();
        EndpointCostService ecService = this.in().getEndpointCostService();
        CostType costType = ecService.getMeta().getCostType();

        resp.meta = new CostResponse.Meta();
        resp.meta.costType = new RFC7285CostType();
        resp.meta.costType.mode = costType.getCostMode().name().toLowerCase();
        resp.meta.costType.metric = String.valueOf(costType.getCostMetric().getValue()).toLowerCase();
        resp.meta.costType.description = costType.getDescription();

        resp.answer = new HashMap<String, Map<String, Object>>();
        for (EndpointCostMap ecm : ecService.getEndpointCostMap()) {
            TypedEndpointAddress src = ecm.getSrc();
            Map<String, Object> map = new HashMap<String, Object>();
            for (DstCosts dstCosts : ecm.getDstCosts()) {
                String dst = String.valueOf(dstCosts.getDst().getValue());
                String cost = dstCosts.getAugmentation(DstCosts1.class).getCostDefault().toString();
                map.put(dst, cost(resp.meta.costType.mode, resp.meta.costType.metric, cost));
            }
            resp.answer.put(String.valueOf(src.getValue()), map);
        }
        return resp;
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
