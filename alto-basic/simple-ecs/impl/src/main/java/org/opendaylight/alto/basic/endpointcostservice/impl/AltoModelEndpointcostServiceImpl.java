/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.impl;

import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.AltoModelEndpointcostService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class AltoModelEndpointcostServiceImpl implements AltoModelEndpointcostService {

    private final BasicECSImplementation basicEcsImpl;

    public AltoModelEndpointcostServiceImpl(DataBroker dataBroker) {
        basicEcsImpl = new BasicECSImplementation(dataBroker);
    }


    @Override
    public Future<RpcResult<QueryOutput>> query(QueryInput input) {
        return basicEcsImpl.getECS(input);
    }
}
