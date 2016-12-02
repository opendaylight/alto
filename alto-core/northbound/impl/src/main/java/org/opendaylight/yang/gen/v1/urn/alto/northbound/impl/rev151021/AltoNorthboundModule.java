package org.opendaylight.yang.gen.v1.urn.alto.northbound.impl.rev151021;

import org.opendaylight.alto.core.northbound.impl.AltoNorthboundProvider;

public class AltoNorthboundModule extends org.opendaylight.yang.gen.v1.urn.alto.northbound.impl.rev151021.AbstractAltoNorthboundModule {
    public AltoNorthboundModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoNorthboundModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.alto.northbound.impl.rev151021.AltoNorthboundModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoNorthboundProvider provider = new AltoNorthboundProvider();

        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}
