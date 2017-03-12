/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.multicost.impl.data;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.opendaylight.alto.core.northbound.api.exception.AltoBasicException;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285CostType;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class Condition {

    public static final ImmutableMap<String, BiFunction<Double, Double, Boolean>> OPERATOR_MAP;

    public static final String OP_LTREPR = "lt";

    public static final String OP_LEREPR = "le";

    public static final String OP_EQREPR = "eq";

    public static final String OP_GTREPR = "gt";

    public static final String OP_GEREPR = "ge";

    public static final double PRECISION = 1e-5;

    public static final BiFunction<Double, Double, Boolean> OP_LT;
    public static final BiFunction<Double, Double, Boolean> OP_LE;
    public static final BiFunction<Double, Double, Boolean> OP_EQ;
    public static final BiFunction<Double, Double, Boolean> OP_GT;
    public static final BiFunction<Double, Double, Boolean> OP_GE;

    static {
        OP_LT = (x, y) -> (x < y);
        OP_LE = (x, y) -> (x <= y);
        OP_EQ = (x, y) -> (Math.abs(y - x) < PRECISION);
        OP_GT = (x, y) -> (x > y);
        OP_GE = (x, y) -> (x >= y);

        Map<String, BiFunction<Double, Double, Boolean>> map = Maps.newHashMap();
        map.put(OP_LTREPR, OP_LT);
        map.put(OP_LEREPR, OP_LE);
        map.put(OP_EQREPR, OP_EQ);
        map.put(OP_GTREPR, OP_GT);
        map.put(OP_GEREPR, OP_GE);

        OPERATOR_MAP = ImmutableMap.copyOf(map);
    }

    public static Condition compile(final String repr,
                                    List<RFC7285CostType> types,
                                    AltoBasicException e) throws AltoBasicException {
        String[] parts = repr.split(" ");
        if (parts.length < 2) {
            throw e;
        }
        if (parts.length == 2) {
            String[] newParts = { "[0]", parts[0], parts[1] };
            parts = newParts;
        }
        if (parts.length > 3) {
            throw e;
        }
        try {
            String irepr = parts[0].replaceAll("\\s", "");
            irepr = irepr.substring(1, irepr.length() - 1);
            int index = Integer.valueOf(irepr);
            if (index > types.size()) {
                throw e;
            }
            RFC7285CostType type = types.get(index);
            BiFunction<Double, Double, Boolean> op = OPERATOR_MAP.get(parts[1]);
            Double bound = Double.valueOf(parts[2]);

            if ((type == null) || (op == null)) {
                throw e;
            }

            return new Condition(type, op, bound);
        } catch (Exception ignore) {
            throw e;
        }
    }

    private Condition(final RFC7285CostType type,
                      final BiFunction<Double, Double, Boolean> operator,
                      final Double bound) {
        this.type = type;
        this.operator = operator;
        this.bound = bound;
    }

    public final RFC7285CostType type;

    public final BiFunction<Double, Double, Boolean> operator;

    public final Double bound;
}
