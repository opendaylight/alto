/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import com.google.common.base.Optional;
import org.opendaylight.alto.basic.manual.maps.ManualMapsUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.costmap.rev151021.cost.map.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.costmap.rev151021.cost.map.map.DstCosts;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.costmap.rev151021.cost.map.map.dst.costs.Cost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.costmap.rev151021.cost.map.map.dst.costs.cost.TypeNumerical;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.costmap.rev151021.cost.map.map.dst.costs.cost.TypeOrdinal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.rev151021.config.context.ResourceCostMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.costmap.filter.data.CostmapFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.query.input.request.costmap.request.costmap.params.filter.CostmapFilterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.query.output.response.costmap.response.costmap.response.data.costmap.source.costmap.destination.cost.NumericalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.costmap.rfc7285.rev151021.query.output.response.costmap.response.costmap.response.data.costmap.source.costmap.destination.cost.OrdinalBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

public class AltoManualCostmapServiceImpl implements AltoModelCostmapService {
    private static final Logger LOG = LoggerFactory.getLogger(AltoManualCostmapServiceImpl.class);
    private DataBroker dataBroker;

    public AltoManualCostmapServiceImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
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
        CostmapFilter filter = ((CostmapFilterData)params.getFilter()).getCostmapFilter();

        InstanceIdentifier<ContextTag> ctagIID = (InstanceIdentifier<ContextTag>)input.getServiceReference();
        ResourceId resourceId = ctagIID.firstIdentifierOf(ContextTag.class)
                .firstKeyOf(Resource.class)
                .getResourceId();
        String tag = ctagIID.firstKeyOf(ContextTag.class).getTag().getValue();

        ReadOnlyTransaction rx = this.dataBroker.newReadOnlyTransaction();
        List<CostmapSource> costmapSources = getFilteredCostmap(resourceId, tag, filter, rx);

        CostmapResponseDataBuilder cmrdBuilder = new CostmapResponseDataBuilder();
        cmrdBuilder.setCostType(costType);
        cmrdBuilder.setCostmapSource(costmapSources);

        CostmapResponseBuilder cmrBuilder = new CostmapResponseBuilder();
        cmrBuilder.setCostmapResponseData(cmrdBuilder.build());

        QueryOutputBuilder builder = new QueryOutputBuilder();
        builder.setType(ResourceTypeCostmap.class).setResponse(cmrBuilder.build());
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    private List<CostmapSource> getFilteredCostmap(ResourceId resourceId, String tag,
                                                   CostmapFilter filter, ReadOnlyTransaction rx) {
        InstanceIdentifier<ResourceCostMap> costMapIID =
                ManualMapsUtils.getResourceCostMapIID(resourceId.getValue());
        Future<Optional<ResourceCostMap>> rcmFuture =
                rx.read(LogicalDatastoreType.OPERATIONAL, costMapIID);
        Optional<ResourceCostMap> optional = null;
        try {
            optional = rcmFuture.get();
        } catch (Exception e) {
            LOG.error("Reading CostMap failed", e);
        }
        ResourceCostMap resourceCostMap = null;
        if (optional.isPresent()) {
            resourceCostMap = optional.get();
        }

        List<CostmapSource> costmapSources = new LinkedList<>();
        for (Map entry : resourceCostMap.getMap()) {
            if (filter!=null && filter.getPidSource()!=null && !filter.getPidSource().isEmpty()
                    && !filter.getPidSource().contains(entry.getSrc())) {
                continue;
            }

            CostmapSourceBuilder costmapSourceBuilder = new CostmapSourceBuilder();
            costmapSourceBuilder.setPidSource(entry.getSrc());
            List<CostmapDestination> costmapDestinations = new LinkedList<>();
            for (DstCosts dstCosts : entry.getDstCosts()) {
                if (filter!=null && filter.getPidDestination()!=null && !filter.getPidDestination().isEmpty()
                        && !filter.getPidDestination().contains(dstCosts.getDst())) {
                    continue;
                }
                CostmapDestinationBuilder costmapDestinationBuilder = new CostmapDestinationBuilder();
                costmapDestinationBuilder.setPidDestination(dstCosts.getDst());
                Cost cost = dstCosts.getCost();
                if (cost instanceof TypeNumerical) {
                    costmapDestinationBuilder.setCost(new NumericalBuilder()
                            .setCost(((TypeNumerical) cost).getNumericalCostValue())
                            .build());
                } else if (cost instanceof TypeOrdinal) {
                    costmapDestinationBuilder.setCost(new OrdinalBuilder()
                            .setCost(((TypeOrdinal) cost).getOrdinalCostValue())
                            .build());
                }
                costmapDestinations.add(costmapDestinationBuilder.build());
            }
            costmapSourceBuilder.setCostmapDestination(costmapDestinations);
            costmapSources.add(costmapSourceBuilder.build());
        }
        return costmapSources;
    }
}
