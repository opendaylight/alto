package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.PropertyRequest;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.PropertyResponse;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;

public interface EndpointPropertyService {

    public PropertyResponse getEndpointProperty(String id, PropertyRequest request);

    public PropertyResponse getEndpointProperty(RFC7285VersionTag vtag, PropertyRequest request);

}
