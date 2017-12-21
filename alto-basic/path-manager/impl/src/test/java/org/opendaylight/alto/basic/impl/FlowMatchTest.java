/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.alto.basic.helper.PathManagerHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.Protocol;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.path.manager.path.FlowDesc;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.path.manager.path.FlowDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;

public class FlowMatchTest {

    private boolean isStringNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    private boolean isPortValid(int portNumber) {
        return portNumber >= 0 && portNumber <= 65535;
    }

    private FlowDesc getRuleAltoFlowDesc(String srcIp, String dstIp, int srcPort, int dstPort,
            Protocol protocol, String srcMac, String dstMac) {
        MatchBuilder builder = new MatchBuilder();

        if (isStringNotEmpty(srcIp) || isStringNotEmpty(dstIp)) {
            Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
            if (isStringNotEmpty(srcIp)) {
                ipv4MatchBuilder.setIpv4Source(new Ipv4Prefix(srcIp));
            }
            if (isStringNotEmpty(dstIp)) {
                ipv4MatchBuilder.setIpv4Destination(new Ipv4Prefix(dstIp));
            }
            builder.setLayer3Match(ipv4MatchBuilder.build());
        }

        if (isStringNotEmpty(srcMac) || isStringNotEmpty(dstMac)) {
            EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
            if (isStringNotEmpty(srcMac)) {
                ethernetMatchBuilder.setEthernetSource(
                        new EthernetSourceBuilder()
                                .setAddress(new MacAddress(srcMac))
                                .build());
            }
            if (isStringNotEmpty(dstMac)) {
                ethernetMatchBuilder.setEthernetDestination(
                        new EthernetDestinationBuilder()
                                .setAddress(new MacAddress(dstMac))
                                .build());
            }
            builder.setEthernetMatch(ethernetMatchBuilder.build());
        }

        if (protocol != null) {
            if (protocol == Protocol.Tcp) {
                TcpMatchBuilder tcpMatchBuilder = new TcpMatchBuilder();
                if (isPortValid(srcPort)) {
                    tcpMatchBuilder.setTcpSourcePort(new PortNumber(srcPort));
                }
                if (isPortValid(dstPort)) {
                    tcpMatchBuilder.setTcpDestinationPort(new PortNumber(dstPort));
                }
                builder.setLayer4Match(tcpMatchBuilder.build());
            } else if (protocol == Protocol.Udp) {
                UdpMatchBuilder udpMatchBuilder = new UdpMatchBuilder();
                if (isPortValid(srcPort)) {
                    udpMatchBuilder.setUdpSourcePort(new PortNumber(srcPort));
                }
                if (isPortValid(dstPort)) {
                    udpMatchBuilder.setUdpDestinationPort(new PortNumber(dstPort));
                }
                builder.setLayer4Match(udpMatchBuilder.build());
            } else if (protocol == Protocol.Sctp) {
                SctpMatchBuilder sctpMatchBuilder = new SctpMatchBuilder();
                if (isPortValid(srcPort)) {
                    sctpMatchBuilder.setSctpSourcePort(new PortNumber(srcPort));
                }
                if (isPortValid(dstPort)) {
                    sctpMatchBuilder.setSctpDestinationPort(new PortNumber(dstPort));
                }
                builder.setLayer4Match(sctpMatchBuilder.build());
            }
        }

        return PathManagerHelper.toAltoFlowDesc(builder.build());
    }

    private FlowDesc getTestAltoFlowDesc(String srcIp, String dstIp, int srcPort, int dstPort,
            Protocol protocol, String srcMac, String dstMac) {
        FlowDescBuilder builder = new FlowDescBuilder();

        if (isStringNotEmpty(srcIp)) {
            builder.setSrcIp(new Ipv4Prefix(srcIp));
        }
        if (isStringNotEmpty(dstIp)) {
            builder.setDstIp(new Ipv4Prefix(dstIp));
        }
        if (isStringNotEmpty(srcMac)) {
            builder.setSrcMac(new MacAddress(srcMac));
        }
        if (isStringNotEmpty(dstMac)) {
            builder.setDstMac(new MacAddress(dstMac));
        }
        if (protocol != null) {
            builder.setProtocol(protocol);
            if (isPortValid(srcPort)) {
                builder.setSrcPort(new PortNumber(srcPort));
            }
            if (isPortValid(dstPort)) {
                builder.setDstPort(new PortNumber(dstPort));
            }
        }

        return builder.build();
    }

