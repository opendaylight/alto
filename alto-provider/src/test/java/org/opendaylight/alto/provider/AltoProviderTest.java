/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.provider;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
//import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.address.tracker.rev140617.address.node.connector.Addresses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.address.tracker.rev140617.address.node.connector.AddressesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.EndpointCostMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.EndpointCostMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.endpoint.cost.map.DstCosts;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.endpoint.cost.map.DstCostsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.costdefault.rev150507.DstCosts1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.costdefault.rev150507.DstCosts1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TypedEndpointAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TypedIpv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.host.AttachmentPoints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.host.AttachmentPointsBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Destination;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.DestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Source;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class AltoProviderTest {

  private AltoProvider altoProvider;
  private Addresses addrs;
  private DataBroker dataBroker;
  private IpAddress ipAddress;
  private MacAddress mac;


  @Before
  public void init(){
    dataBroker = mock(DataBroker.class);
    altoProvider = new AltoProvider();
  }

  @Test
  public void onProcessTopology() throws Exception {
      IpAddress ipAddress1 = new IpAddress(new Ipv4Address("10.0.0.1"));
      MacAddress mac1 = new MacAddress("00:00:00:00:00:01");
      Addresses addrs1 = new AddressesBuilder().setIp(ipAddress1).setMac(mac1).build();
      List<Addresses> addrsList1 = new ArrayList<Addresses>();
      addrsList1.add(addrs1);

      AttachmentPoints ap1 = new AttachmentPointsBuilder().setTpId(new TpId("openflow:1:1")).build();
      List<AttachmentPoints> apList1 = new ArrayList<AttachmentPoints>();
      apList1.add(ap1);
      HostNode hostNode1 = new HostNodeBuilder().setAddresses(addrsList1).setAttachmentPoints(apList1).build();

      IpAddress ipAddress2 = new IpAddress(new Ipv4Address("10.0.0.2"));
      MacAddress mac2 = new MacAddress("00:00:00:00:00:02");
      Addresses addrs2 = new AddressesBuilder().setIp(ipAddress2).setMac(mac2).build();
      List<Addresses> addrsList2 = new ArrayList<Addresses>();
      addrsList2.add(addrs2);

      AttachmentPoints ap2 = new AttachmentPointsBuilder().setTpId(new TpId("openflow:2:1")).build();
      List<AttachmentPoints> apList2 = new ArrayList<AttachmentPoints>();
      apList2.add(ap2);
      HostNode hostNode2 = new HostNodeBuilder().setAddresses(addrsList2).setAttachmentPoints(apList2).build();


      Node node1 = new NodeBuilder().addAugmentation(HostNode.class, hostNode1).build();
      Node node2 = new NodeBuilder().addAugmentation(HostNode.class, hostNode2).build();

      Node sw1 = new NodeBuilder().setNodeId(new NodeId("openflow:1")).build();
      Node sw2 = new NodeBuilder().setNodeId(new NodeId("openflow:2")).build();
      Node sw3 = new NodeBuilder().setNodeId(new NodeId("openflow:3")).build();

      Destination SW1_2 = new DestinationBuilder().setDestNode(new NodeId("openflow:1"))
              .setDestTp(new TpId("openflow:1:2")).build();
      Source SW3_1 = new SourceBuilder().setSourceNode(new NodeId("openflow:3"))
              .setSourceTp(new TpId("openflow:3:1")).build();
      Link l1 = new LinkBuilder().setLinkId(new LinkId("link1"))
              .setDestination(SW1_2)
              .setSource(SW3_1).build();

      Destination SW1_3 = new DestinationBuilder().setDestNode(new NodeId("openflow:1"))
              .setDestTp(new TpId("openflow:1:3")).build();
      Source SW2_2 = new SourceBuilder().setSourceNode(new NodeId("openflow:2"))
              .setSourceTp(new TpId("openflow:2:2")).build();
      Link l2 = new LinkBuilder().setLinkId(new LinkId("link2"))
              .setDestination(SW1_3)
              .setSource(SW2_2).build();

      Destination SW3_2 = new DestinationBuilder().setDestNode(new NodeId("openflow:3"))
              .setDestTp(new TpId("openflow:3:2")).build();
      Source SW2_3 = new SourceBuilder().setSourceNode(new NodeId("openflow:2"))
              .setSourceTp(new TpId("openflow:2:3")).build();
      Link l3 = new LinkBuilder().setLinkId(new LinkId("link3"))
              .setDestination(SW3_2)
              .setSource(SW2_3).build();

      List<Node> nodeList = new ArrayList<Node>();
      nodeList.add(node1);
      nodeList.add(node2);
      nodeList.add(sw1);
      nodeList.add(sw2);
      nodeList.add(sw3);

      List<Link> linkList = new ArrayList<Link>();
      linkList.add(l1);
      linkList.add(l2);
      linkList.add(l3);

      Topology topology = new TopologyBuilder().setTopologyId(new TopologyId("flow:1"))
              .setLink(linkList)
              .setNode(nodeList).build();

      altoProvider.processTopology(topology);

      TypedIpv4Address ipv4_1 = new TypedIpv4Address("ipv4:10.0.0.1");
      TypedEndpointAddress tea1 = new TypedEndpointAddress(ipv4_1);

      List<TypedEndpointAddress> teaList1 = new ArrayList<TypedEndpointAddress>();
      teaList1.add(tea1);

      TypedIpv4Address ipv4_2 = new TypedIpv4Address("ipv4:10.0.0.2");
      TypedEndpointAddress tea2 = new TypedEndpointAddress(ipv4_2);

      List<TypedEndpointAddress> teaList2 = new ArrayList<TypedEndpointAddress>();
      teaList2.add(tea2);

      DstCosts1 dc1 = new DstCosts1Builder().setCostDefault("1").build();
      DstCosts dc = new DstCostsBuilder().setDst(tea2).addAugmentation(DstCosts1.class, dc1).build();

      List<DstCosts> dcList = new ArrayList<DstCosts>();
      dcList.add(dc);

      EndpointCostMap ecp = new EndpointCostMapBuilder().setSrc(tea1).setDstCosts(dcList).build();
      List<EndpointCostMap> ecpList = new ArrayList<EndpointCostMap>();
      ecpList.add(ecp);

      Assert.assertEquals(altoProvider.hopcountNumerical(teaList1, teaList2), ecpList);
  }

}
