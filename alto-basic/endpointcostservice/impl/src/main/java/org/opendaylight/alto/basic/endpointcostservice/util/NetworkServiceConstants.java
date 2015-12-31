/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.util;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class NetworkServiceConstants {
    public static final String TEN_MB_HD = "ten-mb-hd";
    public static final String TEN_MB_FD = "ten-mb-fd";
    public static final String HUNDRED_MD_HD = "hundred-mb-hd";
    public static final String HUNDRED_MD_FD = "hundred-mb-fd";
    public static final String ONE_GB_HD = "one-gb-hd";
    public static final String ONE_MD_FD = "one-gb-fd";
    public static final String TEN_GB_FD = "ten-gb-fd";
    public static final String FORTY_GB_FD = "forty-gb-fd";
    public static final String HUNDRED_GB_FD = "hundred-gb-fd";
    public static final String ONE_TB_FD = "one-tb-fd";
    public static final String OTHER = "other";
    public static final String COPPER = "copper";
    public static final String FIBER = "fiber";
    public static final String AUTOENG = "autoeng";
    public static final String PAUSE = "pause";
    public static final String PAUSE_ASYM = "pause-asym";

    public static final Map<String, Integer> PORT_FEATURES = ImmutableMap.<String, Integer>builder()
            .put(TEN_MB_HD, 0)
            .put(TEN_MB_FD, 1)
            .put(HUNDRED_MD_HD, 2)
            .put(HUNDRED_MD_FD, 3)
            .put(ONE_GB_HD, 4)
            .put(ONE_MD_FD, 5)
            .put(TEN_GB_FD, 6)
            .put(FORTY_GB_FD, 7)
            .put(HUNDRED_GB_FD, 8)
            .put(ONE_TB_FD, 9)
            .put(OTHER, 10)
            .put(COPPER, 11)
            .put(FIBER, 12)
            .put(AUTOENG, 13)
            .put(PAUSE, 14)
            .put(PAUSE_ASYM, 15)
            .build();
}
