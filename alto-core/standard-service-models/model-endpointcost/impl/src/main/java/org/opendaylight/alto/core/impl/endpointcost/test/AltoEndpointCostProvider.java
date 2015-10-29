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

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.ResourceTypeEndpointcost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.cost.type.data.CostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.cost.type.data.CostTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.endpointcost.request.data.EndpointcostRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.endpointcost.response.data.EndpointcostResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.query.input.request.EndpointcostRequestData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.query.output.response.EndpointcostResponseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.verify.resource.input.capability.spec.CostTypeSpecBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.Ipv4AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.Ipv6AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.TypedAddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpoint.filter.data.EndpointFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.EndpointCostMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.EndpointCostList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.EndpointCostListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.list.EndpointDestinationCost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.list.EndpointDestinationCostBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.Cost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.data.endpointcost.request.filter.EndpointFilterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.EndpointCostMapDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.typed.address.data.Address;

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
    private static final CostMetric COST_METRIC_ROUTINGCOST = new CostMetric("routingcost");
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

        CostTypeBuilder ctBuilder = new CostTypeBuilder();
        ctBuilder.setCostMetric(COST_METRIC_ROUTINGCOST);
        ctBuilder.setCostMode("ordinal");

        CostTypeSpecBuilder ctsBuilder = new CostTypeSpecBuilder();
        ctsBuilder.setCostType(ctBuilder.build());

        CapabilityBuilder capBuilder = new CapabilityBuilder();
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
        org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.endpoint.cost.map.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.cost.NumericalBuilder builder;
        builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.endpoint.cost.map.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.cost.NumericalBuilder();

        builder.setCost(new BigDecimal(cost));
        return builder.build();
    }

    protected Cost createOrdinalCost(int order) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.endpoint.cost.map.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.cost.OrdinalBuilder builder;
        builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.endpoint.cost.map.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.cost.OrdinalBuilder();

        builder.setCost(order);
        return builder.build();
    }

    protected Address createSourceAddress(Address from) {
        if (from instanceof Ipv4AddressData) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.endpoint.cost.map.data.endpoint.cost.map.endpoint.cost.list.address.Ipv4Builder builder;
            builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.endpoint.cost.map.data.endpoint.cost.map.endpoint.cost.list.address.Ipv4Builder();

            builder.fieldsFrom((Ipv4AddressData)from);
            return builder.build();
        } else if (from instanceof Ipv6AddressData) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.endpoint.cost.map.data.endpoint.cost.map.endpoint.cost.list.address.Ipv6Builder builder;
            builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.endpoint.cost.map.data.endpoint.cost.map.endpoint.cost.list.address.Ipv6Builder();

            builder.fieldsFrom((Ipv6AddressData)from);
            return builder.build();
}
        return null;
    }

    protected Address createDestinationAddress(Address from) {
        if (from instanceof Ipv4AddressData) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.endpoint.cost.map.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.address.Ipv4Builder builder;
            builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.endpoint.cost.map.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.address.Ipv4Builder();

            builder.fieldsFrom((Ipv4AddressData)from);
            return builder.build();
        } else if (from instanceof Ipv6AddressData) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.endpoint.cost.map.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.address.Ipv6Builder builder;
            builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.data.endpointcost.response.data.endpoint.cost.map.data.endpoint.cost.map.endpoint.cost.list.endpoint.destination.cost.address.Ipv6Builder();

            builder.fieldsFrom((Ipv6AddressData)from);
            return builder.build();
        }
        return null;
    }

    @Override
    public Future<RpcResult<QueryOutput>> query(QueryInput input) {
        if (!input.getType().equals(ResourceTypeEndpointcost.class)) {
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }
        EndpointcostRequestData requestData = (EndpointcostRequestData)input.getRequest();
        EndpointcostRequest request = requestData.getEndpointcostRequest();
        CostType costType = request.getCostType();

        if (!(request.getFilter() instanceof EndpointFilterData)) {
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }
        if (!costType.getCostMode().equals("ordinal")) {
            LOG.warn(costType.getCostMode().toString());
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }
        if (!costType.getCostMetric().equals(COST_METRIC_ROUTINGCOST)) {
            LOG.warn(costType.getCostMetric().toString());
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }

        EndpointFilter filter = ((EndpointFilterData)request.getFilter()).getEndpointFilter();
        List<? extends TypedAddressData> source = filter.getSource();
        List<? extends TypedAddressData> destination = filter.getDestination();

        int order = 0;
        LinkedList<EndpointCostList> eclList = new LinkedList<EndpointCostList>();
        for (TypedAddressData src: source) {
            LinkedList<EndpointDestinationCost> edcList = new LinkedList<EndpointDestinationCost>();
            for (TypedAddressData dst: destination) {
                EndpointDestinationCostBuilder edcBuilder = new EndpointDestinationCostBuilder();

                edcBuilder.setAddress(createDestinationAddress(dst.getAddress()));
                edcBuilder.setCost(createOrdinalCost(++order));

                edcList.add(edcBuilder.build());
            }

            EndpointCostListBuilder eclBuilder = new EndpointCostListBuilder();
            eclBuilder.setAddress(createSourceAddress(src.getAddress()));
            eclBuilder.setEndpointDestinationCost(edcList);

            eclList.add(eclBuilder.build());
        }

        EndpointCostMapBuilder ecmBuilder = new EndpointCostMapBuilder();
        ecmBuilder.setEndpointCostList(eclList);

        EndpointCostMapDataBuilder ecmdBuilder = new EndpointCostMapDataBuilder();
        ecmdBuilder.setEndpointCostMap(ecmBuilder.build());

        EndpointcostResponseBuilder ecsBuilder = new EndpointcostResponseBuilder();
        ecsBuilder.setCostType(costType);
        ecsBuilder.setData(ecmdBuilder.build());

        EndpointcostResponseDataBuilder ecrdBuilder = new EndpointcostResponseDataBuilder();
        ecrdBuilder.setEndpointcostResponse(ecsBuilder.build());

        QueryOutputBuilder builder = new QueryOutputBuilder();
        builder.setType(ResourceTypeEndpointcost.class).setResponse(ecrdBuilder.build());
        return RpcResultBuilder.<QueryOutput>success(builder.build()).buildFuture();
    }

}
