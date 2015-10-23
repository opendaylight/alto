package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.rev151021;

import org.opendaylight.alto.core.impl.AltoModelBaseProvider;

public class AltoModelBaseModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.rev151021.AbstractAltoModelBaseModule {
    public AltoModelBaseModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoModelBaseModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.rev151021.AltoModelBaseModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoModelBaseProvider provider = new AltoModelBaseProvider();

        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}
