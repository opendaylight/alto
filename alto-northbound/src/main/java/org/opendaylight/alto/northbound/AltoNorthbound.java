/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.northbound;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.opendaylight.alto.commons.types.mapper.JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.MediaType;
import org.opendaylight.alto.services.AltoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.IRD;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;

@Path("/")
public class AltoNorthbound {
  private AltoService altoService = new AltoService();
  private JSONMapper mapper = new JSONMapper();
  
  @GET
  @Produces({ MediaType.ALTO_DIRECTORY, MediaType.ALTO_ERROR })
  public Response retrieveIRD() {
    try {
      IRD ird = altoService.getIRD();
      return Response.ok(ird, MediaType.ALTO_DIRECTORY).build();
    } catch (Exception e) {
    }
    return Response.ok("", MediaType.ALTO_ERROR).build();
  }

  @Path("/networkmap/{networkmap_id}")
  @GET
  @Produces({ MediaType.ALTO_NETWORKMAP, MediaType.ALTO_ERROR })
  public Response retrieveNetworkMap(
      @PathParam(value = "networkmap_id") String nmap_id) {
    NetworkMap networkMap = altoService.getNetworkMap(new ResourceId(nmap_id));
    try {
      return Response.ok("", MediaType.ALTO_NETWORKMAP).build();
    } catch (Exception e) { 
      return Response.ok("", MediaType.ALTO_ERROR).build();
    }
  }
}
