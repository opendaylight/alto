package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.IRD;

public interface IRDService {

    public IRD getDefaultIRD();

    public IRD getIRD(String id);

}
