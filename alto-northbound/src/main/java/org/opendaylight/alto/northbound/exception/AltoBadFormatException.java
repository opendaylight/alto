/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.northbound.exception;

import javax.ws.rs.core.Response.Status;

public class AltoBadFormatException extends AltoErrorTestException {

    public AltoBadFormatException() {
        super(Status.BAD_REQUEST);
    }

    public AltoBadFormatException(String code) {
        super(Status.BAD_REQUEST, code);
    }

    public AltoBadFormatException(String code, String field) {
        super(Status.BAD_REQUEST, code, field);
    }

    public AltoBadFormatException(String code, String field, String value) {
        super(Status.BAD_REQUEST, code, field, value);
    }

}
