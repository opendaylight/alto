/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.route.costmap.impl;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;

public class CostmapRouteChecker {

    public static Response checkMissing(JsonNode target, String field, String origin) {
        if (target == null) {
            // TODO :: report missing field, something like
            // return new AltoMissingFieldError(field, origin);
            return null;
        }
        return null;
    }

    public static Response checkList(JsonNode list, String field, String origin) {
        if (!list.isArray()) {
            // TODO :: report invalid field type, something like
            // return new AltoInvalidFieldType(field, "array", origin);
            return null;
        }
        return null;
    }

}
