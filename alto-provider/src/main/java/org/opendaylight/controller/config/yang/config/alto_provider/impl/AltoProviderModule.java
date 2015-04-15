package org.opendaylight.controller.config.yang.config.alto_provider.impl;

import org.opendaylight.alto.provider.AltoProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.AltoServiceService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoProviderModule extends AbstractAltoProviderModule {
    private static final Logger log = LoggerFactory.getLogger(AltoProviderModule.class);

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

        final ListenerRegistration<DataChangeListener> altoDataChangeListenerRegistration =
            dataBrokerService.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, AltoProvider.ALTO_IID, altoProvider, DataChangeScope.SUBTREE);
        final AltoProviderRuntimeRegistration runtimeReg = getRootRuntimeBeanRegistratorWrapper().register(altoProvider);

        final class AutoCloseableAlto implements AutoCloseable {
            @Override
            public void close() throws Exception {
                rpcRegistration.close();
                altoDataChangeListenerRegistration.close();
                runtimeReg.close();
                altoProvider.close();
            }
        }

        return new AutoCloseableAlto();
    }

}
