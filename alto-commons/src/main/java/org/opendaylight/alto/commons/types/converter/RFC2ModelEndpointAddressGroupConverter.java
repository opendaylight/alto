package org.opendaylight.alto.commons.types.converter;

import java.util.List;
import java.util.LinkedList;

import org.opendaylight.alto.commons.helper.Converter;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint;
import org.opendaylight.alto.commons.types.model150404.ModelEndpointAddressGroup;

public class RFC2ModelEndpointAddressGroupConverter
        extends Converter<RFC7285Endpoint.AddressGroup, List<ModelEndpointAddressGroup>> {

    public RFC2ModelEndpointAddressGroupConverter() {
    }

    public RFC2ModelEndpointAddressGroupConverter(RFC7285Endpoint.AddressGroup _in) {
        super(_in);
    }

    @Override
    protected Object _convert() {
        List<ModelEndpointAddressGroup> out = new LinkedList<ModelEndpointAddressGroup>();
        if ((in().ipv4 != null) && (!in().ipv4.isEmpty())) {
            ModelEndpointAddressGroup v4 = new ModelEndpointAddressGroup();
            v4.setJSONAddressType(ModelEndpointAddressGroup.IPV4);
            v4.setJSONEndpointPrefix(in().ipv4);
            out.add(v4);
        }
        if ((in().ipv6 != null) && (!in().ipv6.isEmpty())) {
            ModelEndpointAddressGroup v6 = new ModelEndpointAddressGroup();
            v6.setJSONAddressType(ModelEndpointAddressGroup.IPV6);
            v6.setJSONEndpointPrefix(in().ipv6);
            out.add(v6);
        }
        return out;
    }
}
