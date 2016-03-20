/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.api.exception;

import org.opendaylight.alto.core.northbound.api.utils.rfc7285.MediaType;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class AltoBasicException extends WebApplicationException {

    public AltoBasicException(int status, Object cause) {
        super(Response.status(status).entity(cause).type(MediaType.ALTO_ERROR).build());
    }

    public AltoBasicException(Response.Status status, Object cause) {
        super(Response.status(status).entity(cause).type(MediaType.ALTO_ERROR).build());
    }
}
