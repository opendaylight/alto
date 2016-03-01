package org.opendaylight.alto.core.northbound.route.endpointcost.impl;

/**
 * Created by wukunheng00 on 16-2-5.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.AltoModelEndpointcostService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.alto.request.endpointcost.request.EndpointcostRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.alto.response.endpointcost.response.EndpointcostResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.cost.type.container.CostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.endpointcost.request.data.EndpointcostParams;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.Ipv4AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.Ipv6AddressData;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.TypedAddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpoint.filter.data.EndpointFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpoint.filter.data.endpoint.filter.Destination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpoint.filter.data.endpoint.filter.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpoint.filter.data.endpoint.filter.Source;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpoint.filter.data.endpoint.filter.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.EndpointCostMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.EndpointCost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.EndpointCostBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.Cost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.EndpointFilterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.endpoint.filter.data.endpoint.filter.source.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.endpoint.filter.data.endpoint.filter.source.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.EndpointCostmapDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.cost.OrdinalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.typed.address.data.Address;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
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

public class AltoNorthboundEndpointcostTest {

    String filter = "{\"cost-type\":{\"cost-mode\" : \"ordinal\",\"cost-metric\" : \"routingcost\"},\"endpoints\" : {\"srcs\": [ \"ipv4:192.0.2.2\" ],\"dsts\": [\"ipv4:192.0.2.89\",\"ipv4:198.51.100.34\",\"ipv4:203.0.113.45\"]}}";
    String path = "test-model-endpointcost";
    String costmode = "ordinal";
    String costmetri = "routingcost";
    List<String> endpoints_source = new ArrayList<String>(){
        {
            add("ipv4:192.0.2.2");
        }
    };
    List<String>  endpoints_destination = new ArrayList<String>(){
        {
            add("ipv4:192.0.2.89");
            add("ipv4:198.51.100.34");
            add("ipv4:203.0.113.45");
        }
    };
    @Test
    public void testprepareInput() throws JsonProcessingException {

        AltoNorthboundRouteEndpointcost endpointcost = new AltoNorthboundRouteEndpointcost();
        AltoNorthboundRouteEndpointcost endpointcostSpy = spy(endpointcost);

        BindingAwareBroker.ProviderContext session = mock(BindingAwareBroker.ProviderContext.class);
        InstanceIdentifier<ContextTag> ctagIID = InstanceIdentifier.create(ContextTag.class);

        //configure mock
        doReturn(ctagIID).when(endpointcostSpy).getResourceByPath(eq(path),(ReadOnlyTransaction) anyObject());
        when(session.getSALService(DataBroker.class)).thenReturn(new DataBroker() {
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
        when(session.getRpcService(AltoModelEndpointcostService.class)).thenReturn(new AltoModelEndpointcostService() {
            @Override
            public Future<RpcResult<QueryOutput>> query(QueryInput queryInput) {
                return null;
            }
        });

        //start test
        endpointcostSpy.onSessionInitiated(session);
        QueryInput input = endpointcostSpy.prepareInput(path,costmode,costmetri,endpoints_source,endpoints_destination);
        EndpointcostRequest request = (EndpointcostRequest)input.getRequest();
        EndpointcostParams params = request.getEndpointcostParams();
        EndpointFilter costmapFilter=((EndpointFilterData)params.getFilter()).getEndpointFilter();
        Ipv4Builder ipv4 = new Ipv4Builder();
        ipv4.setIpv4(new Ipv4Address("192.0.2.2"));
        Assert.assertEquals(costmapFilter.getSource().get(0).getAddress(), ipv4.build());
        CostType costType = params.getCostType();

        assertEquals(costmetri,costType.getCostMetric().getValue());
        assertEquals(costmode, costType.getCostMode());
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

    protected Cost createOrdinalCost(int order) {
        OrdinalBuilder builder;
        builder = new OrdinalBuilder();

        builder.setCost(order);
        return builder.build();
    }
    @Test
    public void testgetFilteredMap() throws ExecutionException, InterruptedException, JsonProcessingException {
        //mock config
        AltoNorthboundRouteEndpointcost endpointcost = new AltoNorthboundRouteEndpointcost();
        AltoNorthboundRouteEndpointcost endpointcostSpy = spy(endpointcost);
        BindingAwareBroker.ProviderContext session = mock(BindingAwareBroker.ProviderContext.class);
        InstanceIdentifier<ContextTag> ctagIID = InstanceIdentifier.create(ContextTag.class);

        AltoModelEndpointcostService endpointcostService = mock(AltoModelEndpointcostService.class);
        Future<RpcResult<QueryOutput>> future = mock(Future.class);
        RpcResult<QueryOutput> rpcResult = mock(RpcResult.class);
        List<Source> sources = new ArrayList<Source>();
        List<Destination> destinations = new ArrayList<Destination>();

        for(String source : endpoints_source){
            SourceBuilder sourceBuilder = new SourceBuilder();
            String[] tmp= source.split(":");
            Ipv4Builder ipv4 = new Ipv4Builder();
            Ipv6Builder ipv6 = new Ipv6Builder();
            if(tmp[0].equals("ipv4")){
                ipv4.setIpv4(new Ipv4Address(tmp[1]));
                sourceBuilder.setAddress(ipv4.build());
            }else if(tmp[0].equals("ipv6")){
                ipv6.setIpv6(new Ipv6Address(tmp[1]));
                sourceBuilder.setAddress(ipv6.build());
            }
            sources.add(sourceBuilder.build());
        }
        for(String destination : endpoints_destination){
            DestinationBuilder destinationBuilder = new DestinationBuilder();
            String[] tmp= destination.split(":");
            Ipv4Builder ipv4 = new Ipv4Builder();
            Ipv6Builder ipv6 = new Ipv6Builder();
            if(tmp[0].equals("ipv4")){
                ipv4.setIpv4(new Ipv4Address(tmp[1]));
                destinationBuilder.setAddress(ipv4.build());
            }else if(tmp[0].equals("ipv6")){
                ipv6.setIpv6(new Ipv6Address(tmp[1]));
                destinationBuilder.setAddress(ipv6.build());
            }
            destinations.add(destinationBuilder.build());
        }

        List<? extends TypedAddressData> source = sources;

        List<? extends TypedAddressData> destination = destinations;

        int order = 0;
        LinkedList<EndpointCost> ecList = new LinkedList<EndpointCost>();
        for (TypedAddressData src: source) {
            for (TypedAddressData dst: destination) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.SourceBuilder srcBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.SourceBuilder();
                srcBuilder.setAddress(createSourceAddress(src.getAddress()));

                org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.DestinationBuilder
                dstBuilder = new  org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.DestinationBuilder();
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
        QueryOutputBuilder queryOutputBuilder = new QueryOutputBuilder();
        queryOutputBuilder.setResponse(ecrBuilder.build());

        when(rpcResult.getResult()).thenReturn(queryOutputBuilder.build());
        when(future.get()).thenReturn(rpcResult);
        when(endpointcostService.query((QueryInput)anyObject())).thenReturn(future);

        when(session.getSALService(DataBroker.class)).thenReturn(new DataBroker() {
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
        when(session.getRpcService(AltoModelEndpointcostService.class)).thenReturn(endpointcostService);

        doReturn(ctagIID).when(endpointcostSpy).getResourceByPath(eq(path), (ReadOnlyTransaction)anyObject());

        //start test
        endpointcostSpy.onSessionInitiated(session);
        Response response = endpointcostSpy.getFilteredMap(path, filter);
        String surex = "{\"meta\":{\"cost-type\":{\"cost-mode\":\"ordinal\",\"cost-metric\":\"routingcost\"}},\"endpoint-cost-map\":{\"ipv4:192.0.2.2\":{\"ipv4:198.51.100.34\":\"2\",\"ipv4:192.0.2.89\":\"1\",\"ipv4:203.0.113.45\":\"3\"}}}";

        assertEquals(response.getEntity().toString(), surex);
    }


}
