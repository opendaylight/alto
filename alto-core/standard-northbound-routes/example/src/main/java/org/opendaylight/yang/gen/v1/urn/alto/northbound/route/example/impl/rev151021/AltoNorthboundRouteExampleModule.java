package org.opendaylight.yang.gen.v1.urn.alto.northbound.route.example.impl.rev151021;

import org.opendaylight.alto.core.northbound.route.example.impl.AltoNorthboundRouteExample;

public class AltoNorthboundRouteExampleModule extends org.opendaylight.yang.gen.v1.urn.alto.northbound.route.example.impl.rev151021.AbstractAltoNorthboundRouteExampleModule {
    public AltoNorthboundRouteExampleModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoNorthboundRouteExampleModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.alto.northbound.route.example.impl.rev151021.AltoNorthboundRouteExampleModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        getAltoNorthboundRouterDependency().addRoute("example", new AltoNorthboundRouteExample());

        return new AutoCloseable() {
            @Override
            public void close() {
                getAltoNorthboundRouterDependency().removeRoute("example");
            }
        };
    }

}
