package org.opendaylight.alto.commons.types.rfc7285;

import java.util.Map;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Extensible {

    @JsonIgnore
    private Map<String, Object> extra = new LinkedHashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> any() {
        return extra;
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        extra.put(name, value);
    }
}
