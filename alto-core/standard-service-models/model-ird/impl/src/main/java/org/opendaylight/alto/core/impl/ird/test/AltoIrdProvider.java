/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.impl.ird.test;

import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.AddResourceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.AltoResourcepoolService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.ResourcePool;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.ServiceContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.Resource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.ResourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.ResourceKey;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.ird.rev151021.ResourceTypeIrd;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.AltoModelBaseService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryOutputBuilder;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoIrdProvider implements BindingAwareProvider, AutoCloseable, AltoModelBaseService {

    private static final Logger LOG = LoggerFactory.getLogger(AltoIrdProvider.class);

    private DataBroker m_dataBrokerService = null;
    private RoutedRpcRegistration<AltoModelBaseService> m_serviceReg = null;
    private AltoResourcepoolService m_resourcepoolService = null;

    private static final ResourceId TEST_IRD_RID = new ResourceId("test-model-ird");
    private InstanceIdentifier<Resource> m_testIID = null;

    protected InstanceIdentifier<Resource> getResourceIID(ResourceId rid) {
        ResourceKey key = new ResourceKey(rid);
        return InstanceIdentifier.builder(ResourcePool.class).child(Resource.class, key).build();
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoModelIrdProvider Session Initiated");

        m_dataBrokerService = session.getSALService(DataBroker.class);
        m_serviceReg = session.addRoutedRpcImplementation(AltoModelBaseService.class, this);

        ResourceBuilder builder = new ResourceBuilder();
        builder.setResourceId(TEST_IRD_RID).setType(ResourceTypeIrd.class);

        AddResourceInputBuilder inputBuilder = new AddResourceInputBuilder();
        inputBuilder.fieldsFrom(builder.build());

        try {
            AltoResourcepoolService resourcepool;
            resourcepool = session.getRpcService(AltoResourcepoolService.class);

            RpcResult<Void> result;
            result = resourcepool.addResource(inputBuilder.build()).get();

            assert result.isSuccessful();

            m_testIID = getResourceIID(TEST_IRD_RID);
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
        if (!input.getType().equals(ResourceTypeIrd.class)) {
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }
        QueryOutputBuilder builder = new QueryOutputBuilder();

        builder.setType(ResourceTypeIrd.class);
        return RpcResultBuilder.<QueryOutput>success(builder.build()).buildFuture();
    }

}
