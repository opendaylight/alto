/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.converter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.opendaylight.alto.commons.helper.Converter;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285EndpointPropertyMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285EndpointPropertyMap.Meta;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;

import com.fasterxml.jackson.databind.JsonNode;

public class YANGJSON2RFCEndpointPropMapConverter
    extends Converter<JsonNode, RFC7285EndpointPropertyMap> {
    public YANGJSON2RFCEndpointPropMapConverter() {
    }

    public YANGJSON2RFCEndpointPropMapConverter(JsonNode _in) {
        super(_in);
    }

    @Override
    public Object _convert() {
        JsonNode node = this.in();
        RFC7285EndpointPropertyMap out = new RFC7285EndpointPropertyMap();
        out.meta = convertMeta(in().get("meta"));
        out.map = new LinkedHashMap<String, Map<String, String>>();
        JsonNode endpointProperties = node.get("endpointProperties");
        assert !endpointProperties.isArray();
        for (JsonNode endpoint : endpointProperties) {
            String addr = extract_addr(endpoint);
            JsonNode properties = endpoint.get("properties");
            assert properties.isNull();
            Map<String, String> ps = convertProperties(properties);
            out.map.put(addr, ps);
        }
        return out;
    }

    private Map<String, String> convertProperties(JsonNode properties) {
        // TODO Auto-generated method stub
        Map<String, String> ps = new LinkedHashMap<String, String>();
        for (JsonNode propertyType : properties) {
            String type = propertyType.get("propertyType").get("value").asText();
            String property = propertyType.get("property").get("value").asText();
            ps.put(type, property);
        }
        return ps;
    }

    private String extract_addr(JsonNode endpoint) {
        // TODO Auto-generated method stub
        return endpoint.get("endpoint").get("value").asText();
    }

    private Meta convertMeta(JsonNode jsonNode) {
        // TODO Auto-generated method stub
        Meta meta = new Meta();
        meta.netmap_tags = new LinkedList<RFC7285VersionTag>();

        for (JsonNode vtag : jsonNode.get("dependentVtags")) {
            String resource_id = vtag.get("resourceId").get("value").asText();
            String tag = vtag.get("tag").get("value").asText();
            meta.netmap_tags.add(new RFC7285VersionTag(resource_id, tag));
        }
        return meta;
    }
}