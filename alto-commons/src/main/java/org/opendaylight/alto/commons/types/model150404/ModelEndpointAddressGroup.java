package org.opendaylight.alto.commons.types.model150404;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.EndpointAddressType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroupKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefixBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ModelEndpointAddressGroup implements EndpointAddressGroup {

    public static final String IPV4 = "ipv4";
    public static final String IPV6 = "ipv6";

    @JsonProperty("alto-service:address-type")
    public String type;

    @JsonProperty("alto-service:endpoint-prefix")
    public List<String> prefixes = new LinkedList<String>();

    @JsonIgnore
    @Override
    public Class<EndpointAddressGroup> getImplementedInterface() {
            return EndpointAddressGroup.class;
    }

    @JsonIgnore
    @Override
    public EndpointAddressType getAddressType() {
        switch (type) {
        case IPV4:
          return new EndpointAddressType(EndpointAddressType.Enumeration.Ipv4);
        case IPV6:
          return new EndpointAddressType(EndpointAddressType.Enumeration.Ipv6);
         default:
          throw new RuntimeException("Unsupported endpoint address type");
        }
    }

    @JsonIgnore
    @Override
    public List<IpPrefix> getEndpointPrefix() {
        List<IpPrefix> resultPrefixes = new ArrayList<IpPrefix>();
        for (String prefix : this.prefixes) {
          resultPrefixes.add(IpPrefixBuilder.getDefaultInstance(prefix));
        }        
        return resultPrefixes;
    }

    @JsonIgnore
    @Override
    public EndpointAddressGroupKey getKey() {
        return new EndpointAddressGroupKey(getAddressType());
    }

    @JsonIgnore
    @Override
    public <E extends Augmentation<EndpointAddressGroup>> E getAugmentation(Class<E> augmentationType) {
        return null;
    }
}
