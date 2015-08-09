/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.model150404;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TagString;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map;

import java.util.List;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ModelNetworkMap implements NetworkMap {

    @JsonProperty("alto-service:resource-id")
    public String rid = null;

    @JsonProperty("alto-service:tag")
    public String tag = null;

    @JsonProperty("alto-service:map")
    public List<ModelEndpoint> map = new LinkedList<ModelEndpoint>();

    @JsonIgnore
    @Override
    public Class<NetworkMap> getImplementedInterface() {
        return NetworkMap.class;
    }

    @JsonIgnore
    @Override
    public ResourceId getResourceId() {
        return new ResourceId(rid);
    }

    @JsonIgnore
    @Override
    public TagString getTag() {
        return new TagString(tag);
    }

    @JsonIgnore
    @Override
    public List<Map> getMap() {
        return new LinkedList<Map>(map);
    }
}
