package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.networkmap.rev151021;

import org.opendaylight.alto.core.impl.networkmap.test.AltoNetworkmapProvider;

public class AltoNetworkmapModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.networkmap.rev151021.AbstractAltoNetworkmapModule {
    public AltoNetworkmapModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoNetworkmapModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.networkmap.rev151021.AltoNetworkmapModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoNetworkmapProvider provider = new AltoNetworkmapProvider();

        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}
