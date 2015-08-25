/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.northbound;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.alto.northbound.exception.AltoNorthboundExceptionHandler;

public class AltoNorthboundRSApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(AltoNorthbound.class);
        classes.add(AltoNorthboundExceptionHandler.class);
        return classes;
    }
}
