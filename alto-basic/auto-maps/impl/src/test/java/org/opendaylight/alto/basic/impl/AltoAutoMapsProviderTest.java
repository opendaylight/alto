/*
 * Copyright © 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import static org.mockito.Mockito.mock;

public class AltoAutoMapsProviderTest {

    private DataBroker dataBroker = mock(DataBroker.class);
    private AltoAutoMapsProvider provider;

    @Before
    public void setUp() throws Exception {
        provider = new AltoAutoMapsProvider(dataBroker);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testInit() throws Exception {
        provider.init();
    }

    @Test
    public void testClose() throws Exception {
        provider.close();
    }
}
