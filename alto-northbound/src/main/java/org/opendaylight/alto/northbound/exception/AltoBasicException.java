package org.opendaylight.alto.northbound.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
