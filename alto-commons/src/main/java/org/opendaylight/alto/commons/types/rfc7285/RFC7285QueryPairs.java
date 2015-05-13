package org.opendaylight.alto.commons.types.rfc7285;

import java.util.List;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RFC7285QueryPairs {

    @JsonProperty("srcs")
    public List<String> src = new LinkedList<String>();

    @JsonProperty("dsts")
    public List<String> dst = new LinkedList<String>();

}

