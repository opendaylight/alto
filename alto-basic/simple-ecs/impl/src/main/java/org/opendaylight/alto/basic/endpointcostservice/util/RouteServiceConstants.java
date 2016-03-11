/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.util;

public class RouteServiceConstants {
    public static class IP_PROTOCOL {
        public static short ICMP4 = 1;
        public static short ICMP6 = 58;
        public static short TCP4 = 6;
        public static short TCP6 = 6;
        public static short UDP4 = 17;
        public static short UDP6 = 17;
        public static short SCTP4 = 132;
        public static short SCTP6 = 132;
    }

    public static class ETHERNET_TYPE {
        public static Long IPV4 = (long) 0x0800;
        public static Long IPV6 = (long) 0x86dd;
        public static Long LLDP = (long) 0x88cc;
        public static Long ARP = (long) 0x0806;
        public static Long RARP = (long) 0x8035;
    }
}
