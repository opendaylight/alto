/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.endpointcostservice.impl.rev141210;

import org.opendaylight.alto.basic.endpointcostservice.impl.EndpointcostserviceProvider;


public class EndpointcostserviceModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.endpointcostservice.impl.rev141210.AbstractEndpointcostserviceModule {
    public EndpointcostserviceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public EndpointcostserviceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.endpointcostservice.impl.rev141210.EndpointcostserviceModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final EndpointcostserviceProvider provider = new EndpointcostserviceProvider();
        try {
            getBrokerDependency().registerProvider(provider);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return provider;
    }

}
