/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.northbound.route.costmap.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285CostType;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.CostMetric;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.AltoModelCostmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.alto.request.costmap.request.CostmapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.alto.response.costmap.response.CostmapResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.cost.type.data.CostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.cost.type.data.CostTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.request.data.CostmapParams;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.CostmapResponseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.CostmapSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.CostmapSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.costmap.source.CostmapDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.costmap.source.CostmapDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rev151021.costmap.response.data.costmap.response.data.costmap.source.costmap.destination.Cost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.costmap.filter.data.CostmapFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.query.input.request.costmap.request.costmap.params.filter.CostmapFilterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.query.output.response.costmap.response.costmap.response.data.costmap.source.costmap.destination.cost.OrdinalBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
public class AltoNorthboundCostmapTest {

    String filter = "{\"cost-type\": {\"cost-metric\": \"routingcost\",  \"cost-mode\": \"numerical\"}, \"pids\": {\"dsts\": [ \"PID1\",  \"PID2\", \"PID3\"   ],   \"srcs\": [  \"PID1\"   ] }}";
    String path = "test-model-costmap";
    String costmode = "ordinal";
    String costmetri = "routingcost";
    List<String> pid_source  = new ArrayList<String>(){
        {
            add("PID1");
        }
    };
    List<String>  pid_destination = new ArrayList<String>(){
        {
            add("PID1");
            add("PID2");
            add("PID3");
        }
    };

    @Test
    public void testprepareInput() throws JsonProcessingException {

        AltoNorthboundRouteCostmap costmap = new AltoNorthboundRouteCostmap();
        AltoNorthboundRouteCostmap costmapSpy = spy(costmap);
        AltoModelCostmapService mapService = mock(AltoModelCostmapService.class);
        InstanceIdentifier<ContextTag> ctagIID = InstanceIdentifier.create(ContextTag.class);

        //configure mock
        doReturn(ctagIID).when(costmapSpy).getResourceByPath(eq(path),(ReadOnlyTransaction) anyObject());
        costmapSpy.setDataBroker(new DataBroker() {
            @Override
            public ReadOnlyTransaction newReadOnlyTransaction() {
                return null;
            }

            @Override
            public ReadWriteTransaction newReadWriteTransaction() {
                return null;
            }

            @Override
            public WriteTransaction newWriteOnlyTransaction() {
                return null;
            }

            @Override
            public ListenerRegistration<DataChangeListener> registerDataChangeListener(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<?> instanceIdentifier, DataChangeListener dataChangeListener, DataChangeScope dataChangeScope) {
                return null;
            }

            @Override
            public BindingTransactionChain createTransactionChain(TransactionChainListener transactionChainListener) {
                return null;
            }

            @Nonnull
            @Override
            public <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L> registerDataTreeChangeListener(@Nonnull DataTreeIdentifier<T> dataTreeIdentifier, @Nonnull L l) {
                return null;
            }
        });
        costmapSpy.setMapService(new AltoModelCostmapService() {
            @Override
            public Future<RpcResult<QueryOutput>> query(QueryInput queryInput) {
                return null;
            }
        });

        //start test
        costmapSpy.init();
        QueryInput input = costmapSpy.prepareInput(path,costmode,costmetri,pid_source,pid_destination);

        CostmapRequest request = (CostmapRequest)input.getRequest();
        CostmapParams params = request.getCostmapParams();
        CostmapFilter costmapFilter=((CostmapFilterData)params.getFilter()).getCostmapFilter();
        CostType costType = params.getCostType();
        List<PidName> pidNames1 = new LinkedList<PidName>();
        for (String pid:pid_source){
            PidName p = new PidName(pid);
            pidNames1.add(p);
        }
        List<PidName> pidNames2 = new LinkedList<PidName>();
        for (String pid:pid_destination){
            PidName p = new PidName(pid);
            pidNames2.add(p);
        }

        assertEquals(costmetri,costType.getCostMetric().getValue());
        assertEquals(costmode, costType.getCostMode());
        assertEquals(pidNames1, costmapFilter.getPidSource());
        assertEquals(pidNames2, costmapFilter.getPidDestination());
    }

//  Future<RpcResult<QueryOutput>> future;
    protected Cost createOrdinalCost(int order) {
        OrdinalBuilder builder=new OrdinalBuilder();
        builder.setCost(order);
        return builder.build();
    }

