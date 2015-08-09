/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.rfc7285;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RFC7285IRD {

    public class Meta extends Extensible {

        @JsonProperty("default-alto-network-map")
        public String defaultAltoNetworkMap;

        @JsonProperty("cost-types")
        public Map<String, RFC7285CostType> costTypes;

        public Meta() {
            defaultAltoNetworkMap = null;
            costTypes = new LinkedHashMap<String, RFC7285CostType>();
        }

    }

    public class Entry {
        @JsonProperty("uri")
        public String uri;

        @JsonProperty("media-type")
        public String mediaType;

        @JsonProperty("accepts")
        public String accepts;

        @JsonProperty("capabilities")
        public Map<String, Object> capabilities;

        @JsonProperty("uses")
        public List<String> uses;
    }

    @JsonProperty("meta")
    public Meta meta;

    @JsonProperty("resources")
    public Map<String, Entry> resources = new LinkedHashMap<String, Entry>();
}
