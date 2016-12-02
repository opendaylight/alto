/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.alto.simple.ird.impl.rev151021;

import org.junit.Test;
import org.opendaylight.alto.basic.impl.AltoSimpleIrdProvider;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;
import org.opendaylight.controller.config.api.DependencyResolver;
import org.opendaylight.controller.config.api.JmxAttribute;
import org.opendaylight.controller.config.api.ModuleIdentifier;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;

import javax.management.ObjectName;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AltoSimpleIrdModuleTest {
    @Test
    public void testCustomValidation() {
        AltoSimpleIrdModule module = new AltoSimpleIrdModule(mock(ModuleIdentifier.class), mock(DependencyResolver.class));

        // ensure no exceptions on validation
        // currently this method is empty
        module.customValidation();
    }

    @Test
    public void testCreateInstance() throws Exception {
        // configure mocks
        DependencyResolver dependencyResolver = mock(DependencyResolver.class);
        BindingAwareBroker broker = mock(BindingAwareBroker.class);
        AltoNorthboundRouter router = mock(AltoNorthboundRouter.class);

        when(dependencyResolver.resolveInstance(eq(BindingAwareBroker.class), any(ObjectName.class), any(JmxAttribute.class))).thenReturn(broker);
        when(dependencyResolver.resolveInstance(eq(AltoNorthboundRouter.class), any(ObjectName.class), any(JmxAttribute.class))).thenReturn(router);

        // create instance of module with injected mocks
        AltoSimpleIrdModule module = new AltoSimpleIrdModule(mock(ModuleIdentifier.class), dependencyResolver);

        // getInstance calls resolveInstance to get the broker dependency and then calls createInstance
        AutoCloseable closeable = module.getInstance();

        // verify that the module registered the returned provider with the broker
        verify(broker).registerProvider((AltoSimpleIrdProvider)closeable);

        // ensure no exceptions on close
        closeable.close();
    }
}
