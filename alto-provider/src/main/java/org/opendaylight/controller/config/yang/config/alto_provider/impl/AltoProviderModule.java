package org.opendaylight.controller.config.yang.config.alto_provider.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.provider.impl.rev141119.OpendaylightAlto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.AltoServiceService;

public class AltoProviderModule extends org.opendaylight.controller.config.yang.config.alto_provider.impl.AbstractAltoProviderModule {
    public AltoProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.alto_provider.impl.AltoProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
		final OpendaylightAlto opendaylightAlto = new OpendaylightAlto();

		DataBroker dataBrokerService = getDataBrokerDependency();
		opendaylightAlto.setDataProvider(dataBrokerService);

		final BindingAwareBroker.RpcRegistration<AltoServiceService> rpcRegistration = getRpcRegistryDependency()
            .addRpcImplementation(AltoServiceService.class, opendaylightAlto);
		
		// Wrap toaster as AutoCloseable and close registrations to md-sal at
		// close(). The close method is where you would generally clean up
		// thread pools
		// etc.
		final class AutoCloseableAlto implements AutoCloseable {

			@Override
			public void close() throws Exception {
			    rpcRegistration.close();
			    opendaylightAlto.close();
			}
		}

		return new AutoCloseableAlto();
    }

}
