package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.impl.network.tracker.impl.rev141210;

import org.opendaylight.alto.spce.network.impl.NetworkTrackerProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;

public class NetworkTrackerModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.impl.network.tracker.impl.rev141210.AbstractNetworkTrackerModule {
    public NetworkTrackerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NetworkTrackerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.impl.network.tracker.impl.rev141210.NetworkTrackerModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        // TODO:implement
        // throw new java.lang.UnsupportedOperationException();
        BindingAwareBroker broker = getBrokerDependency();
        NetworkTrackerProvider provider = new NetworkTrackerProvider();
        broker.registerProvider(provider);
        return provider;
    }

}
