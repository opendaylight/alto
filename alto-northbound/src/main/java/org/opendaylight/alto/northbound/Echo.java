/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.northbound;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.opendaylight.alto.commons.types.model150404.ModelNetworkMap;
import org.opendaylight.alto.commons.types.model150404.ModelJSONMapper;

@Path("/echo")
class Echo {

    protected ModelJSONMapper modelMapper = new ModelJSONMapper();

    @Path("/model/networkmap")
    @POST
    Response echoModelNetworkMap(String content) throws Exception {
        ModelNetworkMap nmap = modelMapper.asNetworkMap(content);
        String output = modelMapper.asJSON(nmap);
        return Response.ok(output).build();
    }

    @Path("/model/networkmap")
    @GET
    Response echoModelNetworkMap() throws Exception {
        return Response.ok("not implemented").build();
    }
}
