package org.opendaylight.yang.gen.v1.urn.alto.northbound.route.endpointproperty.impl.rev151021;

import org.opendaylight.alto.core.northbound.route.endpointproperty.impl.AltoNorthboundRouteEndpointproperty;

public class AltoNorthboundRouteEndpointpropertyModule extends org.opendaylight.yang.gen.v1.urn.alto.northbound.route.endpointproperty.impl.rev151021.AbstractAltoNorthboundRouteEndpointpropertyModule {
    public AltoNorthboundRouteEndpointpropertyModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoNorthboundRouteEndpointpropertyModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.alto.northbound.route.endpointproperty.impl.rev151021.AltoNorthboundRouteEndpointpropertyModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoNorthboundRouteEndpointproperty endpointproperty = new AltoNorthboundRouteEndpointproperty();
        getBrokerDependency().registerProvider(endpointproperty);
        endpointproperty.register(getAltoNorthboundRouterDependency());
        return endpointproperty;
    }

}
