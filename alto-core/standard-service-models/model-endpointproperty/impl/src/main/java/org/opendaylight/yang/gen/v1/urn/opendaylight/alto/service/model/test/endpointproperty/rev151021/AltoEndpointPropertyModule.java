package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.endpointproperty.rev151021;

import org.opendaylight.alto.core.impl.endpointproperty.test.AltoEndpointPropertyProvider;

public class AltoEndpointPropertyModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.endpointproperty.rev151021.AbstractAltoEndpointPropertyModule {
    public AltoEndpointPropertyModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoEndpointPropertyModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, AltoEndpointPropertyModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoEndpointPropertyProvider provider = new AltoEndpointPropertyProvider();

        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}
