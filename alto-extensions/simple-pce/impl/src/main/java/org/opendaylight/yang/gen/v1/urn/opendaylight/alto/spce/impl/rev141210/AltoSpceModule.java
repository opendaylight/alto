package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.impl.rev141210;

import org.opendaylight.alto.spce.impl.AltoSpceProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;

public class AltoSpceModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.impl.rev141210.AbstractAltoSpceModule {
    public AltoSpceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoSpceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.impl.rev141210.AltoSpceModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoSpceProvider provider = new AltoSpceProvider();
        BindingAwareBroker broker = getBrokerDependency();
        broker.registerProvider(provider);
        return provider;
    }

}
