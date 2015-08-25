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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.opendaylight.alto.commons.types.rfc7285.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AltoNorthboundExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger logger = LoggerFactory.getLogger(AltoNorthboundExceptionHandler.class);

    @Override
    public Response toResponse(Exception e) {
        logger.info("begin exception handle: " + e.toString());
        logger.info("exception type: " + e.getClass().toString());
        if (e instanceof AltoBasicException) {
            return ((AltoBasicException) e).getResponse();
        }

        if (e instanceof JsonParseException) {
            logger.info("JsonParseException: " + e.toString());
            AltoBasicException ept = new AltoErrorTestException(Status.BAD_REQUEST,
                    AltoErrorTestException.ERROR_CODES.E_SYNTAX.name());
            return ept.getResponse();
        }

        if (e instanceof JsonMappingException) {
            logger.info("JsonMappingException: " + e.toString());
            if (e.getMessage().split(":")[0].equals("Missing field")) {
                AltoBasicException ept = new AltoErrorTestException(Status.BAD_REQUEST,
                        AltoErrorTestException.ERROR_CODES.E_MISSING_FIELD.name(), e.getMessage().split(":")[1]);
                return ept.getResponse();
            } else {
                String fieldName = ((JsonMappingException) e).getPath().get(0).getFieldName();
                AltoBasicException ept = new AltoErrorTestException(Status.BAD_REQUEST,
                        AltoErrorTestException.ERROR_CODES.E_INVALID_FIELD_TYPE.name(), fieldName);
                return ept.getResponse();
            }
        }

        if (e instanceof WebApplicationException) {
            return ((WebApplicationException) e).getResponse();
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.ALTO_ERROR).build();
    }
}