    @Test
    public void Test1() {
        FlowDesc flow1_rule = getRuleAltoFlowDesc("192.168.1.0/24", "10.0.2.0/15",
                20, 30, Protocol.Tcp,
                "12:23:34:34:67:89", "");
        FlowDesc flow1_test = getTestAltoFlowDesc("192.168.1.230/32", "10.0.2.0/16",
                20, 30, Protocol.Tcp,
                "12:23:34:34:67:89",
                "23:56:67:23:33:22");
        Assert.assertEquals(PathManagerHelper.isFlowMatch(flow1_rule, flow1_test), true);
    }

    @Test
    public void Test2() {
        FlowDesc flow1_rule = getRuleAltoFlowDesc("192.168.1.0/24", "10.0.2.0/16",
                20, 30, Protocol.Udp,
                "12:23:34:34:67:89", "");
        FlowDesc flow1_test = getTestAltoFlowDesc("192.168.1.230/32", "10.0.2.0/15",
                20, 30, Protocol.Udp,
                "12:23:34:34:67:89", "");

        Assert.assertEquals(PathManagerHelper.isFlowMatch(flow1_rule, flow1_test), false);
    }

    @Test
    public void Test3() {
        FlowDesc flow1_rule = getRuleAltoFlowDesc("192.168.1.0/24", "10.3.4.1/16",
                20, 30, null,
                "11:11:11:11:11:11", "");
        FlowDesc flow1_test = getTestAltoFlowDesc("192.168.1.230/32", "10.3.34.2/24",
                20, 30, Protocol.Tcp,
                "11:11:11:11:11:11", "");

        Assert.assertEquals(PathManagerHelper.isFlowMatch(flow1_rule, flow1_test), true);
    }

    @Test
    public void Test4() {
        FlowDesc flow1_rule = getRuleAltoFlowDesc("20.2.2.2/24", "10.3.4.1/16",
                20, 30, null,
                "11:11:11:11:11:11", "");
        FlowDesc flow1_test = getTestAltoFlowDesc("192.168.1.230/32", "10.3.34.2/24",
                20, 30, Protocol.Tcp,
                "12:23:34:34:67:89", "");

        Assert.assertEquals(PathManagerHelper.isFlowMatch(flow1_rule, flow1_test), false);
    }

    @Test
    public void Test5() {
        FlowDesc flow1_rule = getRuleAltoFlowDesc("192.168.1.22/24", "10.3.4.1/16",
                40, 30, Protocol.Sctp,
                "11:11:11:11:11:11", "");
        FlowDesc flow1_test = getTestAltoFlowDesc("192.168.1.230/32", "10.3.34.2/24",
                20, 30, Protocol.Sctp,
                "12:23:34:34:67:89", "");

        Assert.assertEquals(PathManagerHelper.isFlowMatch(flow1_rule, flow1_test), false);
    }

    @Test
    public void Test6() {
        FlowDesc flow1_rule = getRuleAltoFlowDesc("192.168.1.22/24", "10.3.4.1/16",
                20, 30, null,
                "23:34:55:34:33:33", "");
        FlowDesc flow1_test = getTestAltoFlowDesc("192.168.1.230/32", "10.3.34.2/24",
                20, 30, Protocol.Sctp,
                "11:11:11:11:11:11", "");

        Assert.assertEquals(PathManagerHelper.isFlowMatch(flow1_rule, flow1_test), false);
    }

}
