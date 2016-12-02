/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.impl.costmap.test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;
import org.opendaylight.alto.core.service.model.costmap.CostmapUtils;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;

import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.CapabilitiesBuilder;

import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.CostMetric;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.PidName;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.AltoModelCostmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.ResourceTypeCostmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.ResourceTypeFilteredCostmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.alto.request.costmap.request.CostmapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.alto.response.costmap.response.CostmapResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.cost.type.data.CostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.request.data.CostmapParams;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.CostmapResponseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.CostmapSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.CostmapSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.costmap.source.CostmapDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.costmap.source.CostmapDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.costmap.source.costmap.destination.Cost;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.costmap.filter.data.CostmapFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.query.input.request.costmap.request.costmap.params.filter.CostmapFilterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.query.output.response.costmap.response.costmap.response.data.costmap.source.costmap.destination.cost.NumericalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.query.output.response.costmap.response.costmap.response.data.costmap.source.costmap.destination.cost.OrdinalBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.CapabilitiesCostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.CapabilitiesCostTypeBuilder;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoCostmapProvider implements BindingAwareProvider, AutoCloseable, AltoModelCostmapService {

    private static final Logger LOG = LoggerFactory.getLogger(AltoCostmapProvider.class);


    private DataBroker m_dataBroker = null;
    private BindingAwareBroker.RoutedRpcRegistration<AltoModelCostmapService> m_serviceReg = null;
    private ListenerRegistration<DataChangeListener> m_listener=null;

    private static final String TEST_COSTMAP_NAME="test-model-costmap";
    private static final String TEST_FILTERED_COSTMAP_NAME="test-model-filtered-costmap";
    private static final ResourceId TEST_COSTMAP_RID = new ResourceId(TEST_COSTMAP_NAME);
    private static final CostMetric COST_METRIC_ROUTINGCOST = new CostMetric("routingcost");
    private static final String COST_MODE_ORDINAL = "ordinal";
    private InstanceIdentifier<Resource> m_testIID = null;

    protected void createContextTag()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException  {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        CapabilitiesCostTypeBuilder cctBuilder = new CapabilitiesCostTypeBuilder();
        cctBuilder.setCostType(Arrays.asList(
                CostmapUtils.createCostTypeCapability(COST_METRIC_ROUTINGCOST,
                        COST_MODE_ORDINAL)
        ));
        CapabilitiesBuilder builder = new CapabilitiesBuilder();
        builder.addAugmentation(CapabilitiesCostType.class, cctBuilder.build());

        ResourcepoolUtils.createResourceWithCapabilities(ResourcepoolUtils.DEFAULT_CONTEXT,
                TEST_COSTMAP_NAME,
                ResourceTypeCostmap.class,
                builder.build(), wx);

        ResourcepoolUtils.lazyUpdateResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                TEST_COSTMAP_NAME, wx);

        ResourcepoolUtils.createResourceWithCapabilities(ResourcepoolUtils.DEFAULT_CONTEXT,
                TEST_FILTERED_COSTMAP_NAME,
                ResourceTypeFilteredCostmap.class,
                builder.build(), wx);

        ResourcepoolUtils.lazyUpdateResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                TEST_FILTERED_COSTMAP_NAME, wx);

        wx.submit().get();
    }


    protected void removeContextTag()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException  {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        ResourcepoolUtils.deleteResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                TEST_COSTMAP_NAME, wx);

        wx.submit().get();
    }

    protected void setupListener() {
        ResourcepoolUtils.ContextTagListener listener = new ResourcepoolUtils.ContextTagListener(m_testIID, m_serviceReg);
        m_listener = m_dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                m_testIID,listener, AsyncDataBroker.DataChangeScope.SUBTREE);

        assert m_listener != null;
    }

    @Override
    public void onSessionInitiated(BindingAwareBroker.ProviderContext session) {
        LOG.info("AltoModelCostmapProvider Session Initiated");

        m_dataBroker = session.getSALService(DataBroker.class);
        m_testIID = ResourcepoolUtils.getResourceIID(ResourcepoolUtils.DEFAULT_CONTEXT,
                TEST_COSTMAP_NAME);
        m_serviceReg = session.addRoutedRpcImplementation(AltoModelCostmapService.class, this);


        try{
            setupListener();
            createContextTag();
        } catch (Exception e){
        }

    }

    protected Cost createNumericalCost(double cost) {
        NumericalBuilder builder;
        builder = new NumericalBuilder();
        builder.setCost(new BigDecimal(cost));
        return builder.build();
    }

    protected Cost createOrdinalCost(int order) {
        OrdinalBuilder builder=new OrdinalBuilder();
        builder.setCost(order);
        return builder.build();
    }


    @Override
    public void close() throws Exception {
        LOG.info("AltoModelCostProvider Closed");

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
        if (!input.getType().equals(ResourceTypeCostmap.class)
                && !input.getType().equals(ResourceTypeFilteredCostmap.class)) {
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }

        CostmapRequest request = (CostmapRequest)input.getRequest();
        CostmapParams params = request.getCostmapParams();

        CostType costType = params.getCostType();
//        if(!(params.getFilter() instanceof CostmapFilter)){
//            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
//        }
//        if(!costType.getCostMode().equals("ordinal")){
//            LOG.warn(costType.getCostMode().toString());
//            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
//        }
//        if(!costType.getCostMetric().equals(COST_METRIC_ROUTINGCOST)){
//            LOG.warn(costType.getCostMetric().toString());
//            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
//        }
        LOG.info("query test3");

        CostmapFilter costmapFilter=((CostmapFilterData)params.getFilter()).getCostmapFilter();
        List<PidName> pid_src=costmapFilter.getPidSource();
        List<PidName> pid_dst=costmapFilter.getPidDestination();
        int order = 0;
        LinkedList<CostmapSource> costmapSources = new LinkedList<CostmapSource>();
        for(PidName src:pid_src){
            LinkedList<CostmapDestination> costmapDestinations= new LinkedList<CostmapDestination>();

            for (PidName dst : pid_dst){
                CostmapDestinationBuilder costmapDestinationBuilder= new CostmapDestinationBuilder();
                costmapDestinationBuilder.setPidDestination(dst);
                costmapDestinationBuilder.setCost(createOrdinalCost(++order));
                costmapDestinations.add(costmapDestinationBuilder.build());
            }
            CostmapSourceBuilder costmapSourceBuilder= new CostmapSourceBuilder();
            costmapSourceBuilder.setPidSource(src);
            costmapSourceBuilder.setCostmapDestination(costmapDestinations);
            costmapSources.add(costmapSourceBuilder.build());
        }





        CostmapResponseDataBuilder costmapResponseDataBuilder= new CostmapResponseDataBuilder();
        costmapResponseDataBuilder.setCostType(costType);
        costmapResponseDataBuilder.setCostmapSource(costmapSources);

        CostmapResponseBuilder costmapResponseBuilder= new CostmapResponseBuilder();
        costmapResponseBuilder.setCostmapResponseData(costmapResponseDataBuilder.build());


        QueryOutputBuilder builder = new QueryOutputBuilder();

        builder.setType(ResourceTypeCostmap.class).setResponse(costmapResponseBuilder.build());


        return RpcResultBuilder.<QueryOutput>success(builder.build()).buildFuture();
    }
}
