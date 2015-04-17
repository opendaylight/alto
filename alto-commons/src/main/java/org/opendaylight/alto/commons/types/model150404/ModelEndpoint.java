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

    @JsonIgnore
    protected PidName pid = null;

    @JsonIgnore
    protected List<EndpointAddressGroup> data = new LinkedList<EndpointAddressGroup>();

    public ModelEndpoint() {
    }

    public ModelEndpoint(Map model) {
        this.pid = model.getPid();
        this.setEndpointAddressGroup(model.getEndpointAddressGroup());
    }

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
        return new MapKey(pid);
    }

    @JsonIgnore
    @Override
    public PidName getPid() {
        return pid;
    }

    @JsonProperty("alto-service:pid")
    public String getJSONPid() {
        return pid.getValue();
    }

    @JsonProperty("alto-service:pid")
    public void setJSONPid(String pid) {
        this.pid = new PidName(pid);
    }

    @JsonIgnore
    @Override
    public List<EndpointAddressGroup> getEndpointAddressGroup() {
        return data;
    }

    @JsonIgnore
    public void setEndpointAddressGroup(List<EndpointAddressGroup> rhs) {
        data = new LinkedList<EndpointAddressGroup>(rhs);
    }

    @JsonProperty("alto-service:endpoint-address-group")
    public List<EndpointAddressGroup> getJSONEndpointAddressGroup() {
        return data;
    }

    @JsonProperty("alto-service:endpoint-address-group")
    public void setJSONEndpointAddressGroup(List<ModelEndpointAddressGroup> rhs) {
        data = new LinkedList<EndpointAddressGroup>(rhs);
    }
}
