/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.northbound.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.opendaylight.alto.commons.types.rfc7285.MediaType;

@Provider
public class AltoNorthboundExceptionHandler
                implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception e) {
        if (e instanceof AltoBasicException)
            return ((AltoBasicException)e).getResponse();
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity(e.getMessage())
                        .type(MediaType.ALTO_ERROR).build();
    }
}
