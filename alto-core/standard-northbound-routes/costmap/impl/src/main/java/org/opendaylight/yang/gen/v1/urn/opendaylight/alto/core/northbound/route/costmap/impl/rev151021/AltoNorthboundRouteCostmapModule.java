package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.costmap.impl.rev151021;

import org.opendaylight.alto.core.northbound.route.costmap.impl.AltoNorthboundRouteCostmap;

public class AltoNorthboundRouteCostmapModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.costmap.impl.rev151021.AbstractAltoNorthboundRouteCostmapModule {
    public AltoNorthboundRouteCostmapModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AltoNorthboundRouteCostmapModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.costmap.impl.rev151021.AltoNorthboundRouteCostmapModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        AltoNorthboundRouteCostmap costmap = new AltoNorthboundRouteCostmap();
        getBrokerDependency().registerProvider(costmap);
        costmap.register(getAltoNorthboundRouterDependency());
        return costmap;
    }

}
