/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.northbound.exception;

import javax.ws.rs.core.Response;

public class AltoErrorTestException extends AltoBasicException {
    public static final String TEMPLATE_EMPTY = "{}";
    public static final String TEMPLATE_CODE =
            "{\"meta\":{\"code\":\"%s\"}}";
    public static final String TEMPLATE_CODE_FIELD =
            "{\"meta\":{\"code\":\"%s\",\"field\":\"%s\"}}";
    public static final String TEMPLATE_CODE_FIELD_VALUE =
            "{\"meta\":{\"code\":\"%s\",\"field\":\"%s\",\"value\":\"%s\"}}";

    public enum ERROR_CODES {
      E_MISSING_FIELD, E_SYNTAX, E_INVALID_FIELD_TYPE, E_INVALID_FIELD_VALUE, E_INVALID_CLIENT_IP
    }

    public AltoErrorTestException(Response.Status status) {
        super(status, "{}");
    }

    public AltoErrorTestException(Response.Status status, String code) {
        super(status, String.format(TEMPLATE_CODE, code));
    }

    public AltoErrorTestException(Response.Status status, String code, String field) {
        super(status, String.format(TEMPLATE_CODE_FIELD, code, field));
    }

    public AltoErrorTestException(Response.Status status, String code, String field, String value) {
        super(status, String.format(TEMPLATE_CODE_FIELD_VALUE, code, field, value));
    }
}
