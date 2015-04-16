package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.alto.altohosttracker.plugin.internal.AltoHostTrackerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoHostTrackerModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.AbstractAltoHostTrackerModule {

    private static final Logger log = LoggerFactory.getLogger(AltoHostTrackerModule.class);

    AltoHostTrackerImpl altoHostTrackerImpl;

    public AltoHostTrackerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoHostTrackerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.AltoHostTrackerModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        DataBroker dataService = getDataBrokerDependency();
        if (dataService == null)log.info("data broker is null");

        altoHostTrackerImpl = new AltoHostTrackerImpl(dataService, null);
        
        altoHostTrackerImpl.writeDefaultNetworkMaps();
        altoHostTrackerImpl.writeDefaultCostMaps();
        altoHostTrackerImpl.writeDefaultEndpointpropertyMap();

        //altoHostTrackerImpl.mergeNetworkMapForAddressesList(null, "default-network-map", "pid0", "ipv4");

        altoHostTrackerImpl.registerAsDataChangeListener();

        log.info("write complete.");
        //mdHostTrackerImpl.readTest();
        final class CloseResources implements AutoCloseable {
            @Override
            public void close() throws Exception {
                if(altoHostTrackerImpl != null) {
                    altoHostTrackerImpl.close();
                }
                log.info("AltoHostTrackerImpl (instance {}) torn down.", this);
            }
        }
        AutoCloseable ret = new CloseResources();
        log.info("AltoHostTrackerImpl (instance {}) initialized.", ret);
        return ret;
    }

}
