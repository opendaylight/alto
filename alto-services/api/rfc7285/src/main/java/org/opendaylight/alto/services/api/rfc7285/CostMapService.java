/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostType;

public interface CostMapService {

    RFC7285CostMap getCostMap(String id);

    RFC7285CostMap getCostMap(RFC7285VersionTag vtag);

    RFC7285CostMap getCostMap(String id, RFC7285CostType type);

    RFC7285CostMap getCostMap(RFC7285VersionTag vtag, RFC7285CostType type);

    RFC7285CostMap getCostMap(String id, RFC7285CostMap.Filter filter);

    RFC7285CostMap getCostMap(RFC7285VersionTag vtag, RFC7285CostMap.Filter filter);

    Boolean supportCostType(String id, RFC7285CostType type);

    Boolean supportCostType(RFC7285VersionTag vtag, RFC7285CostType type);

    Boolean validateCostMapFilter(String id, RFC7285CostMap.Filter filter);

    Boolean validateCostMapFilter(RFC7285VersionTag vtag, RFC7285CostMap.Filter filter);

}
