package org.opendaylight.alto.commons.types.rfc7285;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cost Map: defined in RFC7285 secion 11.2.3
 * */
public class CostMap {

    public static class Meta extends Extensible {

        @JsonProperty("dependent-vtags")
        public List<VersionTag> netmap_tags;

        @JsonProperty("cost-type")
        public CostType costType;
    }

    /**
     * for filtered-cost-map service, RFC7285 secion 11.3.2
     * */
    public static class Filter {

        @JsonProperty("cost-type")
        public CostType costType;

        @JsonProperty("pids")
        public QueryPairs pids;
    }

    @JsonProperty("meta")
    public Meta meta;

    @JsonProperty("cost-map")
    public Map<String, Map<String, Object>> map
                        = new LinkedHashMap<String, Map<String, Object>>();

}
