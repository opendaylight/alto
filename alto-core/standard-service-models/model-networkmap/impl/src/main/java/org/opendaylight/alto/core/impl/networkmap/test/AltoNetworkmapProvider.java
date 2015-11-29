/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.impl.networkmap.test;

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

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.Tag;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.context.Resource;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AddressTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AddressTypeIpv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AddressTypeIpv6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AltoModelNetworkmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.ResourceTypeFilteredNetworkmap;
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

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoNetworkmapProvider implements BindingAwareProvider, AutoCloseable, AltoModelNetworkmapService {

    private static final Logger LOG = LoggerFactory.getLogger(AltoNetworkmapProvider.class);

    private DataBroker m_dataBroker = null;
    private RoutedRpcRegistration<AltoModelNetworkmapService> m_serviceReg = null;
    private ListenerRegistration<DataChangeListener> m_listener = null;
    private ListenerRegistration<DataChangeListener> m_filteredListener = null;

    private static final String TEST_NETWORKMAP_NAME = "test-model-networkmap";
    private static final String TEST_FILTERED_NETWORKMAP_NAME = "test-model-filtered-networkmap";
    private static final ResourceId TEST_NETWORKMAP_RID = new ResourceId(TEST_NETWORKMAP_NAME);
    private InstanceIdentifier<Resource> m_testIID = null;
    private InstanceIdentifier<Resource> m_testFilteredIID = null;

    protected void createContextTag()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException  {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();
        ResourcepoolUtils.createResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                                            TEST_NETWORKMAP_NAME,
                                            ResourceTypeNetworkmap.class, wx);

        Tag tag = ResourcepoolUtils.lazyUpdateResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                                                        TEST_NETWORKMAP_NAME, wx);

        ResourcepoolUtils.createResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                                            TEST_FILTERED_NETWORKMAP_NAME,
                                            ResourceTypeFilteredNetworkmap.class, wx);

        List<InstanceIdentifier<?>> dependencies = new LinkedList<>();
        dependencies.add(
                (InstanceIdentifier<?>)ResourcepoolUtils.getContextTagIID(
                        ResourcepoolUtils.DEFAULT_CONTEXT,
                        TEST_NETWORKMAP_NAME, tag.getValue())
        );
        ResourcepoolUtils.updateResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                                            TEST_FILTERED_NETWORKMAP_NAME,
                                            dependencies, wx);
        wx.submit().get();
    }

    protected void removeContextTag()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException  {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        ResourcepoolUtils.deleteResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                                            TEST_NETWORKMAP_NAME, wx);

        ResourcepoolUtils.deleteResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                                            TEST_FILTERED_NETWORKMAP_NAME, wx);

        wx.submit().get();
    }

    protected void setupListener() {
        ContextTagListener listener;

        listener = new ContextTagListener(m_testIID, m_serviceReg);
        m_listener = m_dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                                        m_testIID, listener, DataChangeScope.SUBTREE);

        listener = new ContextTagListener(m_testFilteredIID, m_serviceReg);
        m_filteredListener = m_dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                                        m_testFilteredIID, listener, DataChangeScope.SUBTREE);

        assert m_listener != null;
        assert m_filteredListener != null;
    }

    protected void closeListener() {
        if (m_listener != null) {
            m_listener.close();
        }
        if (m_filteredListener != null) {
            m_filteredListener.close();
        }
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoModelNetworkProvider Session Initiated");

        m_dataBroker = session.getSALService(DataBroker.class);
        m_serviceReg = session.addRoutedRpcImplementation(AltoModelNetworkmapService.class, this);
        m_testIID = ResourcepoolUtils.getResourceIID(ResourcepoolUtils.DEFAULT_CONTEXT,
                                                        TEST_NETWORKMAP_NAME);
        m_testFilteredIID = ResourcepoolUtils.getResourceIID(ResourcepoolUtils.DEFAULT_CONTEXT,
                                                                TEST_FILTERED_NETWORKMAP_NAME);

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

    @Override
    public Future<RpcResult<QueryOutput>> query(QueryInput input) {
        if (!input.getType().equals(ResourceTypeNetworkmap.class)) {
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }
        NetworkmapRequest request = (NetworkmapRequest)input.getRequest();
        NetworkmapFilter filter = request.getNetworkmapFilter();

        List<PidName> pids = filter.getPid();
        List<Class<? extends AddressTypeBase>> types = filter.getAddressType();

        List<Partition> partitionList = new LinkedList<>();
        int index = 0;
        for (PidName pid: pids) {
            ++index;

            PartitionBuilder partitionBuilder = new PartitionBuilder();
            partitionBuilder.setPid(pid);

            if (types.contains(AddressTypeIpv4.class)) {
                LinkedList<Ipv4Prefix> ipv4List = new LinkedList<Ipv4Prefix>();
                ipv4List.add(new Ipv4Prefix("192.168." + index + ".0/24"));

                Ipv4PrefixListBuilder v4Builder = new Ipv4PrefixListBuilder();
                v4Builder.setIpv4(ipv4List);

                partitionBuilder.addAugmentation(Ipv4PrefixList.class, v4Builder.build());
            }
            if (types.contains(AddressTypeIpv6.class)) {
                LinkedList<Ipv6Prefix> ipv6List = new LinkedList<Ipv6Prefix>();
                ipv6List.add(new Ipv6Prefix("2001:b8:ca2:" + index + "::0/64"));

                Ipv6PrefixListBuilder v6Builder = new Ipv6PrefixListBuilder();
                v6Builder.setIpv6(ipv6List);

                partitionBuilder.addAugmentation(Ipv6PrefixList.class, v6Builder.build());
            }

            partitionList.add(partitionBuilder.build());
        }

        NetworkMapBuilder nmBuilder = new NetworkMapBuilder();
        nmBuilder.setPartition(partitionList);

        NetworkmapResponseBuilder nmrBuilder = new NetworkmapResponseBuilder();
        nmrBuilder.setNetworkMap(nmBuilder.build());

        QueryOutputBuilder builder = new QueryOutputBuilder();

        builder.setType(ResourceTypeNetworkmap.class).setResponse(nmrBuilder.build());
        return RpcResultBuilder.<QueryOutput>success(builder.build()).buildFuture();
    }

}
