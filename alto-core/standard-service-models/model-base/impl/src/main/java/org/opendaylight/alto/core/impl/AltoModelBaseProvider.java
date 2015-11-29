/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;

import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;

import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;
import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils.ContextTagListener;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.context.Resource;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.AltoModelBaseService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.ResourceTypeError;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.alto.response.error.response.ErrorResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.alto.response.error.response.error.response.ErrorBuilder;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AltoModelBaseProvider implements BindingAwareProvider, AutoCloseable, AltoModelBaseService {

    private static final Logger LOG = LoggerFactory.getLogger(AltoModelBaseProvider.class);

    private DataBroker m_dataBroker = null;
    private RoutedRpcRegistration<AltoModelBaseService> m_serviceReg = null;
    private ListenerRegistration<DataChangeListener> m_listener = null;

    private static final String TEST_BASE_NAME = "test-model-base";
    private static final ResourceId TEST_BASE_RID = new ResourceId(TEST_BASE_NAME);
    private InstanceIdentifier<Resource> m_testIID = null;

    protected void createContextTag()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException  {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();
        ResourcepoolUtils.createResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                                            TEST_BASE_NAME, ResourceTypeError.class, wx);

        ResourcepoolUtils.lazyUpdateResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                                            TEST_BASE_NAME, wx);

        wx.submit().get();
    }

    protected void removeContextTag()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException  {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        ResourcepoolUtils.deleteResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                                            TEST_BASE_NAME, wx);

        wx.submit().get();
    }

    protected void setupListener() {
        ContextTagListener listener = new ContextTagListener(m_testIID, m_serviceReg);
        m_listener = m_dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                                        m_testIID,listener, DataChangeScope.SUBTREE);

        assert m_listener != null;
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoModelBaseProvider Session Initiated");

        m_dataBroker = session.getSALService(DataBroker.class);
        m_testIID = ResourcepoolUtils.getResourceIID(ResourcepoolUtils.DEFAULT_CONTEXT, TEST_BASE_NAME);
        m_serviceReg = session.addRoutedRpcImplementation(AltoModelBaseService.class, this);

        try {
            setupListener();
            createContextTag();
        } catch (Exception e) {
        }
    }

    @Override
    public void close() throws Exception {
        LOG.info("AltoModelBaseProvider Closed");

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
        ErrorBuilder errorBuilder = new ErrorBuilder();
        errorBuilder.setErrorCode("E_TEST");

        ErrorResponseBuilder erBuilder = new ErrorResponseBuilder();
        erBuilder.setError(errorBuilder.build());

        QueryOutputBuilder builder = new QueryOutputBuilder();
        builder.setType(ResourceTypeError.class);
        builder.setResponse(erBuilder.build());
        return RpcResultBuilder.<QueryOutput>success(builder.build()).buildFuture();
    }

}
