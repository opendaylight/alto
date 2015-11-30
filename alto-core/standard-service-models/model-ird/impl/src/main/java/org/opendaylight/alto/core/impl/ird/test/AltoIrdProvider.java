/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.impl.ird.test;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoIrdProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AltoIrdProvider.class);

    private DataBroker m_dataBrokerService = null;

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoModelIrdProvider Session Initiated");
        m_dataBrokerService = session.getSALService(DataBroker.class);
    }

    @Override
    public void close() throws Exception {
        LOG.info("AltoModelBaseProvider Closed");
    }
}