    @Test
    public void testgetFilteredMap() throws IOException, ExecutionException, InterruptedException {
        //mock config
        final AltoNorthboundRouteCostmap costmap = new AltoNorthboundRouteCostmap();
        AltoNorthboundRouteCostmap costmapSpy = spy(costmap);
        InstanceIdentifier<ContextTag> ctagIID = InstanceIdentifier.create(ContextTag.class);

        AltoModelCostmapService costmapService = mock(AltoModelCostmapService.class);
        Future<RpcResult<QueryOutput>> future = mock(Future.class);
        RpcResult<QueryOutput> rpcResult = mock(RpcResult.class);
        //build QueryOutput
        int order = 0;
        LinkedList<CostmapSource> costmapSources = new LinkedList<CostmapSource>();
        for(String src:pid_source){
            LinkedList<CostmapDestination> costmapDestinations= new LinkedList<CostmapDestination>();

            for (String dst : pid_destination){
                CostmapDestinationBuilder costmapDestinationBuilder= new CostmapDestinationBuilder();
                costmapDestinationBuilder.setPidDestination(new PidName(dst));
                costmapDestinationBuilder.setCost(createOrdinalCost(++order));
                costmapDestinations.add(costmapDestinationBuilder.build());
            }
            CostmapSourceBuilder costmapSourceBuilder= new CostmapSourceBuilder();
            costmapSourceBuilder.setPidSource(new PidName(src));
            costmapSourceBuilder.setCostmapDestination(costmapDestinations);
            costmapSources.add(costmapSourceBuilder.build());
        }
        CostTypeBuilder costType = new CostTypeBuilder();
        costType.setCostMode(costmode);
        costType.setCostMetric(new CostMetric(costmetri));
        CostmapResponseDataBuilder costmapResponseDataBuilder= new CostmapResponseDataBuilder();
        costmapResponseDataBuilder.setCostType(costType.build());
        costmapResponseDataBuilder.setCostmapSource(costmapSources);

        CostmapResponseBuilder costmapResponseBuilder= new CostmapResponseBuilder();
        costmapResponseBuilder.setCostmapResponseData(costmapResponseDataBuilder.build());

        QueryOutputBuilder queryOutput = new QueryOutputBuilder();
        queryOutput.setResponse(costmapResponseBuilder.build());
        when(rpcResult.getResult()).thenReturn(queryOutput.build());
        when(future.get()).thenReturn(rpcResult);
        when(costmapService.query((QueryInput) anyObject())).thenReturn(future);

        costmapSpy.setMapService(costmapService);

        costmapSpy.setDataBroker(new DataBroker() {
            @Override
            public ReadOnlyTransaction newReadOnlyTransaction() {
                return null;
            }

            @Override
            public ReadWriteTransaction newReadWriteTransaction() {
                return null;
            }

            @Override
            public WriteTransaction newWriteOnlyTransaction() {
                return null;
            }

            @Override
            public ListenerRegistration<DataChangeListener> registerDataChangeListener(LogicalDatastoreType logicalDatastoreType, InstanceIdentifier<?> instanceIdentifier, DataChangeListener dataChangeListener, DataChangeScope dataChangeScope) {
                return null;
            }

            @Override
            public BindingTransactionChain createTransactionChain(TransactionChainListener transactionChainListener) {
                return null;
            }

            @Nonnull
            @Override
            public <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L> registerDataTreeChangeListener(@Nonnull DataTreeIdentifier<T> dataTreeIdentifier, @Nonnull L l) {
                return null;
            }
        });
        doReturn(ctagIID).when(costmapSpy).getResourceByPath(eq(path),(ReadOnlyTransaction) anyObject());
        RFC7285CostMap.Meta meta = new RFC7285CostMap.Meta();
        RFC7285CostType rfc7285costType = new RFC7285CostType();
        rfc7285costType.metric = costmetri;
        rfc7285costType.mode = costmode;
        meta.costType = rfc7285costType;
        doReturn(meta).when(costmapSpy).buildMeta((InstanceIdentifier<?>) anyObject(), (RFC7285CostType)anyObject());

        //start test
        costmapSpy.init();
        Response response = costmapSpy.getFilteredMap(path,filter);
        String responseStr = response.getEntity().toString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseNode = mapper.readTree(responseStr);
        JsonNode costmapNode = responseNode.get("cost-map");
        JsonNode PID1Node = costmapNode.get("PID1");
        JsonNode PID2Node = PID1Node.get("PID2");
        assertEquals("2", PID2Node.asText());

//        assertEquals(responseStr,surex);
    }

}
