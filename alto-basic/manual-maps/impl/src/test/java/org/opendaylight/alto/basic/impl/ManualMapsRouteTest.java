/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.basic.impl;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.opendaylight.alto.basic.impl.ManualMapsRoute.ALTO_CONFIG;
import static org.opendaylight.alto.basic.impl.ManualMapsRoute.CONFIG_SUCCESS;

public class ManualMapsRouteTest {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    AltoManualMapsProvider mockProvider = mock(AltoManualMapsProvider.class);
    ManualMapsRoute mmr = new ManualMapsRoute(mockProvider);
    @Test
    public void modifyResourceRoute() throws Exception {
        Response resultResponse = mmr.modifyResourceRoute(mockRequest, "firstpath", "firstmap");
        Response expectResponse = Response.ok(CONFIG_SUCCESS + "(POST: " + "firstpath" + ")", ALTO_CONFIG).build();
        assertEquals(expectResponse.getStatus(), resultResponse.getStatus());
        assertEquals(expectResponse.getEntity(), resultResponse.getEntity());
        assertEquals(expectResponse.getMetadata(), resultResponse.getMetadata());

    }

    @Test
    public void newResourceRoute() throws Exception {
        Response resultResponse = mmr.newResourceRoute(mockRequest, "secondpath", "secondmap");
        Response expectResponse =  Response.ok(CONFIG_SUCCESS + "(PUT: " + "secondpath" + ")", ALTO_CONFIG).build();
        assertEquals(expectResponse.getStatus(), resultResponse.getStatus());
        assertEquals(expectResponse.getEntity(), resultResponse.getEntity());
        assertEquals(expectResponse.getMetadata(), resultResponse.getMetadata());

    }

    @Test
    public void removeResourceRoute() throws Exception {
        Response resultResponse = mmr.removeResourceRoute(mockRequest, "thirdpath", "thirdmap");
        Response expectResponse = Response.ok(CONFIG_SUCCESS + "(DELETE: " + "thirdpath" + ")", ALTO_CONFIG).build();
        assertEquals(expectResponse.getStatus(), resultResponse.getStatus());
        assertEquals(expectResponse.getEntity(), resultResponse.getEntity());
        assertEquals(expectResponse.getMetadata(), resultResponse.getMetadata());

    }

}