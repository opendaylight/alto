package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.CostMap;
import org.opendaylight.alto.commons.types.rfc7285.VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.CostType;

public interface CostMapService {

    public CostMap getCostMap(String id);

    public CostMap getCostMap(VersionTag vtag);

    public CostMap getCostMap(String id, CostType type);

    public CostMap getCostMap(VersionTag vtag, CostType type);

    public CostMap getCostMap(String id, CostMap.Filter filter);

    public CostMap getCostMap(VersionTag vtag, CostMap.Filter filter);

    public Boolean supportCostType(String id, CostType type);

    public Boolean supportCostType(VersionTag vtag, CostType type);

    public Boolean validateCostMapFilter(String id, CostMap.Filter filter);

    public Boolean validateCostMapFilter(VersionTag vtag, CostMap.Filter filter);

}
