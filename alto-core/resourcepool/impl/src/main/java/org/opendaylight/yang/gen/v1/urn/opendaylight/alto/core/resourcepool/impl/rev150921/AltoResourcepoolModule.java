package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.impl.rev150921;

import org.opendaylight.alto.core.resourcepool.impl.AltoResourcepoolProvider;

public class AltoResourcepoolModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.impl.rev150921.AbstractAltoResourcepoolModule {
    public AltoResourcepoolModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoResourcepoolModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.impl.rev150921.AltoResourcepoolModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoResourcepoolProvider provider = new AltoResourcepoolProvider();

        getBrokerDependency().registerProvider(provider);
        return provider;
   }

}
