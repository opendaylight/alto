package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285IRD;

public interface IRDService {

    public RFC7285IRD getDefaultIRD();

    public RFC7285IRD getIRD(String id);

}
