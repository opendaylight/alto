/*
 * Copyright © 2015 Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.spce.impl.util;

public class RouteInfoKey {
    private String src;
    private String dst;

    public RouteInfoKey(String src, String dst) {
        this.src = src;
        this.dst = dst;
    }

    public int hashCode() {
        return this.src.hashCode()+this.dst.hashCode();
    }

    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RouteInfoKey routeInfoKey = (RouteInfoKey) obj;

        if (null == routeInfoKey.src
                || null == routeInfoKey.dst
                || !this.src.equals(routeInfoKey.src)
                || !this.dst.equals(routeInfoKey.dst)
                ) {
            return false;
        }

        return true;
    }


}
