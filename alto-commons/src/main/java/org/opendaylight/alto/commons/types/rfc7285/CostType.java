package org.opendaylight.alto.commons.types.rfc7285;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

public class CostType {

    @JsonProperty("cost-metric")
    public String metric;

    @JsonProperty("cost-mode")
    public String mode;

    @JsonProperty("description")
    public String description;

    @Override
    public int hashCode() {
        String[] members = { metric, mode };
        return Arrays.hashCode(members);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj != null ? (obj instanceof CostType) : false))
            return false;

        CostType other = (CostType)obj;
        String[] lhs = { metric, mode };
        String[] rhs = { other.metric, other.mode };
        return Arrays.equals(lhs, rhs);
    }
}
