/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.route.endpointproperty.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendaylight.alto.core.northbound.api.exception.AltoErrorInvalideFieldType;
import org.opendaylight.alto.core.northbound.api.exception.AltoErrorMissingField;
import org.opendaylight.alto.core.northbound.api.exception.AltoErrorSyntax;

import java.io.IOException;

public class EndpointpropertyRouteChecker {

    public static final String FIELD_PROPERTIES = "properties";

    public static void checkMissing(JsonNode target, String field) {
        if (target == null) {
            throw new AltoErrorMissingField(field);
        }
    }

    public static void checkList(JsonNode list, String field) {
        if (!list.isArray()) {
            throw new AltoErrorInvalideFieldType(field);
        }
    }

    public static JsonNode checkJsonSyntax(String content) {
        JsonNode jsonContent;
        ObjectMapper mapper = new ObjectMapper();
        try {
            jsonContent = mapper.readTree(content);
        }
        catch (IOException e) {
            throw new AltoErrorSyntax();
        }
        if (null==jsonContent) {
            throw new AltoErrorSyntax();
        } else if (jsonContent.isNull()) {
            throw new AltoErrorMissingField(AltoNorthboundRouteEndpointproperty.FIELD_PROPERTIES);
        } else {
            return jsonContent;
        }
    }
}
