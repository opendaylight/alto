package org.opendaylight.alto.northbound;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class AltoNorthboundRSApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(AltoNorthbound.class);
        return classes;
    }
}
