/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.impl.endpointcost.test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
import org.opendaylight.alto.core.service.model.endpointcost.EndpointcostUtils;

import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.CostMetric;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.CostTypeData;

import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.CapabilitiesBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.AltoModelEndpointcostService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.CapabilitiesCostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.CapabilitiesCostTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.ResourceTypeEndpointcost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.alto.request.endpointcost.request.EndpointcostRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.alto.response.endpointcost.response.EndpointcostResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.endpointcost.request.data.EndpointcostParams;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.Ipv4AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.Ipv6AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.TypedAddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpoint.filter.data.EndpointFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.EndpointCostMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.EndpointCost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.EndpointCostBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.Cost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.EndpointFilterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.EndpointCostmapDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.cost.NumericalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.cost.OrdinalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.typed.address.data.Address;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoEndpointCostProvider implements BindingAwareProvider, AutoCloseable, AltoModelEndpointcostService {

    private static final Logger LOG = LoggerFactory.getLogger(AltoEndpointCostProvider.class);

    private DataBroker m_dataBroker = null;
    private RoutedRpcRegistration<AltoModelEndpointcostService> m_serviceReg = null;
    private ListenerRegistration<DataChangeListener> m_listener = null;

    private static final String TEST_ENDPOINTCOST_NAME = "test-model-endpointcost";
    private static final ResourceId TEST_ENDPOINTCOST_RID = new ResourceId(TEST_ENDPOINTCOST_NAME);
    private static final CostMetric COST_METRIC_ROUTINGCOST = new CostMetric("routingcost");
    private static final String COST_MODE_ORDINAL = "ordinal";
    private InstanceIdentifier<Resource> m_testIID = null;

    protected void createContextTag()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException  {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        CapabilitiesCostTypeBuilder cctBuilder = new CapabilitiesCostTypeBuilder();
        cctBuilder.setCostType(Arrays.asList(
                EndpointcostUtils.createCostTypeCapability(COST_METRIC_ROUTINGCOST,
                                                            COST_MODE_ORDINAL)
        ));
        CapabilitiesBuilder builder = new CapabilitiesBuilder();
        builder.addAugmentation(CapabilitiesCostType.class, cctBuilder.build());

        ResourcepoolUtils.createResourceWithCapabilities(ResourcepoolUtils.DEFAULT_CONTEXT,
                                            TEST_ENDPOINTCOST_NAME,
                                            ResourceTypeEndpointcost.class,
                                            builder.build(), wx);

        ResourcepoolUtils.lazyUpdateResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                                            TEST_ENDPOINTCOST_NAME, wx);

        wx.submit().get();
    }

    protected void removeContextTag()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException  {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        ResourcepoolUtils.deleteResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                                            TEST_ENDPOINTCOST_NAME, wx);

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
        LOG.info("AltoModelEndpointCostProvider Session Initiated");

        m_dataBroker = session.getSALService(DataBroker.class);
        m_testIID = ResourcepoolUtils.getResourceIID(ResourcepoolUtils.DEFAULT_CONTEXT,
                                                        TEST_ENDPOINTCOST_NAME);
        m_serviceReg = session.addRoutedRpcImplementation(AltoModelEndpointcostService.class, this);

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

    protected Cost createNumericalCost(double cost) {
        NumericalBuilder builder;
        builder = new NumericalBuilder();

        builder.setCost(new BigDecimal(cost));
        return builder.build();
    }

    protected Cost createOrdinalCost(int order) {
        OrdinalBuilder builder;
        builder = new OrdinalBuilder();

        builder.setCost(order);
        return builder.build();
    }

    protected Address createSourceAddress(Address from) {
        if (from instanceof Ipv4AddressData) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.source.address.Ipv4Builder builder;
            builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.source.address.Ipv4Builder();

            builder.fieldsFrom((Ipv4AddressData)from);
            return builder.build();
        } else if (from instanceof Ipv6AddressData) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.source.address.Ipv6Builder builder;
            builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.source.address.Ipv6Builder();

            builder.fieldsFrom((Ipv6AddressData)from);
            return builder.build();
}
        return null;
    }

    protected Address createDestinationAddress(Address from) {
        if (from instanceof Ipv4AddressData) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.destination.address.Ipv4Builder builder;
            builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.destination.address.Ipv4Builder();

            builder.fieldsFrom((Ipv4AddressData)from);
            return builder.build();
        } else if (from instanceof Ipv6AddressData) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.destination.address.Ipv6Builder builder;
            builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.destination.address.Ipv6Builder();

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
        EndpointcostRequest request = (EndpointcostRequest)input.getRequest();
        EndpointcostParams params = request.getEndpointcostParams();

        CostTypeData costType = params.getCostType();
        if (!costType.getCostMode().equals(COST_MODE_ORDINAL)) {
            LOG.warn(costType.getCostMode().toString());
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }
        if (!costType.getCostMetric().equals(COST_METRIC_ROUTINGCOST)) {
            LOG.warn(costType.getCostMetric().toString());
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }

        if (!(params.getFilter() instanceof EndpointFilterData)) {
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }

        EndpointFilter filter = ((EndpointFilterData)params.getFilter()).getEndpointFilter();
        List<? extends TypedAddressData> source = filter.getSource();
        List<? extends TypedAddressData> destination = filter.getDestination();

        int order = 0;
        LinkedList<EndpointCost> ecList = new LinkedList<EndpointCost>();
        for (TypedAddressData src: source) {
            for (TypedAddressData dst: destination) {
                SourceBuilder srcBuilder = new SourceBuilder();
                srcBuilder.setAddress(createSourceAddress(src.getAddress()));

                DestinationBuilder dstBuilder = new DestinationBuilder();
                dstBuilder.setAddress(createDestinationAddress(dst.getAddress()));


                EndpointCostBuilder ecBuilder = new EndpointCostBuilder();
                ecBuilder.setSource(srcBuilder.build());
                ecBuilder.setDestination(dstBuilder.build());
                ecBuilder.setCost(createOrdinalCost(++order));

                ecList.add(ecBuilder.build());
            }
        }

        EndpointCostMapBuilder ecmBuilder = new EndpointCostMapBuilder();
        ecmBuilder.setEndpointCost(ecList);

        EndpointCostmapDataBuilder ecmdBuilder = new EndpointCostmapDataBuilder();
        ecmdBuilder.setEndpointCostMap(ecmBuilder.build());

        EndpointcostResponseBuilder ecrBuilder = new EndpointcostResponseBuilder();
        ecrBuilder.setEndpointcostData(ecmdBuilder.build());

        QueryOutputBuilder builder = new QueryOutputBuilder();
        builder.setType(ResourceTypeEndpointcost.class).setResponse(ecrBuilder.build());
        return RpcResultBuilder.<QueryOutput>success(builder.build()).buildFuture();
    }

}
