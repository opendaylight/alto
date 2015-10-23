/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Future;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.AddResourceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.ResourcePool;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.ServiceContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.Resource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.ResourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.ResourceKey;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.AltoModelBaseService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.ResourceTypeError;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.query.output.response.ErrorResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.alto.response.error.ErrorBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.AltoResourcepoolService;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AltoModelBaseProvider implements BindingAwareProvider, AutoCloseable, AltoModelBaseService {

    private static final Logger LOG = LoggerFactory.getLogger(AltoModelBaseProvider.class);

    private DataBroker m_dataBrokerService = null;
    private RoutedRpcRegistration<AltoModelBaseService> m_serviceReg = null;
    private AltoResourcepoolService m_resourcepoolService = null;

    private static final ResourceId TEST_BASE_RID = new ResourceId("test-model-base");
    private InstanceIdentifier<Resource> m_testIID = null;

    protected InstanceIdentifier<Resource> getResourceIID(ResourceId rid) {
        ResourceKey key = new ResourceKey(rid);
        return InstanceIdentifier.builder(ResourcePool.class).child(Resource.class, key).build();
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoModelBaseProvider Session Initiated");

        m_dataBrokerService = session.getSALService(DataBroker.class);
        m_serviceReg = session.addRoutedRpcImplementation(AltoModelBaseService.class, this);

        ResourceBuilder builder = new ResourceBuilder();
        builder.setResourceId(TEST_BASE_RID).setType(ResourceTypeError.class);

        AddResourceInputBuilder inputBuilder = new AddResourceInputBuilder();
        inputBuilder.fieldsFrom(builder.build());

        try {
            AltoResourcepoolService resourcepool;
            resourcepool = session.getRpcService(AltoResourcepoolService.class);

            RpcResult<Void> result;
            result = resourcepool.addResource(inputBuilder.build()).get();

            assert result.isSuccessful();

            m_testIID = getResourceIID(TEST_BASE_RID);
            m_serviceReg.registerPath(ServiceContext.class, m_testIID);
        } catch (Exception e) {
        }
    }

    @Override
    public void close() throws Exception {
        LOG.info("AltoModelBaseProvider Closed");
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
