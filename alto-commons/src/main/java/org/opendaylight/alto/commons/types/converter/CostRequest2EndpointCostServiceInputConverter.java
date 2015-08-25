/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.converter;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.alto.commons.helper.Converter;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostType;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.CostRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.EndpointCostServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.EndpointCostServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.input.CostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.input.CostTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.input.Endpoints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.input.EndpointsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMetricBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TypedEndpointAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TypedEndpointAddressBuilder;

public class CostRequest2EndpointCostServiceInputConverter extends
    Converter<CostRequest, EndpointCostServiceInput> {

    public CostRequest2EndpointCostServiceInputConverter() {
    }

    public CostRequest2EndpointCostServiceInputConverter(CostRequest _in) {
        super(_in);
    }

    @Override
    protected Object _convert() {
        RFC7285CostType rfcCostType = in().costType;

        CostType costType = new CostTypeBuilder()
            .setCostMetric(CostMetricBuilder.getDefaultInstance(rfcCostType.metric))
            .setCostMode(CostMode.valueOf(capitalizeFirstLetter(rfcCostType.mode)))
            .setDescription(rfcCostType.description).build();

        List<TypedEndpointAddress> srcs = toTypedEndpointAddressList(in().endpoints.src);
        List<TypedEndpointAddress> dsts = toTypedEndpointAddressList(in().endpoints.dst);
        Endpoints endpoints = new EndpointsBuilder().setSrcs(srcs).setDsts(dsts).build();

        EndpointCostServiceInput input = new EndpointCostServiceInputBuilder()
            .setCostType(costType)
            .setEndpoints(endpoints)
            .build();

        return input;
    }

    private List<TypedEndpointAddress> toTypedEndpointAddressList(List<String> addresses) {
        List<TypedEndpointAddress> result = new ArrayList<TypedEndpointAddress>();
        for (String address : addresses) {
            result.add(TypedEndpointAddressBuilder.getDefaultInstance(address));
        }
        return result;
    }

    private String capitalizeFirstLetter(String str) {
        str = str.toLowerCase();
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
