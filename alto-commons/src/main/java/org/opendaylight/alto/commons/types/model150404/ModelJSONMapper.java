/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.model150404;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModelJSONMapper {

    private ObjectMapper mapper = new ObjectMapper()
                            .setSerializationInclusion(Include.NON_DEFAULT)
                            .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);


    public ModelNetworkMap asNetworkMap(String json) throws Exception {
      return mapper.readValue(json, ModelNetworkMap.class);
    }

    public List<ModelNetworkMap> asNetworkMapList(String json) throws Exception {
      return Arrays.asList(mapper.readValue(json, ModelNetworkMap[].class));
    }

    public ModelCostMap asCostMap(String json) throws Exception {
      return mapper.readValue(json, ModelCostMap.class);
    }

    public List<ModelCostMap> asCostMapList(String json) throws Exception {
      return Arrays.asList(mapper.readValue(json, ModelCostMap[].class));
    }

    public ModelEndpointPropertyMap asEndpointPropMap(String json) throws Exception {
      return mapper.readValue(json, ModelEndpointPropertyMap.class);
    }

    public ModelEndpoint asModelEndpoint(String json) throws IOException {
      return mapper.readValue(json, ModelEndpoint.class);
    }

    public String asJSON(Object obj) throws Exception {
      return mapper.writeValueAsString(obj);
    }
}
