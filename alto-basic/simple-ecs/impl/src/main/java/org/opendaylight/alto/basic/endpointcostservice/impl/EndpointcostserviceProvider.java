/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.impl;

import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.AltoModelEndpointcostService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.ResourceTypeEndpointcost;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class EndpointcostserviceProvider implements BindingAwareProvider, AutoCloseable,AltoModelEndpointcostService {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointcostserviceProvider.class);

    private DataBroker m_dataBroker = null;
    private BindingAwareBroker.RoutedRpcRegistration<AltoModelEndpointcostService> m_serviceReg = null;
    private ListenerRegistration<DataChangeListener> m_listener = null;

    private static final String SERVICE_ENDPOINTCOST_NAME = "service-endpointcost";
    private static final ResourceId SERVICE_ENDPOINTCOST_RID = new ResourceId(SERVICE_ENDPOINTCOST_NAME);
    private InstanceIdentifier<Resource> m_testIID = null;

    private BasicECSImplementation basicEcsImpl;

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
        m_listener = m_dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                m_testIID,listener, AsyncDataBroker.DataChangeScope.SUBTREE);

        assert m_listener != null;
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("EndpointcostserviceProvider Session Initiated");

        m_dataBroker = session.getSALService(DataBroker.class);
        m_testIID = ResourcepoolUtils.getResourceIID(ResourcepoolUtils.DEFAULT_CONTEXT,
                SERVICE_ENDPOINTCOST_NAME);
        m_serviceReg = session.addRoutedRpcImplementation(AltoModelEndpointcostService.class, this);
        basicEcsImpl = new BasicECSImplementation(m_dataBroker);
        try {
            setupListener();
            createContextTag();
        } catch (Exception e) {
        }
    }

    @Override
    public void close() throws Exception {
        LOG.info("EndpointcostserviceProvider Closed");

        if (m_serviceReg != null) {
            m_serviceReg.close();
        }
        try {
            removeContextTag();
        } catch (Exception e) {
        }
    }

    @Override
    public Future<RpcResult<QueryOutput>> query(QueryInput input) {
        return basicEcsImpl.getECS(input);
    }

}
