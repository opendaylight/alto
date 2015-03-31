package org.opendaylight.alto.northbound;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class AltoNorthboundRSApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(AltoNorthbound.class);
        return classes;
    }
}
