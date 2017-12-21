/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.helper;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IPPrefixHelper {
    private static final Logger LOG = LoggerFactory.getLogger(IPPrefixHelper.class);

    public static int ipv4PrefixToIntIPv4Address(Ipv4Prefix ipv4Prefix) throws UnknownHostException{
        int ipv4Address;
        String[] ipString = ipv4Prefix.getValue().split("/");
        Inet4Address inet4Address = (Inet4Address) Inet4Address.getByName(ipString[0]);
        byte[] ipArray = inet4Address.getAddress();
        ipv4Address = ((ipArray[0] & 0xFF) << 24) | ((ipArray[1] & 0xFF) << 16)
                | ((ipArray[2] & 0xFF) << 8) | ((ipArray[3] & 0xFF));

        return ipv4Address;
    }

    public static int ipv4PrefixToIntIPv4Mask(Ipv4Prefix ipv4Prefix) {
        int ipv4Mask;
        String[] value = ipv4Prefix.getValue().split("/");
        int bits = Integer.parseInt(value[1]);
        ipv4Mask = -1 << (32 - bits);

        return ipv4Mask;
    }
}
