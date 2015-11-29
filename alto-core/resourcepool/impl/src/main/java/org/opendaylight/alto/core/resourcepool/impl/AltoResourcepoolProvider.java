/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.resourcepool.impl;

import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;

import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;

import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoResourcepoolProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AltoResourcepoolProvider.class);

    private DataBroker m_dataBroker = null;

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoResourcesProvider Session Initiated");

        m_dataBroker = session.getSALService(DataBroker.class);
        assert m_dataBroker != null;

        try {
            createDefaultContext();
        } catch (Exception e) {
            LOG.error("Failed to create top-level containers for ALTO services");
        }
    }

    protected void createDefaultContext()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException  {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        ResourcepoolUtils.createContext(ResourcepoolUtils.DEFAULT_CONTEXT, wx);

        wx.submit().get();
    }

    protected void deleteDefaultContext() throws Exception {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        ResourcepoolUtils.deleteContext(ResourcepoolUtils.DEFAULT_CONTEXT, wx);

        wx.submit().get();
    }

    @Override
    public void close() throws Exception {
        try {
            deleteDefaultContext();
        } catch (Exception e) {
            /* Exit anyway */
        }
        LOG.info("AltoResourcesProvider Closed");
    }
}
