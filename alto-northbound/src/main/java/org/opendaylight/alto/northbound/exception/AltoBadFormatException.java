package org.opendaylight.alto.northbound.exception;

import javax.ws.rs.core.Response.Status;

public class AltoBadFormatException extends AltoBasicException {

    public static final String TEMPLATE = "Bad %s format: %s";

    public AltoBadFormatException(String field, String value) {
        super(Status.BAD_REQUEST, String.format(TEMPLATE, field, value));
    }
}
