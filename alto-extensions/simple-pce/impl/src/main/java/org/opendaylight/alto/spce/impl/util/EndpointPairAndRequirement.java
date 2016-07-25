/*
 * Copyright © 2015 Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl.util;

public class EndpointPairAndRequirement {
    private String src;
    private String dst;
    private Long dropRate;
    private Long burstSize;
    public EndpointPairAndRequirement(String src, String dst, Long dropRate, Long burstSize) {
        this.src = src;
        this.dst = dst;
        this.dropRate = dropRate;
        this.burstSize = burstSize;
    }

    public int hashCode() {
        return this.burstSize.hashCode()+this.dropRate.hashCode()+this.src.hashCode()+this.dst.hashCode();
    }

    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EndpointPairAndRequirement epr = (EndpointPairAndRequirement) obj;
        if (null == epr.src || null == epr.dst || null == epr.dropRate || null == epr.burstSize
                || !(this.src.equals(epr.src)) || !(this.dst.equals(epr.dst))
                || !(this.dropRate.equals(epr.dropRate)) || !(this.burstSize.equals(epr.burstSize))) {
            return false;
        }
        return true;
    }
}
