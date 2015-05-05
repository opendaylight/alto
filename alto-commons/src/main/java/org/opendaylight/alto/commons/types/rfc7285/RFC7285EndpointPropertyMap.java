package org.opendaylight.alto.commons.types.rfc7285;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RFC7285EndpointPropertyMap {
  
  public static class Meta extends Extensible {
    @JsonProperty("dependent-vtags")
    public List<RFC7285VersionTag> netmap_tags;
  }
  
  @JsonProperty("meta")
  public Meta meta;
  
  @JsonProperty("endpoint-properties")
  public Map<String, Map<String, String>> map
    = new LinkedHashMap<String, Map<String, String>>(); 
}
