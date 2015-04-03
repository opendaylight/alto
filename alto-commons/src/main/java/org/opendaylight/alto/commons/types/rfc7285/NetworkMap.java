package org.opendaylight.alto.commons.types.rfc7285;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Network Map: defined in RFC 7285 section 11.2.1
 * */
public class NetworkMap {

    public static class Meta extends Extensible {

        @JsonProperty("vtag")
        public VersionTag vtag;

    }

    /**
     * used for filtered-network-map, RFC7285 secion 11.3.1
     * */
    public static class Filter {

        @JsonProperty("pids")
        public List<String> pids;

    }

    @JsonProperty("meta")
    public Meta meta;

    @JsonProperty("network-map")
    public Map<String, Endpoint.AddressGroup> map
                    = new LinkedHashMap<String, Endpoint.AddressGroup>();

}
