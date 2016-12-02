/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.impl.base;


import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.CostTypeData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.ResourceTypeEndpointcost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.alto.request.endpointcost.request.EndpointcostRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.alto.response.endpointcost.response.EndpointcostResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.endpointcost.request.data.EndpointcostParams;
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
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseECSImplementation {
    protected DataBroker dataBroker;
    private RpcResult<QueryOutput> ecsOutput;

    protected EndpointcostRequest request;
    protected EndpointcostParams params;
    protected CostTypeData costType;
    protected EndpointFilter filter;

    public ListenableFuture<RpcResult<QueryOutput>> getECS(QueryInput input){
        if (checkInput(input) && postCheck(input)) {
            buildEndpointCostServiceOutput(input);
        }
        return Futures.immediateFuture(ecsOutput);
    }

    private void buildEndpointCostServiceOutput(QueryInput input) {
        request = (EndpointcostRequest)input.getRequest();
        params = request.getEndpointcostParams();
        costType = params.getCostType();
        filter = ((EndpointFilterData)params.getFilter()).getEndpointFilter();
        List<EndpointCost> ecmList = buildEndpointCostMapList(filter.getSource(), filter.getDestination(), costType);
        buildEndpointCostServiceOutput(ecmList);
    }

    private void buildEndpointCostServiceOutput(List<EndpointCost> ecmList) {
        EndpointCostMapBuilder endpointCostMapBuilder = new EndpointCostMapBuilder().setEndpointCost(ecmList);
        EndpointCostmapDataBuilder endpointCostmapDataBuilder = new EndpointCostmapDataBuilder().setEndpointCostMap(endpointCostMapBuilder.build());
        EndpointcostResponseBuilder endpointcostResponseBuilder = new EndpointcostResponseBuilder().setEndpointcostData(endpointCostmapDataBuilder.build());
        QueryOutputBuilder queryOutputBuilder = new QueryOutputBuilder().setResponse(endpointcostResponseBuilder.build()).setType(ResourceTypeEndpointcost.class);
        if (queryOutputBuilder != null) {
            this.ecsOutput = RpcResultBuilder.success(queryOutputBuilder).build();
        } else {
            this.ecsOutput = buildErrorRpcResult("Invalid output value ",
                    "output is null");
        }
    }

    private List<EndpointCost> buildEndpointCostMapList(List<? extends TypedAddressData> source, List<? extends TypedAddressData> destination, CostTypeData costType) {
        List<EndpointCost> ecmList = new ArrayList<EndpointCost>();
        for (TypedAddressData src : source){
            for(TypedAddressData dst:destination){
                Cost cost = getCost(src, dst, costType);
                ecmList.add(buildEndpointCost(src,dst,cost));
            }
        }
        //TODO  operate oridinal
        return ecmList;
    }

    private EndpointCost buildEndpointCost(TypedAddressData src, TypedAddressData dst, Cost cost) {
        EndpointCost endpointCost = null;
        SourceBuilder sourceBuilder = new SourceBuilder();
        DestinationBuilder destinationBuilder = new DestinationBuilder();
        endpointCost = new EndpointCostBuilder().setCost(cost)
                .setSource(sourceBuilder.setAddress(src.getAddress()).build())
                .setDestination(destinationBuilder.setAddress(dst.getAddress()).build())
                .build();
        return endpointCost;
    }

    private Cost getCost(TypedAddressData src, TypedAddressData dst, CostTypeData costType) {
        if (validSrcDstIpPair(src, dst)) {
            return computeCost(src, dst, costType);
        }
        return null;
    }

    protected abstract Cost computeCost(TypedAddressData src, TypedAddressData dst, CostTypeData costType);


    private boolean validSrcDstIpPair(TypedAddressData src, TypedAddressData dst) {
        return true;
    }

    /**
     * @param input is the input of ECS.
     * @return true if input is validate.
     */
    public boolean checkInput(QueryInput input) {
        request = (EndpointcostRequest)input.getRequest();
        params = request.getEndpointcostParams();
        costType = params.getCostType();
        filter = ((EndpointFilterData)params.getFilter()).getEndpointFilter();

        if(costType == null){
            ecsOutput = buildErrorRpcResult("Invalid cost-type value ",
                    "CostType is null");
            return false;
        }
        if(!(params.getFilter() instanceof EndpointFilterData) || filter == null){
            ecsOutput = buildErrorRpcResult("Invalid endpoints value ",
                    "Endpoints is null");
            return false;
        }
        return true;
    }

    protected boolean postCheck(QueryInput input) {
        return true;
    }

    /**
     * @param errorCode
     * @param errorMessage
     * @return built RPC error result.
     */
    protected RpcResult<QueryOutput> buildErrorRpcResult(
            String errorCode, String errorMessage) {
        return RpcResultBuilder.<QueryOutput> failed()
                .withError(RpcError.ErrorType.APPLICATION, errorCode, errorMessage)
                .build();
    }
}
