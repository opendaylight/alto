/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.northbound.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.opendaylight.alto.commons.types.rfc7285.MediaType;

public class AltoBasicException extends WebApplicationException {

    public static final String MEDIA_TYPE = MediaType.ALTO_ERROR;

    public AltoBasicException(int status, Object cause) {
        super(Response.status(status).entity(cause).type(MEDIA_TYPE).build());
    }

    public AltoBasicException(Response.Status status, Object cause) {
        super(Response.status(status).entity(cause).type(MEDIA_TYPE).build());
    }
}
