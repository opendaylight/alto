package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.impl.rev151021;

import org.opendaylight.alto.basic.impl.AltoManualMapsProvider;

public class AltoManualMapsModule extends AbstractAltoManualMapsModule {
    public AltoManualMapsModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoManualMapsModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, AltoManualMapsModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final AltoManualMapsProvider provider = new AltoManualMapsProvider();

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
