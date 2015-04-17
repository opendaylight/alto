package org.opendaylight.alto.commons.types.model150404;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TagString;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map;

import java.util.List;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ModelNetworkMap implements NetworkMap {

    @JsonIgnore
    protected ResourceId rid = null;

    @JsonIgnore
    protected TagString tag = null;

    @JsonIgnore
    protected List<Map> data = new LinkedList<Map>();

    public ModelNetworkMap() {
    }

    public ModelNetworkMap(NetworkMap map) {
        this.rid = map.getResourceId();
        this.tag = map.getTag();
        this.setMap(map.getMap());
    }

    @JsonIgnore
    @Override
    public Class<NetworkMap> getImplementedInterface() {
        return NetworkMap.class;
    }

    @JsonIgnore
    @Override
    public ResourceId getResourceId() {
        return rid;
    }

    @JsonProperty("alto-service:resource-id")
    public String getJSONResourceId() {
        return rid.getValue();
    }

    @JsonProperty("alto-service:resource-id")
    public void setJSONResourceId(String rid) {
        this.rid = new ResourceId(rid);
    }

    @JsonIgnore
    @Override
    public TagString getTag() {
        return tag;
    }

    @JsonProperty("alto-service:tag")
    public String getJSONTag() {
        return tag.getValue();
    }

    @JsonProperty("alto-service:tag")
    public void setJSONTag(String tag) {
        this.tag = new TagString(tag);
    }

    @JsonIgnore
    @Override
    public List<Map> getMap() {
        return data;
    }

    @JsonIgnore
    public void setMap(List<Map> data) {
        this.data = new LinkedList<Map>(data);
    }

    @JsonProperty("alto-service:map")
    public List<Map> getJSONMap() {
        return data;
    }

    @JsonProperty("alto-service:map")
    public void setJSONMap(List<ModelEndpoint> data) {
        this.data = new LinkedList<Map>(data);
    }
}
