package org.opendaylight.alto.commons.types.rfc7285;

import java.util.List;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryPairs {

    @JsonProperty("srcs")
    List<String> src = new LinkedList<String>();

    @JsonProperty("dsts")
    List<String> dst = new LinkedList<String>();

}

