/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.northbound.route.networkmap.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285NetworkMap;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.PidName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AddressTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AddressTypeIpv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AddressTypeIpv6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.AltoModelNetworkmapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.alto.request.networkmap.request.NetworkmapRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.alto.response.networkmap.response.NetworkmapResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.networkmap.response.data.NetworkMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.networkmap.response.data.network.map.Partition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rev151021.networkmap.response.data.network.map.PartitionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rfc7285.rev151021.Ipv4PrefixList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rfc7285.rev151021.Ipv4PrefixListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rfc7285.rev151021.Ipv6PrefixList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.networkmap.rfc7285.rev151021.Ipv6PrefixListBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
public class AltoNorthboundNetworkmapTest {
    String path = "test-model-networkmap";
    String filter = "{\n" +
            "\t\"pids\": [\"PID1\", \"PID2\"],\n" +
            "\t\"address-types\": [\"ipv4\", \"ipv6\"]\n" +
            "}";
    List<String> pids = new ArrayList<String>(){
        {
            add("PID1");
            add("PID2");
        }
    };
    List<String> addressTypes = new ArrayList<String>(){
        {
            add("ipv4");
            add("ipv6");
        }
    };
    @Test
    public void testprepareInput() throws JsonProcessingException{

        //configure the mock
        AltoNorthboundRouteNetworkmap networkmap = new AltoNorthboundRouteNetworkmap();
        AltoNorthboundRouteNetworkmap networkmapSpy = spy(networkmap);

        InstanceIdentifier<ContextTag> ctagIID = InstanceIdentifier.create(ContextTag.class);
        doReturn(ctagIID).when(networkmapSpy).getResourceByPath(eq(path), (ReadOnlyTransaction) anyObject());
        doReturn( AddressTypeIpv4.class).when(networkmapSpy).getAddressTypeByName(eq("ipv4"), eq(path), (ReadOnlyTransaction)anyObject());
        doReturn( AddressTypeIpv6.class).when(networkmapSpy).getAddressTypeByName(eq("ipv6"), eq(path), (ReadOnlyTransaction)anyObject());
        networkmapSpy.setDataBroker(mock(DataBroker.class));

        networkmapSpy.setMapService(queryInput -> null);

        QueryInput input = networkmapSpy.prepareInput(path, pids, addressTypes);
        NetworkmapRequest request = (NetworkmapRequest)input.getRequest();
        PidName pid1 = request.getNetworkmapFilter().getPid().get(0);
        PidName pid2 = request.getNetworkmapFilter().getPid().get(1);

        assertEquals(pid1.getValue(), "PID1");
        assertEquals(pid2.getValue(), "PID2");

        assertEquals(request.getNetworkmapFilter().getAddressType().get(0), AddressTypeIpv4.class);
        assertEquals(request.getNetworkmapFilter().getAddressType().get(1), AddressTypeIpv6.class);
    }

    @Test
    public void testgetFilteredMap() throws ExecutionException, InterruptedException, IOException {
        //config mock
        AltoNorthboundRouteNetworkmap networkmap = new AltoNorthboundRouteNetworkmap();
        AltoNorthboundRouteNetworkmap networkmapSpy = spy(networkmap);

        InstanceIdentifier<ContextTag> ctagIID = InstanceIdentifier.create(ContextTag.class);
        doReturn(ctagIID).when(networkmapSpy).getResourceByPath(eq(path), (ReadOnlyTransaction) anyObject());
        doReturn( AddressTypeIpv4.class).when(networkmapSpy).getAddressTypeByName(eq("ipv4"), eq(path), (ReadOnlyTransaction)anyObject());
        doReturn( AddressTypeIpv6.class).when(networkmapSpy).getAddressTypeByName(eq("ipv6"), eq(path), (ReadOnlyTransaction)anyObject());

        AltoModelNetworkmapService networkmapService = mock(AltoModelNetworkmapService.class);

        Future<RpcResult<QueryOutput>> future = mock(Future.class);
        RpcResult<QueryOutput> rpcResult = mock(RpcResult.class);
        List<Class<? extends AddressTypeBase>> types = new ArrayList<Class<? extends AddressTypeBase>>(){
            {
                add(AddressTypeIpv4.class);
                add(AddressTypeIpv6.class);
            }
        };
        List<Partition> partitionList = new LinkedList<>();
        int index = 0;
        for (String pid: pids) {
            ++index;

            PartitionBuilder partitionBuilder = new PartitionBuilder();
            partitionBuilder.setPid(new PidName(pid));

            if (types.contains(AddressTypeIpv4.class)) {
                LinkedList<Ipv4Prefix> ipv4List = new LinkedList<>();
                ipv4List.add(new Ipv4Prefix("192.168." + index + ".0/24"));

                Ipv4PrefixListBuilder v4Builder = new Ipv4PrefixListBuilder();
                v4Builder.setIpv4(ipv4List);

                partitionBuilder.addAugmentation(Ipv4PrefixList.class, v4Builder.build());
            }
            if (types.contains(AddressTypeIpv6.class)) {
                LinkedList<Ipv6Prefix> ipv6List = new LinkedList<>();
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

        QueryOutputBuilder queryOutputBuilder = new QueryOutputBuilder();
        queryOutputBuilder.setResponse(nmrBuilder.build());
        when(rpcResult.getResult()).thenReturn(queryOutputBuilder.build());
        when(future.get()).thenReturn(rpcResult);
        when(networkmapService.query((QueryInput)anyObject())).thenReturn(future);
        networkmapSpy.setMapService(networkmapService);
        networkmapSpy.setDataBroker(mock(DataBroker.class));

        doReturn(new RFC7285NetworkMap.Meta()).when(networkmapSpy).buildMeta((InstanceIdentifier<?>)anyObject());
        //start test
        networkmapSpy.init();
        Response response = networkmapSpy.getFilteredMap(path, filter);
        String surex = response.getEntity().toString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseTree = mapper.readTree(surex);
        JsonNode networkmapTree = responseTree.get("network-map");
        JsonNode PID1 = networkmapTree.get("PID1");
        JsonNode ipv4Node = PID1.get("ipv4");
        assertEquals("192.168.1.0/24", ipv4Node.get(0).asText());
    }

}
