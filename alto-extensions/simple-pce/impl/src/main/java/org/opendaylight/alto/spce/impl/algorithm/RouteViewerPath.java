/*
 * Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl.algorithm;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;

public class RouteViewerPath {
    TpId src;
    TpId dst;
    Long bandwidth;

    @Override
    public String toString() {
        return "" + src + "->" + dst + "@" + bandwidth;
    }
}
