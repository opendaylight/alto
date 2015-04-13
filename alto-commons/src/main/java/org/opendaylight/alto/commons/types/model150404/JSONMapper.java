package org.opendaylight.alto.commons.types.model150404;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285Type;
import org.opendaylight.alto.commons.types.convertor.RFC2ModelConvertor;
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

public class JSONMapper {

    private RFC7285Type.JSONMapper rfc7285Mapper = new RFC7285Type.JSONMapper();
    private RFC2ModelConvertor convertor = new RFC2ModelConvertor();

    public NetworkMap asNetworkMap(String rfc7285json) throws Exception {
        return (NetworkMap)convertor.convert((RFC7285Type.NetworkMap)rfc7285Mapper.asNetworkMap(rfc7285json));
    }

}
