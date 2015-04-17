package org.opendaylight.alto.commons.types.model150404;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.EndpointAddressType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroupKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefixBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

import java.util.List;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ModelEndpointAddressGroup implements EndpointAddressGroup {

    public static final String IPV4 = "ipv4";
    public static final String IPV6 = "ipv6";


    @JsonIgnore
    protected EndpointAddressType type;

    @JsonIgnore
    protected List<IpPrefix> data = new LinkedList<IpPrefix>();

    @JsonIgnore
    protected List<String> prefixes = new LinkedList<String>();

    public ModelEndpointAddressGroup() {
    }

    public ModelEndpointAddressGroup(EndpointAddressGroup model) {
    }

    @JsonIgnore
    @Override
    public Class<EndpointAddressGroup> getImplementedInterface() {
            return EndpointAddressGroup.class;
    }

    @JsonIgnore
    @Override
    public EndpointAddressType getAddressType() {
        return type;
    }

    @JsonProperty("alto-service:address-type")
    public String getJSONAddressType() {
        if (type == null)
            return null;
        if (EndpointAddressType.Enumeration.Ipv4.equals(type.getEnumeration()))
            return IPV4;
        if (EndpointAddressType.Enumeration.Ipv6.equals(type.getEnumeration()))
            return IPV6;
        return "unsupported";
    }

    @JsonProperty
    public void setJSONAddressType(String type) {
        if (IPV4.equals(type))
            this.type = new EndpointAddressType(EndpointAddressType.Enumeration.Ipv4);
        if (IPV6.equals(type))
            this.type = new EndpointAddressType(EndpointAddressType.Enumeration.Ipv6);
        this.type = null;
    }

    @JsonIgnore
    @Override
    public List<IpPrefix> getEndpointPrefix() {
        return data;
    }

    @JsonIgnore
    public void setEndpointPrefix(List<IpPrefix> rhs) {
        data = new LinkedList<IpPrefix>(rhs);
        prefixes = new LinkedList<String>();
        for (IpPrefix p: rhs) {
            prefixes.add(p.getValue().toString());
        }
    }

    @JsonProperty("alto-service:endpoint-prefix")
    public List<String> getJSONEndpointAddressGroup() {
        return prefixes;
    }

    @JsonProperty("alto-service:endpoint-prefix")
    public void setJSONEndpointPrefix(List<String> rhs) {
        prefixes = rhs;
        data = new LinkedList<IpPrefix>();
        for (String p: rhs) {
            data.add(IpPrefixBuilder.getDefaultInstance(p));
        }
    }

    @JsonIgnore
    @Override
    public EndpointAddressGroupKey getKey() {
        return new EndpointAddressGroupKey(type);
    }

    @JsonIgnore
    @Override
    public <E extends Augmentation<EndpointAddressGroup>> E getAugmentation(Class<E> augmentationType) {
        return null;
    }
}
