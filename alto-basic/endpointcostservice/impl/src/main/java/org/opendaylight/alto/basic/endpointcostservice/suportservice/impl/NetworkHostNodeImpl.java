/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.suportservice.impl;

import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.NetworkHostNodeService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.address.tracker.rev140617.address.node.connector.Addresses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.Ipv4AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.Ipv6AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.TypedAddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkHostNodeImpl implements NetworkHostNodeService {
    private static final Logger log = LoggerFactory
            .getLogger(NetworkHostNodeImpl.class);

    private Map<String, Set<String>> ipIndexedHostNode = new ConcurrentHashMap<String, Set<String>>();
    private Map<String, HostNode> hostNodes = new ConcurrentHashMap<String, HostNode>();

    @Override
    public void addHostNode(HostNode node) {
        synchronized (this) {
            this.hostNodes.put(node.getId().getValue(), node);
            addToIpIndexedHostNodeMaps(node);
            log.info("Adding hostnode " + node.getId().getValue());
        }
    }

    private void addToIpIndexedHostNodeMaps(HostNode node) {
        String uniqueIp = uniqueIpAddress(node);
        if (!this.ipIndexedHostNode.containsKey(uniqueIp)) {
            Set<String> linkIdSet = new HashSet<String>();
            ipIndexedHostNode.put(uniqueIp, linkIdSet);
        }
        ipIndexedHostNode.get(uniqueIp).add(node.getId().getValue());
    }

    private String uniqueIpAddress(HostNode node) {
        Addresses address = node.getAddresses().get(0);
        return touniqueIpAddress(address.getIp());
    }

    private String touniqueIpAddress(IpAddress ipAddress) {
        Ipv4Address ipv4 = ipAddress.getIpv4Address();
        if (ipv4 != null) {
            return ipv4.getValue();
        }
        Ipv6Address ipv6 = ipAddress.getIpv6Address();
        if (ipv6 != null) {
            return uniqueIpv6String(ipv6.getValue());
        }
        return null;
    }
    private static String uniqueIpv6String(String ipv6) {
        try {
            return ipv6ToUniqueString(ipv6);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String ipv6ToUniqueString(String address) throws UnknownHostException {
        byte[] b = toBytes(address);
        return toHexString(b);
    }

    private static byte[] toBytes(String address) throws UnknownHostException {
        Inet6Address addr = (Inet6Address) InetAddress.getByName(address);
        return addr.getAddress();
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    @Override
    public void deleteHostNode(HostNode node) {
        String uniqueId = uniqueIpAddress(node);
        synchronized (this) {
            this.ipIndexedHostNode.remove(uniqueId);
            removeFromIpIndexedHostNodeMaps(node);
            log.info("Removing host node " + node.getId().getValue());
        }

    }

    private void removeFromIpIndexedHostNodeMaps(HostNode node) {
        String uniqueIp = uniqueIpAddress(node);
        if (this.ipIndexedHostNode.containsKey(uniqueIp)) {
            ipIndexedHostNode.get(uniqueIp).remove(node.getId().getValue());
        }
    }

    @Override
    public HostNode getHostNodeByHostIP(TypedAddressData ip) {
        return getHostNodes(ip).get(0);
    }

    private List<HostNode> getHostNodes(TypedAddressData ip) {
        String uniqueIp = touniqueIpAddress(ip);
        List<HostNode> hosts = new ArrayList<HostNode>();
        if (ipIndexedHostNode.containsKey(uniqueIp)) {
            for (String hostId : ipIndexedHostNode.get(uniqueIp)) {
                hosts.add(getHostNodeByHostId(hostId));
            }
        }
        return hosts;
    }

    @Override
    public HostNode getHostNodeByHostId(String hostId) {
        return hostNodes.get(hostId);
    }

    @Override
    public boolean isValidHost(TypedAddressData ip) {
        return this.ipIndexedHostNode.containsKey(touniqueIpAddress(ip));
    }

    public static String touniqueIpAddress(TypedAddressData ipAddress) {
        if (ipAddress.getAddress() instanceof Ipv4AddressData) {
            return ((Ipv4AddressData) ipAddress.getAddress()).getIpv4().getValue().replaceAll("^ipv4:", "");
        }
        if (ipAddress.getAddress() instanceof Ipv6AddressData) {
            return uniqueIpv6String(
                    ((Ipv6AddressData) ipAddress.getAddress()).getIpv6().
                            getValue().replaceAll("^ipv6:", ""));
        }
        return null;
    }
}
