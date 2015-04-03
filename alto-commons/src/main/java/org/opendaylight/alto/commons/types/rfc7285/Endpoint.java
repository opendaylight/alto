package org.opendaylight.alto.commons.types.rfc7285;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

public class Endpoint {

    public static class AddressGroup extends Extensible {

        @JsonProperty("ipv4")
        public List<String> ipv4 = new ArrayList<String>();

        @JsonProperty("ipv6")
        public List<String> ipv6 = new ArrayList<String>();

    }

    public static class PropertyRequest {

        @JsonProperty("properties")
        public List<String> properties;

        @JsonProperty("endpoints")
        public List<String> endpoints;
    }

    public static class PropertyRespond {

        public static class Meta extends Extensible {

            @JsonProperty("dependent-vtags")
            public List<VersionTag> netmap_tags;

        }

        @JsonProperty("meta")
        public Meta meta;

        @JsonProperty("endpoint-properties")
        public Map<String, Map<String, Object>> answer
                            = new LinkedHashMap<String, Map<String, Object>>();
    }

    public static class CostRequest {

        @JsonProperty("cost-type")
        public CostType costType;

        @JsonProperty("endpoints")
        public QueryPairs endpoints;
    }

    public static class CostRespond {

        public static class Meta extends Extensible {

            @JsonProperty("cost-type")
            public CostType costType;

        }

        @JsonProperty("meta")
        public Meta meta;

        @JsonProperty("endpoint-cost-map")
        public Map<String, Map<String, Object>> answer
                            = new LinkedHashMap<String, Map<String, Object>>();
    }
}
