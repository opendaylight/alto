/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.api.utils.rfc7285;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RFC7285VersionTag {

    private final static char ILLEGAL = '$';

    @JsonProperty("resource-id")
    public String rid;

    @JsonProperty("tag")
    public String tag;

    public RFC7285VersionTag() {
        rid = "";
        tag = "";
    }

    public RFC7285VersionTag(String rid, String tag) {
        this.rid = (rid != null ? rid : "");
        this.tag = (tag != null ? tag : "");
    }

    public boolean incomplete() {
        return (rid == null) || (tag == null) || (rid == "") || (tag == "");
    }

    @Override
    public int hashCode() {
        return (rid + ILLEGAL + tag).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (this.getClass() != obj.getClass())
            return false;

        if (this == obj)
            return true;

        RFC7285VersionTag other = (RFC7285VersionTag)obj;
        boolean ridTest = (rid == null ? (other.rid == null) : rid.equals(other.rid));
        boolean tagTest = (tag == null ? (other.tag == null) : tag.equals(other.tag));
        return (ridTest && tagTest);
    }
}
