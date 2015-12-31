/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.opendaylight.alto.basic.resource.config.ResourceConfigUtils;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.AltoModelConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class AltoResourceConfigProvider implements BindingAwareProvider, AutoCloseable {
    public static final String RESOURCE_CONFIG_ROUTE_NAME = "config";
    public static final String RESOURCE_CONFIG_CONTEXT_NAME = "alto-resource-config";

    private static final Logger LOG = LoggerFactory.getLogger(AltoResourceConfigProvider.class);

    private DataBroker dataBroker = null;
    private ResourceConfigListener resourceConfigListener = null;
    private BindingAwareBroker.RpcRegistration<AltoModelConfigService> altoModelConfigService = null;

    private AltoNorthboundRouter m_router = null;
    private List<Uuid> m_contexts = null;

    protected void setupListener() {
        resourceConfigListener = new ResourceConfigListener();
        resourceConfigListener.register(dataBroker);
    }

    protected void closeListener() throws Exception {
        if (resourceConfigListener != null) {
            resourceConfigListener.close();
        }
    }

    protected void initializeConfigContext() throws Exception {
        m_contexts = new LinkedList<>();
        WriteTransaction wx = dataBroker.newWriteOnlyTransaction();
        m_contexts.add(ResourceConfigUtils.createContext(wx));
        wx.submit().get();
    }

    protected void clearConfigContext() throws Exception {
        WriteTransaction wx = dataBroker.newWriteOnlyTransaction();
        for (Uuid context : m_contexts) {
            ResourceConfigUtils.deleteContext(context, wx);
        }
        wx.submit().get();
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoResourceConfigProvider Session Initiated");

        dataBroker = session.getSALService(DataBroker.class);
        altoModelConfigService = session.addRpcImplementation(AltoModelConfigService.class,
                new AltoModelConfigImpl(dataBroker));
        try {
            setupListener();
            initializeConfigContext();
        } catch (Exception e) {
            LOG.error("Failed to create top-level containers");
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        try {
            if (m_router != null) {
                m_router.removeRoute(RESOURCE_CONFIG_ROUTE_NAME);
            }
            clearConfigContext();
            closeListener();
        } catch (Exception e) {
            LOG.error("Failed to remove route");
        }
        if (altoModelConfigService != null) {
            altoModelConfigService.close();
        }
        LOG.info("AltoResourceConfigProvider Closed");
    }

    public void setupRoute(AltoNorthboundRouter router) {
        AltoNorthboundRoute route = new ResourceConfigRoute(this);
        String base_url = router.addRoute(RESOURCE_CONFIG_ROUTE_NAME, route);
        if (base_url == null) {
            LOG.error("Failed to register route for AltoResourceConfig");
            return;
        }

        try {
            m_router = router;
        } catch (Exception e) {
            LOG.error("Failed to reigster route");
        }
    }
}
