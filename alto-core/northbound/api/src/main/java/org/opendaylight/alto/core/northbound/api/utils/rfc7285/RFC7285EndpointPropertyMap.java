/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.api.utils.rfc7285;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RFC7285EndpointPropertyMap {

  public static class Meta extends Extensible {
    @JsonProperty("dependent-vtags")
    public List<RFC7285VersionTag> netmap_tags;
  }

  @JsonProperty("meta")
  public Meta meta;

  @JsonProperty("endpoint-properties")
  public Map<String, Map<String, String>> map
    = new LinkedHashMap<String, Map<String, String>>();
}
