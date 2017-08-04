/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import java.util.LinkedList;
import java.util.List;
import org.opendaylight.alto.basic.manual.maps.ManualMapsUtils;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.AltoModelCostmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AltoModelNetworkmapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoManualMapsProvider implements AutoCloseable {
    public static final String RESOURCE_CONFIG_ROUTE_NAME = "config";
    public static final String RESOURCE_CONFIG_CONTEXT_NAME = "alto-manual-maps";

    private static final Logger LOG = LoggerFactory.getLogger(AltoManualMapsProvider.class);

    private final DataBroker dataBroker;
    private final AltoNorthboundRouter router;
    private final ManualMapsListener manualMapsListener = new ManualMapsListener();
    private final BindingAwareBroker.RoutedRpcRegistration<AltoModelNetworkmapService> altoModelNetworkmapService;
    private final BindingAwareBroker.RoutedRpcRegistration<AltoModelCostmapService> altoModelCostmapService;

    private AltoNorthboundRouter m_router = null;
    private List<Uuid> m_contexts = null;

    public AltoManualMapsProvider(DataBroker dataBroker,
            AltoNorthboundRouter router,
            RoutedRpcRegistration<AltoModelNetworkmapService> altoModelNetworkmapService,
            RoutedRpcRegistration<AltoModelCostmapService> altoModelCostmapService) {
        this.dataBroker = dataBroker;
        this.router = router;
        this.altoModelNetworkmapService = altoModelNetworkmapService;
        this.altoModelCostmapService = altoModelCostmapService;
    }

    protected void setupListener() {
        manualMapsListener.register(dataBroker);
        manualMapsListener.setNetworkmapServiceReg(altoModelNetworkmapService);
        manualMapsListener.setCostmapServiceReg(altoModelCostmapService);
    }

    protected void closeListener() throws Exception {
        manualMapsListener.close();
    }

    protected void initializeConfigContext() throws Exception {
        m_contexts = new LinkedList<>();
        WriteTransaction wx = dataBroker.newWriteOnlyTransaction();
        m_contexts.add(ManualMapsUtils.createContext(wx));
        wx.submit().get();
    }

    protected void clearConfigContext() throws Exception {
        WriteTransaction wx = dataBroker.newWriteOnlyTransaction();
        for (Uuid context : m_contexts) {
            ManualMapsUtils.deleteContext(context, wx);
        }
        wx.submit().get();
    }

    public void init() {
        try {
            setupListener();
            initializeConfigContext();
        } catch (Exception e) {
            LOG.error("Failed to create top-level containers", e);
        }

        setupRoute();

        LOG.info("AltoManualMapsProvider Session Initiated");
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

        LOG.info("AltoManualMapsProvider Closed");
    }

    private void setupRoute() {
        AltoNorthboundRoute route = new ManualMapsRoute(this);
        String base_url = router.addRoute(RESOURCE_CONFIG_ROUTE_NAME, route);
        if (base_url == null) {
            LOG.error("Failed to register route for AltoManualMaps");
            return;
        }

        try {
            m_router = router;
        } catch (Exception e) {
            LOG.error("Failed to reigster route");
        }
    }
}
