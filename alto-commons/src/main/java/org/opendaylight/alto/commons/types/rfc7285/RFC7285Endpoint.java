/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.rfc7285;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;


public class RFC7285Endpoint {

    public static class AddressGroup extends Extensible {

        @JsonIgnore
        public static final String IPV4_LABEL = "ipv4";

        @JsonIgnore
        public static final String IPV6_LABEL = "ipv6";

        @JsonProperty(IPV4_LABEL)
        public List<String> ipv4 = new ArrayList<String>();

        @JsonProperty(IPV6_LABEL)
        public List<String> ipv6 = new ArrayList<String>();

    }

    public static class PropertyRequest {

        @JsonProperty("properties")
        public List<String> properties = new ArrayList<String>();

        @JsonProperty("endpoints")
        public List<String> endpoints = new ArrayList<String>();
    }

    public static class PropertyResponse {

        public static class Meta extends Extensible {

            @JsonProperty("dependent-vtags")
            public List<RFC7285VersionTag> netmap_tags = new ArrayList<RFC7285VersionTag>();

        }

        @JsonProperty("meta")
        public Meta meta = new Meta();

        @JsonProperty("endpoint-properties")
        public Map<String, Map<String, Object>> answer
                            = new LinkedHashMap<String, Map<String, Object>>();
    }

    public static class CostRequest {

        @JsonProperty("cost-type")
        public RFC7285CostType costType = new RFC7285CostType();

        @JsonProperty("endpoints")
        public RFC7285QueryPairs endpoints = new RFC7285QueryPairs();
    }

    public static class CostResponse {

        public static class Meta extends Extensible {

            @JsonProperty("cost-type")
            public RFC7285CostType costType = new RFC7285CostType();

        }

        @JsonProperty("meta")
        public Meta meta = new Meta();

        @JsonProperty("endpoint-cost-map")
        public Map<String, Map<String, Object>> answer
                            = new LinkedHashMap<String, Map<String, Object>>();
    }
}
