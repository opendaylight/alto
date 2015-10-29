package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.endpointcost.rev151021;

import org.opendaylight.alto.core.impl.endpointcost.test.AltoEndpointCostProvider;

public class AltoEndpointCostModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.endpointcost.rev151021.AbstractAltoEndpointCostModule {
    public AltoEndpointCostModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoEndpointCostModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.endpointcost.rev151021.AltoEndpointCostModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoEndpointCostProvider provider = new AltoEndpointCostProvider();

        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}
