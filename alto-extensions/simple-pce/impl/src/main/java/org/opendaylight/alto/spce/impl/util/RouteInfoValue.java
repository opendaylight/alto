/*
 * Copyright © 2015 Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.spce.impl.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.FlowType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;

import java.util.List;

public class RouteInfoValue {
    private FlowType flowType;
    private Long limitedRate;
    private Long burstSize;
    private List<TpId> route;

    public RouteInfoValue(FlowType flowType, long limitedRate, long burstSize, List<TpId> route) {
        this.flowType = flowType;
        this.limitedRate = limitedRate;
        this.burstSize = burstSize;
        this.route = route;
    }

    public void setFlowType(FlowType flowType) {
        this.flowType = flowType;
    }

    public void setLimitedRate(Long limitedRate) {
        this.limitedRate = new Long(limitedRate);
    }

    public void setBurstSize(Long burstSize) {
        this.burstSize = new Long(burstSize);
    }

    public void setRoute(List<TpId> route) {
        this.route = route;
    }

    public FlowType getFlowType() {
        return this.flowType;
    }

    public Long getLimitedRate() {
        return this.limitedRate;
    }

    public Long getBurstSize() {
        return this.burstSize;
    }

    public  List<TpId> getRoute() {
        return this.route;
    }
}
