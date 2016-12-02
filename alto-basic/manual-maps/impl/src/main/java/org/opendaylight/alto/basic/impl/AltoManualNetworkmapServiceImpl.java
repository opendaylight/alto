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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.EndpointAddressType;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.network.map.Map;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.config.context.ResourceNetworkMap;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AddressTypeIpv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AddressTypeIpv6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AltoModelNetworkmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.ResourceTypeNetworkmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.alto.request.networkmap.request.NetworkmapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.alto.response.networkmap.response.NetworkmapResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.networkmap.request.data.NetworkmapFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.networkmap.response.data.NetworkMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.networkmap.response.data.network.map.Partition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.networkmap.response.data.network.map.PartitionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rfc7285.rev151021.Ipv4PrefixList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rfc7285.rev151021.Ipv4PrefixListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rfc7285.rev151021.Ipv6PrefixList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rfc7285.rev151021.Ipv6PrefixListBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

public class AltoManualNetworkmapServiceImpl implements AltoModelNetworkmapService {
    private static final Logger LOG = LoggerFactory.getLogger(AltoManualNetworkmapServiceImpl.class);
    private DataBroker dataBroker;

    public AltoManualNetworkmapServiceImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public Future<RpcResult<QueryOutput>> query(QueryInput input) {
        if (!input.getType().equals(ResourceTypeNetworkmap.class)) {
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }
        NetworkmapRequest request = (NetworkmapRequest)input.getRequest();
        NetworkmapFilter filter = request.getNetworkmapFilter();

        InstanceIdentifier<ContextTag> ctagIID = (InstanceIdentifier<ContextTag>)input.getServiceReference();
        ResourceId resourceId = ctagIID.firstIdentifierOf(ContextTag.class)
                .firstKeyOf(Resource.class)
                .getResourceId();
        String tag = ctagIID.firstKeyOf(ContextTag.class).getTag().getValue();

        ReadOnlyTransaction rx = this.dataBroker.newReadOnlyTransaction();
        List<Partition> partitionList = getFilteredNetworkmap(resourceId, tag, filter, rx);

        NetworkMapBuilder networkMapBuilder = new NetworkMapBuilder();
        networkMapBuilder.setPartition(partitionList);

        NetworkmapResponseBuilder nmrBuilder = new NetworkmapResponseBuilder();
        nmrBuilder.setNetworkMap(networkMapBuilder.build());

        QueryOutputBuilder builder = new QueryOutputBuilder();
        builder.setType(ResourceTypeNetworkmap.class).setResponse(nmrBuilder.build());
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    private List<Partition> getFilteredNetworkmap(ResourceId resourceId, String tag,
                                                  NetworkmapFilter filter, ReadOnlyTransaction rx) {
        InstanceIdentifier<ResourceNetworkMap> networkMapIID =
                ManualMapsUtils.getResourceNetworkMapIID(resourceId.getValue());
        Future<Optional<ResourceNetworkMap>> rnmFuture =
                rx.read(LogicalDatastoreType.OPERATIONAL, networkMapIID);
        Optional<ResourceNetworkMap> optional = null;
        try {
            optional = rnmFuture.get();
        } catch (Exception e) {
            LOG.error("Reading NetworkMap failed", e);
            return null;
        }
        if (optional==null || !optional.isPresent()) {
            return null;
        }
        ResourceNetworkMap resourceNetworkMap = optional.get();

        List<Partition> partitionList = new LinkedList<>();

        for (Map entry : resourceNetworkMap.getMap()) {
            if (filter!=null && filter.getPid()!=null && !filter.getPid().isEmpty()
                    && filter.getPid().contains(entry.getPid())) {
                continue;
            }

            PartitionBuilder partitionBuilder = new PartitionBuilder();
            partitionBuilder.setPid(entry.getPid());
            for (EndpointAddressGroup endpointAddressGroup : entry.getEndpointAddressGroup()) {
                if (endpointAddressGroup.getAddressType().getEnumeration() ==
                        EndpointAddressType.Enumeration.Ipv4) {
                    if (filter!=null && filter.getAddressType()!=null && !filter.getAddressType().isEmpty()
                            && !filter.getAddressType().contains(AddressTypeIpv4.class)) {
                        continue;
                    }

                    List<Ipv4Prefix> ipv4List = new LinkedList<>();
                    for (IpPrefix ipPrefix : endpointAddressGroup.getEndpointPrefix()) {
                        ipv4List.add(ipPrefix.getIpv4Prefix());
                    }

                    Ipv4PrefixListBuilder v4Builder = new Ipv4PrefixListBuilder();
                    v4Builder.setIpv4(ipv4List);

                    partitionBuilder.addAugmentation(Ipv4PrefixList.class, v4Builder.build());
                } else if (endpointAddressGroup.getAddressType().getEnumeration() ==
                        EndpointAddressType.Enumeration.Ipv6) {
                    if (filter!=null && filter.getAddressType()!=null && !filter.getAddressType().isEmpty()
                            && !filter.getAddressType().contains(AddressTypeIpv6.class)) {
                        continue;
                    }

                    List<Ipv6Prefix> ipv6List = new LinkedList<>();
                    for (IpPrefix ipPrefix : endpointAddressGroup.getEndpointPrefix()) {
                        ipv6List.add(ipPrefix.getIpv6Prefix());
                    }

                    Ipv6PrefixListBuilder v6Builder = new Ipv6PrefixListBuilder();
                    v6Builder.setIpv6(ipv6List);

                    partitionBuilder.addAugmentation(Ipv6PrefixList.class, v6Builder.build());
                }
            }

            partitionList.add(partitionBuilder.build());
        }
        return partitionList;
    }
}
