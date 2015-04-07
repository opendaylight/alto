package org.opendaylight.alto.commons.types.rfc7285;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.MapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.MapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TagString;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Network Map: defined in RFC 7285 section 11.2.1
 * */
public class AltoNetworkMap {

    public AltoNetworkMap() {}

    public AltoNetworkMap(NetworkMap base) {
      this.meta = new AltoNetworkMap.Meta(
          new VersionTag(base.getResourceId().getValue(), base.getTag().getValue()));
      
      this.map = new LinkedHashMap<String, Endpoint.AddressGroup>();
      List<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map> baseMaps = base.getMap();
      for (org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map map : baseMaps) {
        String key = map.getKey().getPid().getValue();
        this.map.put(key, new Endpoint.AddressGroup(map.getEndpointAddressGroup()));
      }
    }

    public static class Meta extends Extensible {
        public Meta() {}
        
        public Meta(VersionTag vtag) {
          this.vtag = vtag;
        }

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
    
    public NetworkMap asYangNetworkMap() {
      ResourceId rid = new ResourceId(this.meta.vtag.rid);
      return new NetworkMapBuilder()
          .setTag(new TagString(this.meta.vtag.tag))
          .setResourceId(rid)
          .setKey(new NetworkMapKey(rid))
          .setMap(asNetworkMapDataList(this.map))
          .build();
    }
    
    private List<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map> asNetworkMapDataList(
        java.util.Map<String, Endpoint.AddressGroup> mapData) {
      List<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map> resultMapData = 
          new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map>();
      for (String key : mapData.keySet()) {
        resultMapData.add(asNetworkMapData(key, mapData.get(key)));
      }
      return resultMapData;
    }
    
    private org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map asNetworkMapData(
        String key, Endpoint.AddressGroup data) {
      PidName pid = new PidName(key);
      return new MapBuilder()
          .setPid(pid)
          .setKey(new MapKey(pid))
          .setEndpointAddressGroup(data.asYangEndpointAddressGroupList())
          .build();
    }

}
