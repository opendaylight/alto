/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.flow;

import org.opendaylight.alto.basic.endpointcostservice.util.RouteServiceConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.Ipv4AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.Ipv6AddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.TypedAddressData;

public class MatchFields {
    public String inPort;
    public TypedAddressData srcIp;
    public TypedAddressData dstIp;
    public MacAddress srcMac;
    public MacAddress dstMac;
    public Long ethernetType;

    public MatchFields(TypedAddressData srcIp, TypedAddressData dstIp) {
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.ethernetType = getEthernetType(srcIp);
    }

    public MatchFields(MacAddress srcMac, MacAddress dstMac,
                       TypedAddressData srcIp, TypedAddressData dstIp) {
        this(srcIp, dstIp);
        this.srcMac = srcMac;
        this.dstMac = dstMac;
    }
    private Long getEthernetType(TypedAddressData Ip) {
        if(Ip.getAddress() instanceof Ipv4AddressData){
            return RouteServiceConstants.ETHERNET_TYPE.IPV4;
        }
        if(Ip.getAddress() instanceof Ipv6AddressData){
            return RouteServiceConstants.ETHERNET_TYPE.IPV6;
        }
        return null;
    }
}
