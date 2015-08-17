package org.opendaylight.alto.commons.types.converter;

import org.opendaylight.alto.commons.helper.Converter;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint;

import com.fasterxml.jackson.databind.JsonNode;

public class YANGJSON2RFCNetworkMapConverter extends Converter<JsonNode, RFC7285NetworkMap> {
    public YANGJSON2RFCNetworkMapConverter() {
    }

    public YANGJSON2RFCNetworkMapConverter(JsonNode _in) {
        super(_in);
    }

    protected YANGJSON2RFCAddressGroupConverter group_converter
                = new YANGJSON2RFCAddressGroupConverter();

    @Override
    protected Object _convert() {
        JsonNode node = this.in();
        RFC7285NetworkMap nm = new RFC7285NetworkMap();

        String resource_id = node.get("resourceId").get("value").asText();
        String tag = node.get("tag").get("value").asText();

        nm.meta.vtag = new RFC7285VersionTag(resource_id, tag);

        JsonNode map = node.get("map");
        assert map.isArray();
        for (JsonNode egroup: map) {
            String pid = extract_pid(egroup);
            JsonNode addr_group = egroup.get("endpointAddressGroup");
            assert !addr_group.isNull();
            RFC7285Endpoint.AddressGroup ag = group_converter.convert(addr_group);

            nm.map.put(pid, ag);
        }
        return nm;
    }

    protected String extract_pid(JsonNode node) {
        return node.get("pid").get("value").asText();
    }
}
