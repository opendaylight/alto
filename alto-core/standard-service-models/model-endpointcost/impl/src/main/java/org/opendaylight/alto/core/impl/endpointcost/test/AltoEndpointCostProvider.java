/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.impl.endpointcost.test;

import java.math.BigDecimal;
import java.util.concurrent.Future;
import java.util.LinkedList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.AddResourceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.AltoResourcepoolService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.ResourcePool;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.ServiceContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.desc.Capability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.desc.CapabilityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.Resource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.ResourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.ResourceKey;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.CostMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.CapabilityCategoryCosttype;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.ResourceTypeEndpointcost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.query.input.request.EndpointcostRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.query.output.response.EndpointcostResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.resource.pool.resource.capability.spec.CostTypeSpecBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.EndpointcostResponseTypeEndpointcostmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.FilterTypeEndpointfilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.TypedAddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.EndpointCostList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.EndpointCostListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.list.EndpointDestinationCost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.list.EndpointDestinationCostBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.list.endpoint.destination.cost.Cost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.filter.data.EndpointFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.EndpointCostMapBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.AltoModelBaseService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.QueryOutputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoEndpointCostProvider implements BindingAwareProvider, AutoCloseable, AltoModelBaseService {

    private static final Logger LOG = LoggerFactory.getLogger(AltoEndpointCostProvider.class);

    private DataBroker m_dataBrokerService = null;
    private RoutedRpcRegistration<AltoModelBaseService> m_serviceReg = null;
    private AltoResourcepoolService m_resourcepoolService = null;

    private static final ResourceId TEST_ENDPOINTCOST_RID = new ResourceId("test-model-endpointcost");
    private InstanceIdentifier<Resource> m_testIID = null;

    protected InstanceIdentifier<Resource> getResourceIID(ResourceId rid) {
        ResourceKey key = new ResourceKey(rid);
        return InstanceIdentifier.builder(ResourcePool.class).child(Resource.class, key).build();
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoModelEndpointCostProvider Session Initiated");

        m_dataBrokerService = session.getSALService(DataBroker.class);
        m_serviceReg = session.addRoutedRpcImplementation(AltoModelBaseService.class, this);

        CostTypeSpecBuilder ctsBuilder = new CostTypeSpecBuilder();
        ctsBuilder.setCostMetric(new CostMetric("routingcost"));
        ctsBuilder.setCostMode("ordinal");

        CapabilityBuilder capBuilder = new CapabilityBuilder();
        capBuilder.setCategory(CapabilityCategoryCosttype.class);
        capBuilder.setSpec(ctsBuilder.build());

        LinkedList<Capability> capabilityList = new LinkedList<Capability>();
        capabilityList.add(capBuilder.build());

        ResourceBuilder builder = new ResourceBuilder();
        builder.setResourceId(TEST_ENDPOINTCOST_RID).setType(ResourceTypeEndpointcost.class);
        builder.setCapability(capabilityList);

        AddResourceInputBuilder inputBuilder = new AddResourceInputBuilder();
        inputBuilder.fieldsFrom(builder.build());

        try {
            AltoResourcepoolService resourcepool;
            resourcepool = session.getRpcService(AltoResourcepoolService.class);

            RpcResult<Void> result;
            result = resourcepool.addResource(inputBuilder.build()).get();

            assert result.isSuccessful();

            m_testIID = getResourceIID(TEST_ENDPOINTCOST_RID);
            m_serviceReg.registerPath(ServiceContext.class, m_testIID);
        } catch (Exception e) {
        }
    }

    @Override
    public void close() throws Exception {
        LOG.info("AltoModelBaseProvider Closed");
    }

    protected Cost createNumericalCost(double cost) {
    org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.cost.NumericalBuilder builder;
        builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.cost.NumericalBuilder();

        builder.setValue(new BigDecimal(cost));
        return builder.build();
    }

    protected Cost createOrdinalCost(int order) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.cost.OrdinalBuilder builder;
        builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.cost.OrdinalBuilder();

        builder.setValue(order);
        return builder.build();
    }

    @Override
    public Future<RpcResult<QueryOutput>> query(QueryInput input) {
        if (!input.getType().equals(ResourceTypeEndpointcost.class)) {
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }
        EndpointcostRequest request = (EndpointcostRequest)input.getRequest();

        if (!request.getFilterType().equals(FilterTypeEndpointfilter.class)) {
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }
        if (!request.getCostMode().equals("ordinal")) {
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }
        CostMetric costMetric = request.getCostMetric();

        EndpointFilter filter = (EndpointFilter)request.getFilterData();
        List<? extends TypedAddressData> source = filter.getSource();
        List<? extends TypedAddressData> destination = filter.getDestination();

        int order = 0;
        LinkedList<EndpointCostList> eclList = new LinkedList<EndpointCostList>();
        for (TypedAddressData src: source) {
            LinkedList<EndpointDestinationCost> edcList = new LinkedList<EndpointDestinationCost>();
            for (TypedAddressData dst: destination) {
                EndpointDestinationCostBuilder edcBuilder = new EndpointDestinationCostBuilder();
                edcBuilder.fieldsFrom(dst);
                edcBuilder.setCost(createOrdinalCost(++order));

                edcList.add(edcBuilder.build());
            }

            EndpointCostListBuilder eclBuilder = new EndpointCostListBuilder();
            eclBuilder.fieldsFrom(src);
            eclBuilder.setEndpointDestinationCost(edcList);

            eclList.add(eclBuilder.build());
        }

        EndpointCostMapBuilder ecmBuilder = new EndpointCostMapBuilder();
        ecmBuilder.setEndpointCostList(eclList);

        EndpointcostResponseBuilder ecsBuilder = new EndpointcostResponseBuilder();
        ecsBuilder.setCostMetric(costMetric);
        ecsBuilder.setCostMode("ordinal");
        ecsBuilder.setType(EndpointcostResponseTypeEndpointcostmap.class);
        ecsBuilder.setData(ecmBuilder.build());

        QueryOutputBuilder builder = new QueryOutputBuilder();

        builder.setType(ResourceTypeEndpointcost.class).setResponse(ecsBuilder.build());
        return RpcResultBuilder.<QueryOutput>success(builder.build()).buildFuture();
    }

}
