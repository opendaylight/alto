/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.helper;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.alto.commons.types.Subnet;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TypedEndpointAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup;

public class NetworkMapIpPrefixHelper {
    private Map<IpPrefix, PidName> prefixToPID = new HashMap<IpPrefix, PidName>();
    private Map<IpPrefix, Subnet> prefixToSubnet = new HashMap<IpPrefix, Subnet>();
    private boolean initiated = false;

    public void update(NetworkMap networkMap) throws UnknownHostException {
        clear();
        if (networkMap == null) {
            return;
        }

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map map : networkMap
                .getMap()) {
            for (EndpointAddressGroup group : map.getEndpointAddressGroup()) {
                for (IpPrefix prefix : group.getEndpointPrefix()) {
                    this.prefixToPID.put(prefix, map.getPid());
                    this.prefixToSubnet.put(prefix,
                            new Subnet(group.getAddressType(), prefix));
                }
            }
        }
        initiated = true;
    }

    public void clear() {
        this.initiated = false;
        this.prefixToPID.clear();
        this.prefixToSubnet.clear();
    }

    public boolean initiated() {
        return this.initiated;
    }

    public Map<TypedEndpointAddress, PidName> getPIDsByEndpointAddresses(
            List<TypedEndpointAddress> addresses) {
        Map<TypedEndpointAddress, PidName> pids = new HashMap<TypedEndpointAddress, PidName>();
        for (TypedEndpointAddress address : addresses) {
            PidName pid = null;
            try {
                pid = getPIDByEndpointAddress(address);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            pids.put(address, pid);
        }
        return pids;
    }

    public PidName getPIDByEndpointAddress(TypedEndpointAddress address)
            throws UnknownHostException {
        int maxLen = -1;
        PidName pid = null;
        for (IpPrefix prefix : this.prefixToSubnet.keySet()) {
            Subnet sn = this.prefixToSubnet.get(prefix);
            if (sn.match(address) && sn.getSubnetLength() > maxLen) {
                pid = this.prefixToPID.get(prefix);
                maxLen = sn.getSubnetLength();
            }
        }
        return pid;
    }
}