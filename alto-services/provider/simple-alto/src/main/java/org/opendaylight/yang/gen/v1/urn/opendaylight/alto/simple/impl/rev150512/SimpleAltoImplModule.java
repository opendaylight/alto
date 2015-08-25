package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.simple.impl.rev150512;

import org.opendaylight.alto.services.provider.simple.SimpleAltoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.AltoServiceService;

public class SimpleAltoImplModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.simple.impl.rev150512.AbstractSimpleAltoImplModule {
    public SimpleAltoImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public SimpleAltoImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.simple.impl.rev150512.SimpleAltoImplModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        SimpleAltoService service = new SimpleAltoService(this.getDataBrokerDependency(), 
                this.getRpcRegistryDependency().getRpcService(AltoServiceService.class));
        return service;
    }

}
