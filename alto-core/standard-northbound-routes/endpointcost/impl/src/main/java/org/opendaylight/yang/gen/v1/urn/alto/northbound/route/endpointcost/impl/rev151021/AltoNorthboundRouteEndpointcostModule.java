package org.opendaylight.yang.gen.v1.urn.alto.northbound.route.endpointcost.impl.rev151021;

import org.opendaylight.alto.core.northbound.route.endpointcost.impl.AltoNorthboundRouteEndpointcost;

public class AltoNorthboundRouteEndpointcostModule extends org.opendaylight.yang.gen.v1.urn.alto.northbound.route.endpointcost.impl.rev151021.AbstractAltoNorthboundRouteEndpointcostModule {
    public AltoNorthboundRouteEndpointcostModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoNorthboundRouteEndpointcostModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.alto.northbound.route.endpointcost.impl.rev151021.AltoNorthboundRouteEndpointcostModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoNorthboundRouteEndpointcost endpointcost = new AltoNorthboundRouteEndpointcost();
        getBrokerDependency().registerProvider(endpointcost);
        endpointcost.register(getAltoNorthboundRouterDependency());
        return endpointcost;
    }

}
