/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.opendaylight.alto.basic.manual.maps.ManualMapsUtils;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;


import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.AltoModelConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.AltoModelCostmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AltoModelNetworkmapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class AltoManualMapsProvider {
    public static final String RESOURCE_CONFIG_ROUTE_NAME = "config";
    public static final String RESOURCE_CONFIG_CONTEXT_NAME = "alto-manual-maps";

    private static final Logger LOG = LoggerFactory.getLogger(AltoManualMapsProvider.class);

    private DataBroker dataBroker = null;
    private ManualMapsListener manualMapsListener = null;
    private BindingAwareBroker.RpcRegistration<AltoModelConfigService> altoModelConfigService = null;
    private BindingAwareBroker.RoutedRpcRegistration<AltoModelNetworkmapService> altoModelNetworkmapService = null;
    private BindingAwareBroker.RoutedRpcRegistration<AltoModelCostmapService> altoModelCostmapService = null;

    private RpcProviderRegistry rpcProviderRegistry = null;

    private AltoNorthboundRouter altoNorthboundRouter = null;
    private List<Uuid> m_contexts = null;

    private AltoModelNetworkmapService m_AltoModelNetworkmapService = null;

    public void setRpcProviderRegistry(RpcProviderRegistry rpr) {
        this.rpcProviderRegistry = rpr;
    }

    public void setDataBroker(DataBroker db) {
        this.dataBroker = db;
    }

    public void setAltoNorthboundRouter(AltoNorthboundRouter anr) {
        this.altoNorthboundRouter = anr;
    }

    public void setAltoModelNetworkmapService(AltoManualNetworkmapServiceImpl ansi) {
        this.m_AltoModelNetworkmapService = ansi;
    }
    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        this.rpcProviderRegistry.addRpcImplementation(AltoModelConfigService.class,
                new AltoModelConfigImpl(dataBroker));
        altoModelNetworkmapService = this.rpcProviderRegistry.addRoutedRpcImplementation(AltoModelNetworkmapService.class,
                new AltoManualNetworkmapServiceImpl(dataBroker));
        this.rpcProviderRegistry.addRoutedRpcImplementation(AltoModelCostmapService.class,
                new AltoManualCostmapServiceImpl(dataBroker));
        try {
            setupListener();
            initializeConfigContext();
        } catch (Exception e) {
            LOG.error("Failed to create top-level containers");
            e.printStackTrace();
        }
        LOG.info("AltoManualMapsProvider Session Initiated");
    }

    protected void setupListener() {
        manualMapsListener = new ManualMapsListener();
        manualMapsListener.register(dataBroker);
        manualMapsListener.setNetworkmapServiceReg(altoModelNetworkmapService);
        manualMapsListener.setCostmapServiceReg(altoModelCostmapService);
    }

    protected void closeListener() throws Exception {
        if (manualMapsListener != null) {
            manualMapsListener.close();
        }
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

    public void close() throws Exception {
        try {
            if (altoNorthboundRouter != null) {
                altoNorthboundRouter.removeRoute(RESOURCE_CONFIG_ROUTE_NAME);
            }
            clearConfigContext();
            closeListener();
        } catch (Exception e) {
            LOG.error("Failed to remove route");
        }
        if (altoModelConfigService != null) {
            altoModelConfigService.close();
        }
        if (altoModelNetworkmapService != null) {
            altoModelNetworkmapService.close();
        }
        if (altoModelCostmapService != null) {
            altoModelCostmapService.close();
        }
        LOG.info("AltoManualMapsProvider Closed");
    }

    public void setupRoute(AltoNorthboundRouter router) {
        AltoNorthboundRoute route = new ManualMapsRoute(this);
        String base_url = router.addRoute(RESOURCE_CONFIG_ROUTE_NAME, route);
        if (base_url == null) {
            LOG.error("Failed to register route for AltoManualMaps");
            return;
        }

        try {
            altoNorthboundRouter = router;
        } catch (Exception e) {
            LOG.error("Failed to reigster route");
        }
    }
}
