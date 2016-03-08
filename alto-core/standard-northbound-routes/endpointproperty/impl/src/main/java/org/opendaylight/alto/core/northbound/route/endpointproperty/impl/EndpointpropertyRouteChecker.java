/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.route.endpointproperty.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendaylight.alto.core.northbound.api.exception.AltoErrorInvalideFieldType;
import org.opendaylight.alto.core.northbound.api.exception.ALTOErrorMissingField;

public class EndpointpropertyRouteChecker {

    public static void checkMissing(JsonNode target, String field) {
        if (target == null) {
            // TODO :: report missing field, something like
            throw new ALTOErrorMissingField(field);
        }
    }

    public static void checkList(JsonNode list, String field) {
        if (!list.isArray()) {
            throw new AltoErrorInvalideFieldType(field);
        }
    }

}
