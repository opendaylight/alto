/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.model150404;

import java.util.LinkedList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.EndpointAddressType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroupKey;
import org.opendaylight.yangtools.yang.binding.Augmentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelEndpointAddressGroup implements EndpointAddressGroup {

    @JsonIgnore
    public static final String IPV4 = "ipv4";

    @JsonIgnore
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
      if (IPV4.equals(type))
        return new EndpointAddressType(EndpointAddressType.Enumeration.Ipv4);
      if (IPV6.equals(type))
        return new EndpointAddressType(EndpointAddressType.Enumeration.Ipv6);
      throw new java.lang.UnsupportedOperationException("Unsupported AddressType");
    }

    @JsonIgnore
    @Override
    public <E extends Augmentation<EndpointAddressGroup>> E getAugmentation(Class<E> augmentationType) {
      return null;
    }

    @JsonIgnore
    @Override
    public List<IpPrefix> getEndpointPrefix() {
      List<IpPrefix> ipPrefixes = new LinkedList<IpPrefix>();
      for (String prefix : prefixes) {
        ipPrefixes.add(IpPrefixBuilder.getDefaultInstance(prefix));
      }
      return ipPrefixes;
    }

    @JsonIgnore
    @Override
    public EndpointAddressGroupKey getKey() {
      return new EndpointAddressGroupKey(getAddressType());
    }
}
