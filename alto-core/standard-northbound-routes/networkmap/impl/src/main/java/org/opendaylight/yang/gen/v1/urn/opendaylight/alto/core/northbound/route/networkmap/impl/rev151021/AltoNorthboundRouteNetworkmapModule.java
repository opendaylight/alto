package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.networkmap.impl.rev151021;

import org.opendaylight.alto.core.northbound.route.networkmap.impl.AltoNorthboundRouteNetworkmap;

public class AltoNorthboundRouteNetworkmapModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.networkmap.impl.rev151021.AbstractAltoNorthboundRouteNetworkmapModule {
    public AltoNorthboundRouteNetworkmapModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoNorthboundRouteNetworkmapModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.networkmap.impl.rev151021.AltoNorthboundRouteNetworkmapModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoNorthboundRouteNetworkmap networkmap = new AltoNorthboundRouteNetworkmap();
        getBrokerDependency().registerProvider(networkmap);
        networkmap.register(getAltoNorthboundRouterDependency());

        return networkmap;
    }

}
