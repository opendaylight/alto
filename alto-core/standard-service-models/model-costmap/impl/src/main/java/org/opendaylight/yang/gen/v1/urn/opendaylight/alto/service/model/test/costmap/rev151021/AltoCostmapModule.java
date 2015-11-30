package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.costmap.rev151021;

import org.opendaylight.alto.core.impl.costmap.test.AltoCostmapProvider;

public class AltoCostmapModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.costmap.rev151021.AbstractAltoCostmapModule {
    public AltoCostmapModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoCostmapModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.test.costmap.rev151021.AltoCostmapModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoCostmapProvider provider = new AltoCostmapProvider();
        getBrokerDependency().registerProvider(provider);
        return provider;

    }

}
