package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.config.rev151021;

import org.opendaylight.alto.core.impl.config.test.AltoConfigProvider;

public class AltoConfigModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.config.rev151021.AbstractAltoConfigModule {
    public AltoConfigModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoConfigModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.config.rev151021.AltoConfigModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoConfigProvider provider = new AltoConfigProvider();

        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}
