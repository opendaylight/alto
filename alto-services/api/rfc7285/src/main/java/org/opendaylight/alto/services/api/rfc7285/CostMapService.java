package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostType;

public interface CostMapService {

    public RFC7285CostMap getCostMap(String id);

    public RFC7285CostMap getCostMap(RFC7285VersionTag vtag);

    public RFC7285CostMap getCostMap(String id, RFC7285CostType type);

    public RFC7285CostMap getCostMap(RFC7285VersionTag vtag, RFC7285CostType type);

    public RFC7285CostMap getCostMap(String id, RFC7285CostMap.Filter filter);

    public RFC7285CostMap getCostMap(RFC7285VersionTag vtag, RFC7285CostMap.Filter filter);

    public Boolean supportCostType(String id, RFC7285CostType type);

    public Boolean supportCostType(RFC7285VersionTag vtag, RFC7285CostType type);

    public Boolean validateCostMapFilter(String id, RFC7285CostMap.Filter filter);

    public Boolean validateCostMapFilter(RFC7285VersionTag vtag, RFC7285CostMap.Filter filter);

}
