package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.Endpoint;
import org.opendaylight.alto.commons.types.rfc7285.VersionTag;

public interface EndpointCostService {

    public Endpoint.CostResponse getEndpointCost(String id, Endpoint.CostRequest request);

    public Endpoint.CostResponse getEndpointCost(VersionTag vtag, Endpoint.CostRequest request);

}
