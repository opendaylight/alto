/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.rfc7285;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

public class RFC7285CostType {

    @JsonProperty("cost-mode")
    public String mode = null;

    @JsonProperty("cost-metric")
    public String metric = null;

    @JsonProperty("description")
    public String description = null;

    public RFC7285CostType() {
    }

    public RFC7285CostType(String mode, String metric) {
        this.mode = mode;
        this.metric = metric;
    }

    public RFC7285CostType(String mode, String metric, String description) {
        this.mode = mode;
        this.metric = metric;
        this.description = description;
    }

    @Override
    public int hashCode() {
        String[] members = { metric, mode };
        return Arrays.hashCode(members);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj != null ? (obj instanceof RFC7285CostType) : false))
            return false;

        RFC7285CostType other = (RFC7285CostType)obj;
        String[] lhs = { metric, mode };
        String[] rhs = { other.metric, other.mode };
        return Arrays.equals(lhs, rhs);
    }
}
