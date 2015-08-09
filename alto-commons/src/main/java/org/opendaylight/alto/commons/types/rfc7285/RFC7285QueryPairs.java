/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

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

