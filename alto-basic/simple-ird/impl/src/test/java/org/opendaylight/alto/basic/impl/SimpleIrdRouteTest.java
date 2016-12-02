/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.basic.impl;


import com.google.common.collect.Maps;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstance;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.entry.configuration.data.location.FixedUrlBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.entry.configuration.data.location.RelativePathBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.entry.data.EntryCapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.IrdEntry;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.IrdEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.Rfc7285CostTypeCapabilities;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.Rfc7285CostTypeCapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.Rfc7285IrdMetadata;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.Rfc7285IrdMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.ird.instance.MetaBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.rfc7285.ird.meta.CostType;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.rfc7285.ird.meta.CostTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.CostMetric;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SimpleIrdRouteTest {

    private AltoSimpleIrdProvider m_provider = mock(AltoSimpleIrdProvider.class);
    private HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    private IrdInstance mockIrdInstance = mock(IrdInstance.class);
    SimpleIrdRoute simpleIrdRoute = new SimpleIrdRoute(m_provider);
    SimpleIrdRoute sirSpy = spy(simpleIrdRoute);

    @Test
    public void testRoute() {
        when(m_provider.getInstance(new ResourceId("helloworld"))).thenReturn(mockIrdInstance);

        List<CostType> costTypeList = new LinkedList<>();
        CostTypeBuilder costTypeBuilder = new CostTypeBuilder()
                .setCostMetric(new CostMetric("routingcost"))
                .setCostMode("ordinal")
                .setName("num-routing");
        costTypeList.add(costTypeBuilder.build());
        MetaBuilder metaBuilder = new MetaBuilder()
                .setCostType(costTypeList)
                .setDefaultNetworkMap(new ResourceId("my-default-alto-network-map"));
        Rfc7285IrdMetadata meta = new Rfc7285IrdMetadataBuilder()
                .setMeta(metaBuilder.build())
                .build();

        List<IrdEntry> irdEntryList = new LinkedList<>();
        List<String> listCostTypeName = new LinkedList<>();
        List<ResourceId> listResourceId = new LinkedList<>();
        listResourceId.add(new ResourceId("my-default-alto-network-map"));
        listCostTypeName.add("num-routing");
        EntryCapabilitiesBuilder ecb = new EntryCapabilitiesBuilder()
                .addAugmentation(Rfc7285CostTypeCapabilities.class,
                        new Rfc7285CostTypeCapabilitiesBuilder()
                                .setCostTypeNames(listCostTypeName)
                                .setCostConstraints(true).build()
                );
        IrdEntryBuilder irdEntryBuilder = new IrdEntryBuilder()
                .setLocation(new FixedUrlBuilder().setUri(new Uri("http://alto.example.com/costmap/num/routingcost")).build())
                .setMediaType("application/alto-costmap+json")
                .setEntryCapabilities(ecb.build())
                .setUses(listResourceId)
                .setEntryId(new ResourceId("default-ird"));
        irdEntryList.add(irdEntryBuilder.build());

        irdEntryBuilder
                .setLocation(new RelativePathBuilder().setPath(new Uri("/costmap2/num/routingcost")).build())
                .setMediaType("application/alto-costmap+json")
                .setEntryCapabilities(ecb.build())
                .setUses(listResourceId)
                .setEntryId(new ResourceId("another-ird"));

        irdEntryList.add(irdEntryBuilder.build());

        when(mockIrdInstance.getAugmentation(Rfc7285IrdMetadata.class)).thenReturn(meta);
        when(mockIrdInstance.getIrdEntry()).thenReturn(irdEntryList);
        when(mockHttpServletRequest.getScheme()).thenReturn("http");
        when(mockHttpServletRequest.getLocalName()).thenReturn("alto.example.com");
        when(mockHttpServletRequest.getLocalPort()).thenReturn(80);
        Response resultResponse = this.sirSpy.route(mockHttpServletRequest, "helloworld");

        SimpleIrdRoute.Rfc7285Ird ird = new SimpleIrdRoute.Rfc7285Ird();
        Map<String, Object> costType = new HashMap<String, Object>();
        costType.put("cost-mode", "ordinal");
        costType.put("cost-metric", "routingcost");
        Map<String, Object> costTypes = new HashMap<>();
        costTypes.put("num-routing", costType);
        ird.meta.put("default-alto-network-map", "my-default-alto-network-map");
        ird.meta.put("cost-types", costTypes);

        Map<String, Object> resource = new HashMap<>();

        resource.put("uri", "http://alto.example.com/costmap/num/routingcost");
        resource.put("media-type", "application/alto-costmap+json");

        Map<String, Object> capabilityMap = new HashMap<>();
        capabilityMap.put("cost-type-names", listCostTypeName);
        capabilityMap.put("cost-constraints", true);
        resource.put("capabilities", capabilityMap);
        List<String> uses = new LinkedList<>();
        uses.add("my-default-alto-network-map");
        resource.put("uses", uses);

        ird.resources.put("default-ird", resource);
        resource.put("uri", "http://alto.example.com:80/costmap2/num/routingcost");
        ird.resources.put("another-ird", resource);

        assertEquals(Maps.difference(ird.meta, ((SimpleIrdRoute.Rfc7285Ird)resultResponse.getEntity()).meta).areEqual(), true);

        SimpleIrdRoute nullSimpleIrdRoute = new SimpleIrdRoute(null);
        SimpleIrdRoute sirSpy = spy(nullSimpleIrdRoute);
        assertEquals(sirSpy.route(mockHttpServletRequest, "helloworld").getStatus(), Response.status(Response.Status.NOT_FOUND).build().getStatus());
    }
}