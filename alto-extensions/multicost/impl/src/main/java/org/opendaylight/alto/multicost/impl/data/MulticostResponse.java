/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.multicost.impl.data;

import java.util.List;

import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285CostType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MulticostResponse extends RFC7285CostMap {

    public static class Meta extends RFC7285CostMap.Meta {

        public Meta(RFC7285CostMap.Meta base) {
            this.netmap_tags = base.netmap_tags;
            this.costType = base.costType;
        }

        @JsonProperty(MulticostRequest.FIELD_MULCOST)
        public List<RFC7285CostType> multicostTypes;
    }

}
