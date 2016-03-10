/*
 * Copyright (c) 2015 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.spce.network.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.NetworkTrackerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkTrackerProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkTrackerProvider.class);
    private DataBroker dataBroker;
    private NetworkTrackerRpcHandler networkTrackerRpcHandler;
    private RpcRegistration<NetworkTrackerService> networkTrackerService;

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("NetworkTrackerProvider Session Initiated");
        dataBroker = session.getSALService(DataBroker.class);
        networkTrackerRpcHandler = new NetworkTrackerRpcHandler(dataBroker);
        networkTrackerService = session.addRpcImplementation(NetworkTrackerService.class, networkTrackerRpcHandler);
    }

    @Override
    public void close() throws Exception {
        LOG.info("NetworkTrackerProvider Closed");
    }

}
