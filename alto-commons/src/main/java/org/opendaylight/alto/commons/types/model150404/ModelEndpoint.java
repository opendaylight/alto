package org.opendaylight.alto.commons.types.model150404;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.MapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yangtools.yang.binding.Augmentation;

import java.util.List;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ModelEndpoint implements Map {

    @JsonProperty("alto-service:pid")
    public String pid = null;

    @JsonProperty("alto-service:endpoint-address-group")
    public List<ModelEndpointAddressGroup> addressGroup = new LinkedList<ModelEndpointAddressGroup>();

    @JsonIgnore
    @Override
    public Class<Map> getImplementedInterface() {
        return Map.class;
    }

    @JsonIgnore
    @Override
    public <E extends Augmentation<Map>> E getAugmentation(Class<E> augmentationType) {
        return null;
    }

    @JsonIgnore
    @Override
    public MapKey getKey() {
        return new MapKey(getPid());
    }

    @JsonIgnore
    @Override
    public PidName getPid() {
        return new PidName(pid);
    }

    @JsonIgnore
    @Override
    public List<EndpointAddressGroup> getEndpointAddressGroup() {
        return new LinkedList<EndpointAddressGroup>(addressGroup);
    }
}
