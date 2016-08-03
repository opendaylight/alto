/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.flow;

import org.opendaylight.alto.basic.endpointcostservice.helper.IPPrefixHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.TypedAddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6ExtHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6Label;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagsMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Tunnel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.protocol.match.fields.Pbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class FlowEntryMatcher {
    private static final Logger log = LoggerFactory
            .getLogger(FlowEntryMatcher.class);

    /**
     * @param match is the original fields should be matched.
     * @param matchFields are the target fields should be compared.
     * @return the result of match.
     */
    public boolean match(Match match, MatchFields matchFields) {
        log.info("Flow Entry Matching Start");
        return (match == null) ||
                ((matchInPort(match.getInPort(), matchFields)
                        && matchInPhyPort(match.getInPhyPort(), matchFields)
                        && matchIp(match.getIpMatch(), matchFields)
                        && matchVlan(match.getVlanMatch(), matchFields)
                        && matchTunnel(match.getTunnel(), matchFields)
                        && matchMetadata(match.getMetadata(), matchFields)
                        && matchEthernet(match.getEthernetMatch(), matchFields)
                        && matchLayer3(match.getLayer3Match(), matchFields)
                        && matchLayer4(match.getLayer4Match(), matchFields)
                        && matchIcmpv4Match(match.getIcmpv4Match(), matchFields)
                        && matchIcmpv6Match(match.getIcmpv6Match(), matchFields)
                        && matchProtocolMatchFields(match.getProtocolMatchFields(), matchFields)
                        && matchTcpFlag(match.getTcpFlagsMatch(), matchFields)));
    }

    /**
     * Match if port is equal.
     * @param port is the port filed in a flow entry.
     * @param matchFields are the target fields should be compared.
     * @return the result of match.
     */
    public boolean matchInPort(NodeConnectorId port, MatchFields matchFields) {
        if (port == null) return true;
        String flowInPort = port.getValue();
        return flowInPort.equals(matchFields.inPort);
    }

    /**
     * Match if port is a physical port.
     * @param port is the port field in a flow entry.
     * @param matchFields are the target fields should be compared.
     * @return the result of match.
     */
    public boolean matchInPhyPort(NodeConnectorId port, MatchFields matchFields) {
        return (port == null);
    }

    /**
     * Match if IP is equal.
     * @param ipMatch is the IP address in the flow entry.
     * @param matchFields are the target fields should be compared.
     * @return the result of match.
     */
    public boolean matchIp(IpMatch ipMatch, MatchFields matchFields) {
        return (ipMatch == null) ||
                (ipMatch.getIpProtocol() == null && ipMatch.getIpDscp() == null
                        && ipMatch.getIpEcn() == null && ipMatch.getIpProto() == null);
    }

    /**
     * Match if VLAN is equal.
     * @param match is the VLAN field in flow entry.
     * @param matchFields are the target fields should be compared.
     * @return the result of match.
     */
    public boolean matchVlan(VlanMatch match, MatchFields matchFields) {
        return (match == null) || (match.getVlanPcp() == null
                && matchVlanId(match.getVlanId()));
    }

    private boolean matchVlanId(VlanId id) {
        return (id == null) || (!id.isVlanIdPresent());
    }

    /**
     * Match if tunnel is equal.
     * @param tunnel is the tunnel field in flow entry.
     * @param matchFields are the target fields should be compared.
     * @return the result of match.
     */
    public boolean matchTunnel(Tunnel tunnel, MatchFields matchFields) {
        return (tunnel == null) ||
                (tunnel.getTunnelId() == null && tunnel.getTunnelMask() == null);
    }

    /**
     * Match if metadata is equal.
     * @param meta is the metadata in flow entry.
     * @param matchFields are the target fields should be compared.
     * @return the result of match.
     */
    public boolean matchMetadata(Metadata meta, MatchFields matchFields) {
        return (meta == null) ||
                (meta.getMetadata() == null && meta.getMetadataMask() == null);
    }

    /**
     * Match if ethernet is equal.
     * @param match is the ethernet in flow entry.
     * @param matchFields are the target fields should be compared.
     * @return the result of match.
     */
    public boolean matchEthernet(EthernetMatch match, MatchFields matchFields) {
        return (match == null)
                || (matchEthernetSrc(match.getEthernetSource(), matchFields)
                && matchEthernetDst(match.getEthernetDestination(), matchFields)
                && matchEthernetType(match.getEthernetType(), matchFields));
    }

    private boolean matchEthernetSrc(EthernetSource ethSrc, MatchFields matchFields) {
        return (ethSrc == null) ||
                matchMacAddressWithMask(ethSrc.getAddress(), matchFields.srcMac, ethSrc.getMask());
    }

    private boolean matchEthernetDst(EthernetDestination ethDest, MatchFields matchFields) {
        return (ethDest == null) ||
                matchMacAddressWithMask(ethDest.getAddress(), matchFields.dstMac, ethDest.getMask());
    }

    private String normalizeMacAddress(MacAddress mac) {
        return mac.getValue().replaceAll(":|-", "").toLowerCase();
    }

    private long getMaskedMacAddress(String macAddress, String mask) {
        long macLong = (Long.parseLong(macAddress,16));
        long maskLong = Long.parseLong(mask,16);
        return macLong & maskLong;
    }

    /**
     * Match the MAC address with a mask.
     * @param macA MAC address A.
     * @param macB MAC address B.
     * @param mask to XOR with MAC address.
     * @return the result of match.
     */
    public boolean matchMacAddressWithMask(MacAddress macA, MacAddress macB, MacAddress mask) {
        return (mask == null && normalizeMacAddress(macA).equals(normalizeMacAddress(macB)))
                || (mask != null && getMaskedMacAddress(normalizeMacAddress(macA), normalizeMacAddress(mask))
                == getMaskedMacAddress(normalizeMacAddress(macB), normalizeMacAddress(mask)));
    }

    private boolean matchEthernetType(EthernetType ethernetType, MatchFields matchFields) {
        return (ethernetType == null) || (matchFields.ethernetType != null
                && ethernetType.getType().getValue().longValue()
                == matchFields.ethernetType.longValue());
    }

    /**
     * Match if layer 3 routing information is equal.
     * @param match is the layer 3 match.
     * @param matchFields are the target fields should be compared.
     * @return the result of match.
     */
    public boolean matchLayer3(Layer3Match match, MatchFields matchFields) {
        if (match == null) {
            return true;
        } else if (match instanceof Ipv4Match) {
            return matchIpv4((Ipv4Match) match, matchFields);
        } else if (match instanceof Ipv6Match) {
            return matchIpv6((Ipv6Match) match, matchFields);
        } else if (match instanceof ArpMatch) {
            return matchArp((ArpMatch) match, matchFields);
        } else if (match instanceof TunnelIpv4Match) {
            return matchTunnelIpv4((TunnelIpv4Match) match, matchFields);
        }
        return false;
    }

    /**
     * Match by IPv4 address.
     * @param match is the original IPv4 address.
     * @param matchFields are the target fields should be compared.
     * @return the result of match.
     */
    public boolean matchIpv4(Ipv4Match match, MatchFields matchFields) {
        return (match == null) || (matchIpv4Address(match.getIpv4Source(), matchFields.srcIp)
                && matchIpv4Address(match.getIpv4Destination(), matchFields.dstIp));
    }

    /**
     * @param prefix
     * @param address
     * @return the result of match.
     */
    public boolean matchIpv4Address(Ipv4Prefix prefix, TypedAddressData address) {
        try {
            return (prefix == null) || new IPPrefixHelper(prefix).match(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param match
     * @param matchFields
     * @return the result of match.
     */
    public boolean matchIpv6(Ipv6Match match, MatchFields matchFields) {
        return (match == null) || (matchIpv6Address(match.getIpv6Source(), matchFields.srcIp)
                && matchIpv6Address(match.getIpv6Destination(), matchFields.dstIp)
                && matchIpv6ExtHeader(match.getIpv6ExtHeader())
                && matchIpv6Label(match.getIpv6Label())
                && (match.getIpv6NdSll() == null)
                && (match.getIpv6NdTarget() == null)
                && (match.getIpv6NdTll() == null));
    }

    /**
     * @param ipv6ExtHeader
     * @return the result of match.
     */
    public boolean matchIpv6ExtHeader(Ipv6ExtHeader ipv6ExtHeader) {
        return (ipv6ExtHeader == null) ||
                (ipv6ExtHeader.getIpv6Exthdr() == null
                        && ipv6ExtHeader.getIpv6ExthdrMask() == null);
    }

    /**
     * @param ipv6Label
     * @return the result of match.
     */
    public boolean matchIpv6Label(Ipv6Label ipv6Label) {
        return (ipv6Label == null) ||
                (ipv6Label.getIpv6Flabel() == null
                        && ipv6Label.getFlabelMask() == null);
    }

    /**
     * @param prefix
     * @param address
     * @return the result of match.
     */
    public boolean matchIpv6Address(Ipv6Prefix prefix, TypedAddressData address) {
        try {
            return (prefix == null) || new IPPrefixHelper(prefix).match(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Match by ARP protocol.
     * @param match
     * @param matchFields are the target fields should be compared.
     * @return the result of match.
     */
    public boolean matchArp(ArpMatch match, MatchFields matchFields) {
        return (match == null) || (match.getArpOp() == null
                && match.getArpSourceHardwareAddress() == null
                && match.getArpSourceTransportAddress() == null
                && match.getArpTargetHardwareAddress() == null
                && match.getArpTargetTransportAddress() == null);
    }

    /**
     * Match by tunnel IPv4.
     * @param match
     * @param matchFields
     * @return the result of match.
     */
    public boolean matchTunnelIpv4(TunnelIpv4Match match, MatchFields matchFields) {
        return (match == null) ||
                (match.getTunnelIpv4Source() == null
                        && match.getTunnelIpv4Destination() == null);
    }

    /**
     * Match by layer 4 routing information.
     * @param match
     * @param matchFields
     * @return the result of match.
     */
    public boolean matchLayer4(Layer4Match match, MatchFields matchFields) {
        if (match == null) {
            return true;
        } else if (match instanceof UdpMatch) {
            return mathcUdp((UdpMatch) match);
        } else if (match instanceof TcpMatch) {
            return matchTcp((TcpMatch) match);
        } else if (match instanceof SctpMatch) {
            return matchSctp((SctpMatch) match);
        }
        return false;
    }

    /**
     * Match by UDP.
     * @param match
     * @return the result of match.
     */
    public boolean mathcUdp(UdpMatch match) {
        return (match == null) ||
                (match.getUdpSourcePort() == null
                        && match.getUdpDestinationPort() == null);
    }

    /**
     * Match by TCP.
     * @param match
     * @return the result of match.
     */
    public boolean matchTcp(TcpMatch match) {
        return (match == null) ||
                (match.getTcpSourcePort() == null
                        && match.getTcpDestinationPort() == null);
    }

    /**
     * Match by SCTP.
     * @param match
     * @return the result of match.
     */
    public boolean matchSctp(SctpMatch match) {
        return (match == null) ||
                (match.getSctpSourcePort() == null
                        && match.getSctpDestinationPort() == null);
    }

    /**
     * Match by ICMP.
     * @param match
     * @param matchFields
     * @return the result of match.
     */
    public boolean matchIcmpv4Match(Icmpv4Match match, MatchFields matchFields) {
        return (match == null) || (match.getIcmpv4Code() == null
                && match.getIcmpv4Type() == null);
    }

    /**
     * Match by ICMPv6.
     * @param match
     * @param matchFields
     * @return the result of match.
     */
    public boolean matchIcmpv6Match(Icmpv6Match match, MatchFields matchFields) {
        return (match == null) || (match.getIcmpv6Code() == null
                && match.getIcmpv6Type() == null);
    }

    /**
     * Match by multiple protocol fields.
     * @param match
     * @param matchFields
     * @return the result of match.
     */
    public boolean matchProtocolMatchFields(ProtocolMatchFields match, MatchFields matchFields) {
        return (match == null) || (match.getMplsBos() == null
                && match.getMplsLabel() == null
                && match.getMplsTc() == null
                && matchPbb(match.getPbb(), matchFields));
    }

    /**
     * Match by PBB protocol.
     * @param pbb
     * @param matchFields
     * @return the result of match.
     */
    public boolean matchPbb(Pbb pbb, MatchFields matchFields) {
        return (pbb == null) ||
                (pbb.getPbbIsid() == null && pbb.getPbbMask() == null);
    }

    /**
     * Match by TCP flag.
     * @param match
     * @param matchFields
     * @return the result of match.
     */
    public boolean matchTcpFlag(TcpFlagsMatch match, MatchFields matchFields) {
        return (match == null) || (match.getTcpFlags() == null);
    }
}
