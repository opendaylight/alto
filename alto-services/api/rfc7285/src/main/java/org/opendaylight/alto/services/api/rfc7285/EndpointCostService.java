package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.CostRequest;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.CostResponse;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;

public interface EndpointCostService {

    public CostResponse getEndpointCost(String id, CostRequest request);

    public CostResponse getEndpointCost(RFC7285VersionTag vtag, CostRequest request);

}
