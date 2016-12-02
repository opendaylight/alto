/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.basic.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;

import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstance;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.entry.configuration.data.location.FixedUrl;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.entry.configuration.data.location.RelativePath;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.entry.data.EntryCapabilities;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.ird.instance.IrdEntry;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.Rfc7285CostTypeCapabilities;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.Rfc7285IrdMeta;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.Rfc7285IrdMetadata;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rfc7285.rev151021.rfc7285.ird.meta.CostType;

import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;

@Path("/")
public class SimpleIrdRoute implements AltoNorthboundRoute {

    public static final String ALTO_IRD = "application/alto-directory+json";

    private AltoSimpleIrdProvider m_provider = null;

    public SimpleIrdRoute(AltoSimpleIrdProvider provider) {
        m_provider = provider;
    }

    @Path("{path:.+}")
    @GET
    @Produces({ALTO_IRD, ALTO_ERROR})
    public Response route(@Context HttpServletRequest req, @PathParam("path") String path) {
        if (m_provider == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        IrdInstance ird = m_provider.getInstance(new ResourceId(path));
        if (ird == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            Rfc7285Ird rfcIrd = convert(ird, req);
            return Response.ok(rfcIrd, ALTO_IRD).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    protected static class Rfc7285Ird {

        public Map<String, Object> meta = new HashMap<String, Object>();

        public Map<String, Object> resources = new HashMap<String, Object>();
    }

    private Rfc7285Ird convert(IrdInstance ird, HttpServletRequest req) {
        Rfc7285Ird rfcIrd = new Rfc7285Ird();

        Rfc7285IrdMetadata metadata = ird.getAugmentation(Rfc7285IrdMetadata.class);
        if (metadata != null) {
            Rfc7285IrdMeta meta = metadata.getMeta();

            if (meta != null) {
                if (meta.getDefaultNetworkMap() != null) {
                    rfcIrd.meta.put("default-alto-network-map", meta.getDefaultNetworkMap().getValue());
                }

                if (meta.getCostType() != null) {
                    Map<String, Object> costTypes = new HashMap<String, Object>();
                    for (CostType type: meta.getCostType()) {
                        Map<String, Object> costType = new HashMap<String, Object>();
                        costType.put("cost-mode", type.getCostMode());
                        costType.put("cost-metric", type.getCostMetric().getValue());

                        //TODO Support 'description' field

                        costTypes.put(type.getName(), costType);
                    }

                    rfcIrd.meta.put("cost-types", costTypes);
                }
            }
        }

        List<IrdEntry> resourceList = ird.getIrdEntry();
        if (resourceList == null)
            return rfcIrd;

        for (IrdEntry entry: resourceList) {
            Map<String, Object> resource = new HashMap<String, Object>();

            if (entry.getLocation() instanceof FixedUrl) {
                FixedUrl url = (FixedUrl)entry.getLocation();
                resource.put("uri", url.getUri().getValue());
            } else if (entry.getLocation() instanceof RelativePath) {
                RelativePath relativePath = (RelativePath)entry.getLocation();
                String path = relativePath.getPath().getValue();

                String uri = req.getScheme() + "://" + req.getLocalName()
                                + ":" + req.getLocalPort() + path;

                resource.put("uri", uri);
            }

            if (entry.getAccepts() != null) {
                resource.put("accepts", entry.getAccepts());
            }

            if (entry.getMediaType() != null) {
                resource.put("media-type", entry.getMediaType());
            }

            if (entry.getEntryCapabilities() != null) {
                EntryCapabilities capabilities = entry.getEntryCapabilities();
                Map<String, Object> capabilityMap = new HashMap<String, Object>();

                Rfc7285CostTypeCapabilities costTypeCapabilities;
                costTypeCapabilities = capabilities.getAugmentation(Rfc7285CostTypeCapabilities.class);
                if (costTypeCapabilities != null) {
                    capabilityMap.put("cost-type-names", costTypeCapabilities.getCostTypeNames());
                    capabilityMap.put("cost-constraints", costTypeCapabilities.isCostConstraints());
                }

                if (!capabilityMap.isEmpty()) {
                    resource.put("capabilities", capabilityMap);
                }
            }

            if ((entry.getUses() != null) && (!entry.getUses().isEmpty())) {
                List<String> uses = new LinkedList<>();
                for (ResourceId rid: entry.getUses()) {
                    uses.add(rid.getValue());
                }
                resource.put("uses", uses);
            }

            rfcIrd.resources.put(entry.getEntryId().getValue(), resource);
        }
        return rfcIrd;
    }
}
