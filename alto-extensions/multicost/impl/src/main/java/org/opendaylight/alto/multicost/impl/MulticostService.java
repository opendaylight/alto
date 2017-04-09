/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.multicost.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.opendaylight.alto.core.northbound.api.exception.AltoBadFormatException;
import org.opendaylight.alto.core.northbound.api.exception.AltoBasicException;
import org.opendaylight.alto.core.northbound.api.exception.AltoErrorInvalidFieldValue;
import org.opendaylight.alto.core.northbound.api.exception.AltoErrorInvalideFieldType;
import org.opendaylight.alto.core.northbound.api.exception.AltoErrorMissingField;
import org.opendaylight.alto.core.northbound.api.exception.AltoErrorSyntax;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285CostType;
import org.opendaylight.alto.multicost.impl.data.Condition;
import org.opendaylight.alto.multicost.impl.data.MulticostRequest;
import org.opendaylight.alto.multicost.impl.data.MulticostResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

class MulticostService {

    private final ImmutableList<RFC7285CostType> types;

    private final boolean testable;

    public MulticostService(final List<RFC7285CostType> types, boolean testable) {
        this.types = ImmutableList.copyOf(types);
        this.testable = testable;
    }

    public String accept(final String input) {
        try {
            MulticostRequest req = decode(input);

            MulticostResponse rep = respond(req);

            return encode(rep);
        } catch (Exception e) {
            return "";
        }
    }

    private ObjectMapper mapper = new ObjectMapper();

    private MulticostResponse respond(MulticostRequest request) {
        return null;
    }

    private void checkSemantic(MulticostRequest request) throws AltoBasicException {
        if ((request.multicostTypes == null) && (request.costType == null)) {
            /* Either field MUST be specified */
            throw new AltoErrorMissingField(MulticostRequest.FIELD_MULCOST);
        }
        if ((request.multicostTypes != null) && (request.costType != null)) {
            /* ... but not both */
            throw new AltoBadFormatException();
        }
        if (request.multicostTypes == null) {
            /* Fallback mode */
            request.fallbackMode = true;
            request.multicostTypes = Arrays.asList(request.costType);
            if (!types.contains(request.costType)) {
                throw new AltoErrorInvalidFieldValue("cost-type");
            }
        } else {
            boolean allMatch = request.multicostTypes
                .stream()
                .allMatch(t -> types.contains(t));
            if (!allMatch) {
                throw new AltoErrorInvalidFieldValue(MulticostRequest.FIELD_MULCOST);
            }
        }
        if (!this.testable) {
            if (request.constraints != null) {
                throw new AltoErrorInvalideFieldType("constraints");
            }
            if (request.orConstraintsRepr != null) {
                throw new AltoErrorInvalideFieldType(MulticostRequest.FIELD_ORCONSTRAINT);
            }
        } else {
            if ((request.constraints != null) || (request.orConstraintsRepr != null)) {
                /* ... MUST not both be specified */
                throw new AltoErrorInvalideFieldType(MulticostRequest.FIELD_ORCONSTRAINT);
            }
            if (request.constraints != null) {
                request.orConstraintsRepr = Arrays.asList(request.constraints);
            }

            if (request.testableTypes == null) {
                request.testableTypes = request.multicostTypes;
            }
            boolean allMatch = request.testableTypes
                .stream()
                .allMatch(t -> types.contains(t));
            if (!allMatch) {
                throw new AltoErrorInvalidFieldValue(MulticostRequest.FIELD_TESTABLE);
            }

            AltoBasicException e;
            e = new AltoErrorInvalidFieldValue(MulticostRequest.FIELD_ORCONSTRAINT);

            request.orConstraints = request.orConstraintsRepr
                .stream()
                .map(l -> l.stream()
                     .map(repr -> Condition.compile(repr, request.testableTypes, e))
                     .collect(Collectors.toList()))
                .collect(Collectors.toList());
        }
    }

    private MulticostRequest decode(String input) throws AltoErrorSyntax {
        try {
            MulticostRequest req = mapper.readValue(input, MulticostRequest.class);
            checkSemantic(req);
            return req;
        } catch (Exception e) {
            throw new AltoErrorSyntax();
        }
    }

    private String encode(MulticostResponse response) throws JsonProcessingException {
        String output = mapper.writeValueAsString(response);
        return output;
    }
}
