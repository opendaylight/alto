package org.opendaylight.alto.commons.types.rfc7285;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.EndpointAddressType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroupKey;

public class Endpoint {
  
  
  public static class AddressGroup extends Extensible {

    @JsonIgnore
    public static final String IPV4_LABEL = "ipv4";
    
    @JsonIgnore
    public static final String IPV6_LABEL = "ipv6";
    
    @JsonProperty(IPV4_LABEL)
    public List<String> ipv4 = new ArrayList<String>();

    @JsonProperty(IPV6_LABEL)
    public List<String> ipv6 = new ArrayList<String>();

    public AddressGroup() {}

    public AddressGroup(List<EndpointAddressGroup> base) {
      for (EndpointAddressGroup group : base) {
        switch (group.getAddressType().getEnumeration()) {
        case Ipv4:
          this.ipv4 = asStringIpPrefixList(EndpointAddressType.Enumeration.Ipv4, group.getEndpointPrefix());
          break;
        case Ipv6:
          this.ipv6 = asStringIpPrefixList(EndpointAddressType.Enumeration.Ipv6, group.getEndpointPrefix());
          break;
        default:
          break;
        }
      }
    }

    private List<String> asStringIpPrefixList(EndpointAddressType.Enumeration type, List<IpPrefix> base) {
      List<String> addressPrefixes = new ArrayList<String>();
      for (IpPrefix prefix : base) {
        switch (type) {
        case Ipv4:
          addressPrefixes.add(prefix.getIpv4Prefix().getValue());
          break;
        case Ipv6:
          addressPrefixes.add(prefix.getIpv6Prefix().getValue());
          break;
        default:
          break;
        }
      }
      return addressPrefixes;
    }
    
    public List<EndpointAddressGroup> asYangEndpointAddressGroupList() {
      List<EndpointAddressGroup> endpointAddressGroup = new ArrayList<EndpointAddressGroup>();
      if (this.ipv4.size() > 0) {
        endpointAddressGroup.add(asYangEndpointAddressGroup(IPV4_LABEL, this.ipv4));
      }
      if (this.ipv6.size() > 0) {
        endpointAddressGroup.add(asYangEndpointAddressGroup(IPV6_LABEL, this.ipv6));
      }
      return endpointAddressGroup; 
    }

    private EndpointAddressGroup asYangEndpointAddressGroup(String key, List<String> ipList){  
      EndpointAddressType type = null;
      if (IPV4_LABEL.equals(key)) {
        type = new EndpointAddressType(EndpointAddressType.Enumeration.Ipv4);
      } else if (IPV6_LABEL.equals(key)) {
        type = new EndpointAddressType(EndpointAddressType.Enumeration.Ipv6);
      }
      
      return new EndpointAddressGroupBuilder().setAddressType(type)
          .setKey(new EndpointAddressGroupKey(type))
          .setEndpointPrefix(asYangIpPrefix(ipList))
          .build();
    }

    private List<IpPrefix> asYangIpPrefix(List<String> ipList) {
      List<IpPrefix> ipPrefixList = new ArrayList<IpPrefix>();
      for (String ip : ipList) {
        ipPrefixList.add(IpPrefixBuilder.getDefaultInstance(ip));
      }
      return ipPrefixList;
    }
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
    public Map<String, Map<String, Object>> answer = new LinkedHashMap<String, Map<String, Object>>();
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
    public Map<String, Map<String, Object>> answer = new LinkedHashMap<String, Map<String, Object>>();
  }
}
