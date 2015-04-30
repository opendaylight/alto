package org.opendaylight.alto.commons.types.converter;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;

import org.opendaylight.alto.commons.helper.Converter;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint;
import org.opendaylight.alto.commons.types.model150404.ModelEndpoint;

public class RFC2ModelNetworkMapDataConverter
        extends Converter<Map<String, RFC7285Endpoint.AddressGroup>, List<ModelEndpoint>> {

    protected RFC2ModelEndpointAddressGroupConverter conv = new RFC2ModelEndpointAddressGroupConverter();

    public RFC2ModelNetworkMapDataConverter() {
    }

    public RFC2ModelNetworkMapDataConverter(Map<String, RFC7285Endpoint.AddressGroup> _in) {
        super(_in);
    }

    @Override
    public Object _convert() {
        List<ModelEndpoint> out = new LinkedList<ModelEndpoint>();

        for (Map.Entry<String, RFC7285Endpoint.AddressGroup> rep: in().entrySet()) {
            ModelEndpoint mep = new ModelEndpoint();
            mep.setJSONPid(rep.getKey());
            mep.setJSONEndpointAddressGroup(conv.convert(rep.getValue()));
            out.add(mep);
        }
        return out;
    }
}
