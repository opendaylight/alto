package org.opendaylight.alto.commons.types.mapper;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.alto.commons.types.rfc7285.AltoNetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.Endpoint;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.MapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.MapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.EndpointAddressType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TagString;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroupKey;

public class AltoYangMapper {
  public static final String IPV4 = "ipv4";
  public static final String IPV6 = "ipv6";
  
  public AltoYangMapper() {};
  
  public NetworkMap asNetworkMap(AltoNetworkMap base) {
    ResourceId rid = new ResourceId(base.meta.vtag.rid);
    
    NetworkMapBuilder networkMapBuilder =  new NetworkMapBuilder();
    networkMapBuilder.setTag(new TagString(base.meta.vtag.tag));
    networkMapBuilder.setResourceId(rid);
    networkMapBuilder.setKey(new NetworkMapKey(rid));
    networkMapBuilder.setMap(asNetworkMapDataList(base.map));
    
    return networkMapBuilder.build();
  }

  public List<Map> asNetworkMapDataList(java.util.Map<String, Endpoint.AddressGroup> mapData) {
    List<Map> resultMapData = new ArrayList<Map>();
    for (String key : mapData.keySet()) {
      resultMapData.add(asNetworkMapData(key, mapData.get(key)));
    }
    return resultMapData;
  }
  
  public Map asNetworkMapData(String key, Endpoint.AddressGroup data) {
    PidName pid = new PidName(key);
    return new MapBuilder()
        .setPid(pid)
        .setKey(new MapKey(pid))
        .setEndpointAddressGroup(asEndpointAddressGroupList(data))
        .build();
  }

  public List<EndpointAddressGroup> asEndpointAddressGroupList(Endpoint.AddressGroup addressGroup) {
    List<EndpointAddressGroup> endpointAddressGroup = new ArrayList<EndpointAddressGroup>();
    java.util.Map<String, Object> addressGroupMap = addressGroup.any();
    for (String key : addressGroupMap.keySet()) {
      endpointAddressGroup.add(constructEndpointAddressGroupElement(key, addressGroupMap));
    }
    return endpointAddressGroup; 
  }

  @SuppressWarnings("unchecked")
  private EndpointAddressGroup constructEndpointAddressGroupElement(String key, java.util.Map<String, Object> addressGroupMap){  
    EndpointAddressGroupBuilder addressGroupBuilder = new EndpointAddressGroupBuilder();
    EndpointAddressType type = null;
    if (IPV4.equals(key)) {
      type = new EndpointAddressType(EndpointAddressType.Enumeration.Ipv4);
    } else if (IPV6.equals(key)) {
      type = new EndpointAddressType(EndpointAddressType.Enumeration.Ipv6);
    }
    
    addressGroupBuilder.setAddressType(type);
    addressGroupBuilder.setKey(new EndpointAddressGroupKey(type));
    addressGroupBuilder.setEndpointPrefix(constructIpPrefixList((List<String>)addressGroupMap.get(key)));
    return addressGroupBuilder.build();
  }

  private List<IpPrefix> constructIpPrefixList(List<String> ipList) {
    List<IpPrefix> ipPrefixList = new ArrayList<IpPrefix>();
    for (String ip : ipList) {
      ipPrefixList.add(IpPrefixBuilder.getDefaultInstance(ip));
    }
    return ipPrefixList;
  }
}
