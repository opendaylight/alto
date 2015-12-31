package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.resource.config.impl.rev151021;

import org.opendaylight.alto.basic.impl.AltoResourceConfigProvider;

public class AltoResourceConfigModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.resource.config.impl.rev151021.AbstractAltoResourceConfigModule {
    public AltoResourceConfigModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoResourceConfigModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.resource.config.impl.rev151021.AltoResourceConfigModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final AltoResourceConfigProvider provider = new AltoResourceConfigProvider();

        try {
            getBrokerDependency().registerProvider(provider);
            provider.setupRoute(getAltoNorthboundRouterDependency());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return provider;
    }

}
