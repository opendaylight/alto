package org.opendaylight.alto.commons.types.convertor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.MapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.MapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.EndpointAddressType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TagString;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroupKey;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285Type;
import org.opendaylight.alto.commons.types.model150404.ModelType;

public class RFC2ModelConvertor {

    public static final String IPV4 = "ipv4";
    public static final String IPV6 = "ipv6";

    public ModelType.NetworkMap convert(RFC7285Type.NetworkMap nmap) {
        ResourceId rid = new ResourceId(nmap.meta.vtag.rid);
        TagString tag = new TagString(nmap.meta.vtag.tag);
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map> mapData
                = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map>(convert(nmap.map));
        ModelType.NetworkMap result = (ModelType.NetworkMap)(new NetworkMapBuilder()
                                        .setResourceId(rid)
                                        .setTag(tag)
                                        .setMap(mapData).build());
        return result;
    }

    public List<ModelType.NetworkMapData> convert(Map<String, RFC7285Type.Endpoint.AddressGroup> mapData) {
        List<ModelType.NetworkMapData> result = new ArrayList<ModelType.NetworkMapData>();
        for (Map.Entry<String, RFC7285Type.Endpoint.AddressGroup> entry: mapData.entrySet()) {
            String pid = entry.getKey();
            RFC7285Type.Endpoint.AddressGroup addresses = entry.getValue();

            result.add(convert(pid, addresses));
        }
        return result;
    }

    public ModelType.NetworkMapData convert(String pid, RFC7285Type.Endpoint.AddressGroup data) {
        PidName _pid = new PidName(pid);
        return (ModelType.NetworkMapData)new MapBuilder()
                    .setPid(_pid)
                    .setKey(new MapKey(_pid))
                    .setEndpointAddressGroup(convert(data))
                    .build();
    }

    public List<EndpointAddressGroup> convert(RFC7285Type.Endpoint.AddressGroup addressGroup) {
        EndpointAddressGroup result[] = {
            buildAddressGroup(EndpointAddressType.Enumeration.Ipv4, str2IpPrefix(addressGroup.ipv4)),
            buildAddressGroup(EndpointAddressType.Enumeration.Ipv6, str2IpPrefix(addressGroup.ipv6)),
        };

        return Arrays.asList(result); 
    }

    private EndpointAddressGroup buildAddressGroup(EndpointAddressType.Enumeration type, List<IpPrefix> prefixes) {
        EndpointAddressType _type = new EndpointAddressType(type);
        return new EndpointAddressGroupBuilder()
                    .setAddressType(_type)
                    .setEndpointPrefix(prefixes).build();
    }

    private List<IpPrefix> str2IpPrefix(List<String> ipList) {
        List<IpPrefix> prefixList = new ArrayList<IpPrefix>();
        for (String ip : ipList) {
            prefixList.add(IpPrefixBuilder.getDefaultInstance(ip));
        }
        return prefixList;
    }

}
