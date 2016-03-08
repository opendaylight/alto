/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.impl;

import org.opendaylight.alto.core.northbound.api.exception.AltoBasicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class AltoNorthboundExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger logger = LoggerFactory
        .getLogger(AltoNorthboundExceptionHandler.class);

    @Override
    public Response toResponse(Exception e) {
        logger.info("begin exception handle: " + e.toString());
        logger.info("exception type: " + e.getClass().toString());
        if (e instanceof AltoBasicException) {
            return ((AltoBasicException) e).getResponse();
        }
        if (e instanceof WebApplicationException) {
            return ((WebApplicationException) e).getResponse();
        }
        return Response.status(Response
            .Status.INTERNAL_SERVER_ERROR)
            .type(org.opendaylight.alto.core.northbound.api.utils.rfc7285.MediaType.ALTO_ERROR).build();
    }
}
