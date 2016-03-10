/*
 * Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev151106.AltoSpceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.NetworkTrackerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoSpceProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AltoSpceProvider.class);
    private RpcRegistration<AltoSpceService> altoSpceService;

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoSpceProvider Session Initiated!");
        SalFlowService salFlowService = session.getRpcService(SalFlowService.class);
        NetworkTrackerService networkTrackerService = session.getRpcService(NetworkTrackerService.class);
        DataBroker dataBroker = session.getSALService(DataBroker.class);
        altoSpceService = session.addRpcImplementation(AltoSpceService.class, new AltoSpceImpl(salFlowService, networkTrackerService, dataBroker));
    }

    @Override
    public void close() throws Exception {
        LOG.info("AltoSpceProvider Closed!");
        if (altoSpceService != null) {
            altoSpceService.close();
        }
    }
}
