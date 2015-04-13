package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.Endpoint;
import org.opendaylight.alto.commons.types.rfc7285.VersionTag;

public interface EndpointPropertyService {

    public Endpoint.PropertyResponse getEndpointProperty(String id, Endpoint.PropertyRequest request);

    public Endpoint.PropertyResponse getEndpointProperty(VersionTag vtag, Endpoint.PropertyRequest request);

}
