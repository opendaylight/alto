/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.northbound.api.exception;

import javax.ws.rs.core.Response;

public class AltoErrorMissingField extends AltoErrorTestException {
    public AltoErrorMissingField() {
        super(Response.Status.BAD_REQUEST, ERROR_CODES.E_MISSING_FIELD.name());
    }

    public AltoErrorMissingField(String field) {
        super(Response.Status.BAD_REQUEST, ERROR_CODES.E_MISSING_FIELD.name(), field);
    }
}
