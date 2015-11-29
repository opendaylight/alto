/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.northbound.route.networkmap.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wukunheng00 on 12/1/15.
 */
public class RFC7285NetworkMap {
    public static class AddressGroup  {

        @JsonIgnore
        public static final String IPV4_LABEL = "ipv4";

        @JsonIgnore
        public static final String IPV6_LABEL = "ipv6";

        @JsonProperty(IPV4_LABEL)
        public List<String> ipv4 = new ArrayList<String>();

        @JsonProperty(IPV6_LABEL)
        public List<String> ipv6 = new ArrayList<String>();

        public List<String> getIpv4() {
            return ipv4;
        }

        public void setIpv4(List<String> ipv4) {
            this.ipv4 = ipv4;
        }

        public List<String> getIpv6() {
            return ipv6;
        }

        public void setIpv6(List<String> ipv6) {
            this.ipv6 = ipv6;
        }
    }

    public static class Meta   {

        @JsonProperty("vtag")
        public String vtag = new String("this is vtag");

        public String getVtag() {
            return vtag;
        }

        public void setVtag(String vtag) {
            this.vtag = vtag;
        }
    }

    public static class PID{
        String PIDName;

        AddressGroup addressGroup = new AddressGroup();

        public String getPIDName() {
            return PIDName;
        }

        public void setPIDName(String PIDName) {
            this.PIDName = PIDName;
        }

        public AddressGroup getAddressGroup() {
            return addressGroup;
        }

        public void setAddressGroup(AddressGroup addressGroup) {
            this.addressGroup = addressGroup;
        }
    }

    @JsonProperty("meta")
    public Meta meta = new Meta();

    @JsonProperty("network-map")
    public List<PID> networkmap = new ArrayList<PID>();

    public List<PID> getNetworkmap() {
        return networkmap;
    }

    public void setNetworkmap(List<PID> networkmap) {
        this.networkmap = networkmap;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
