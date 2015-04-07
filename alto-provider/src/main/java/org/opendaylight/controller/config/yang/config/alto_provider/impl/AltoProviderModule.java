package org.opendaylight.controller.config.yang.config.alto_provider.impl;

import org.opendaylight.controller.alto.provider.AltoProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.AltoServiceService;

public class AltoProviderModule extends AbstractAltoProviderModule {
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
		final AltoProvider altoProvider = new AltoProvider();

		DataBroker dataBrokerService = getDataBrokerDependency();
		altoProvider.setDataProvider(dataBrokerService);
		
		final BindingAwareBroker.RpcRegistration<AltoServiceService> rpcRegistration = getRpcRegistryDependency()
            .addRpcImplementation(AltoServiceService.class, altoProvider);
		final AltoProviderRuntimeRegistration runtimeReg = getRootRuntimeBeanRegistratorWrapper().register(altoProvider);
		
		final class AutoCloseableAlto implements AutoCloseable {
			@Override
			public void close() throws Exception {
			    rpcRegistration.close();
			    runtimeReg.close();
			    altoProvider.close();
			}
		}

		return new AutoCloseableAlto();
    }

}
