package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.ird.rev151021;

import org.opendaylight.alto.core.impl.ird.test.AltoIrdProvider;

public class AltoIrdModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.ird.rev151021.AbstractAltoIrdModule {
    public AltoIrdModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoIrdModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.ird.rev151021.AltoIrdModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoIrdProvider provider = new AltoIrdProvider();

        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}
