/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.impl;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

public class AltoNorthboundApplication extends Application {
    private final AltoNorthbound northbound;

    public AltoNorthboundApplication(AltoNorthbound northbound) {
        this.northbound = northbound;
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(AltoNorthboundExceptionHandler.class);
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();
        singletons.add(northbound);
        return singletons;
    }
}
