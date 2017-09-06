/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.impl;

import java.util.concurrent.ExecutionException;
import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.AltoModelEndpointcostService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.ResourceTypeEndpointcost;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointcostserviceProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointcostserviceProvider.class);

    private final DataBroker m_dataBroker;
    private final BindingAwareBroker.RoutedRpcRegistration<AltoModelEndpointcostService> m_serviceReg;
    private ListenerRegistration<?> m_listener;

    private static final String SERVICE_ENDPOINTCOST_NAME = "service-endpointcost";
    private static final ResourceId SERVICE_ENDPOINTCOST_RID = new ResourceId(SERVICE_ENDPOINTCOST_NAME);
    private final InstanceIdentifier<Resource> m_testIID = ResourcepoolUtils.getResourceIID(ResourcepoolUtils.DEFAULT_CONTEXT,
            SERVICE_ENDPOINTCOST_NAME);

    public EndpointcostserviceProvider(DataBroker m_dataBroker,
            RoutedRpcRegistration<AltoModelEndpointcostService> m_serviceReg) {
        this.m_dataBroker = m_dataBroker;
        this.m_serviceReg = m_serviceReg;
    }

    protected void createContextTag()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        ResourcepoolUtils.createResourceWithCapabilities(ResourcepoolUtils.DEFAULT_CONTEXT,
                SERVICE_ENDPOINTCOST_NAME,
                ResourceTypeEndpointcost.class,
                null, wx);

        ResourcepoolUtils.lazyUpdateResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                SERVICE_ENDPOINTCOST_NAME, wx);

        wx.submit().get();
    }

    protected void removeContextTag()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException  {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        ResourcepoolUtils.deleteResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                SERVICE_ENDPOINTCOST_NAME, wx);

        wx.submit().get();
    }

    protected void setupListener() {
        ResourcepoolUtils.ContextTagListener listener = new ResourcepoolUtils.ContextTagListener(m_testIID, m_serviceReg);
        m_listener = m_dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL,
                m_testIID.child(ContextTag.class)), listener);

        assert m_listener != null;
    }

    public void init() {
        try {
            setupListener();
            createContextTag();
        } catch (Exception e) {
            LOG.error("Failed to initialize", e);
        }

        LOG.info("EndpointcostserviceProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {
        try {
            removeContextTag();
        } catch (Exception e) {
            LOG.debug("Error remocing context tag", e);
        }

        LOG.info("EndpointcostserviceProvider Closed");
    }
}
