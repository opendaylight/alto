/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.northbound.exception;

import javax.ws.rs.core.Response.Status;

public class AltoBadFormatException extends AltoBasicException {

    public static final String TEMPLATE = "Bad %s format: %s";

    public AltoBadFormatException(String field, String value) {
        super(Status.BAD_REQUEST, String.format(TEMPLATE, field, value));
    }
}
