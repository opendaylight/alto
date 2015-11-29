/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import java.util.concurrent.ExecutionException;

import org.opendaylight.alto.basic.simpleird.SimpleIrdUtils;

import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;
import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rev151021.Information;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rev151021.InformationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rev151021.IrdInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.simple.ird.rev151021.IrdInstanceConfiguration;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoSimpleIrdProvider implements BindingAwareProvider, AutoCloseable {

    public static final String ROOT_INSTANCE = "root";
    public static final String SIMPLE_IRD_ROUTE_NAME = "simpleird";

    private static final Logger LOG = LoggerFactory.getLogger(AltoSimpleIrdProvider.class);

    private DataBroker m_dataBroker = null;

    InstanceIdentifier<IrdInstanceConfiguration> m_iid = null;
    InstanceIdentifier<IrdInstance> m_rootIID = null;

    private String m_context = null;
    private SimpleIrdListener m_listener = null;
    private AltoNorthboundRouter m_router = null;
    private AltoNorthboundRoute m_route = null;

    protected void createContext() throws InterruptedException, ExecutionException {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        m_context = ResourcepoolUtils.getUUID(SIMPLE_IRD_ROUTE_NAME);
        ResourcepoolUtils.createContext(m_context, wx);

        InformationBuilder builder = new InformationBuilder();
        builder.setContextId(new Uuid(m_context));

        InstanceIdentifier<Information> infoIID;
        infoIID = InstanceIdentifier.builder(Information.class).build();

        wx.put(LogicalDatastoreType.OPERATIONAL, infoIID, builder.build());
        wx.submit().get();

        LOG.info("Registered context {} for SimpleIrd", m_context);
    }

    protected void setupListener() {
        m_listener = new SimpleIrdListener(new Uuid(m_context));
        m_listener.register(m_dataBroker, m_iid);
    }

    protected void deleteContext() throws InterruptedException, ExecutionException {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();
        ResourcepoolUtils.deleteContext(m_context, wx);

        wx.submit().get();
    }

    protected void closeListener() throws Exception {
        if (m_listener != null) {
            m_listener.close();
        }
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoSimpleIrdProvider Session Initiated");

        m_dataBroker = session.getSALService(DataBroker.class);
        m_iid = SimpleIrdUtils.getInstanceConfigurationListIID();
        m_rootIID = SimpleIrdUtils.getInstanceIID(ROOT_INSTANCE);

        try {
            createContext();
            setupListener();
        } catch (Exception e) {
            LOG.error("Failed to create top-level containers");
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        try {
            if (m_router != null) {
                m_router.removeRoute(SIMPLE_IRD_ROUTE_NAME);
            }

            closeListener();
            deleteContext();
        } catch (Exception e) {
        }
        LOG.info("AltoSimpleIrdProvider Closed");
    }

    IrdInstance getInstance(ResourceId rid) {
        ReadTransaction rx = m_dataBroker.newReadOnlyTransaction();

        try {
            return SimpleIrdUtils.readInstance(rid, rx);
        } catch (Exception e) {
            return null;
        }
    }

    public void setupRoute(AltoNorthboundRouter router) {
        AltoNorthboundRoute route = new SimpleIrdRoute(this);
        String base_url = router.addRoute(SIMPLE_IRD_ROUTE_NAME, route);
        if (base_url == null) {
            LOG.error("Failed to register route for AltoSimpleIrd");
            return;
        }

        try {
            WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

            InstanceIdentifier<Information> infoIID;
            infoIID = InstanceIdentifier.builder(Information.class).build();
            InformationBuilder builder = new InformationBuilder();

            builder.setBaseUrl(base_url);
            wx.merge(LogicalDatastoreType.OPERATIONAL, infoIID, builder.build());

            wx.submit().get();

            m_router = router;
            m_route = route;
        } catch (Exception e) {
            LOG.error("Failed to reigster route");
        }
    }
}
