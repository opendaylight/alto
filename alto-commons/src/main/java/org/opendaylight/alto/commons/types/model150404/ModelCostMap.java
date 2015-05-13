package org.opendaylight.alto.commons.types.model150404;

import java.util.LinkedList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.CostMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Meta;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TagString;
import org.opendaylight.yangtools.yang.binding.DataContainer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelCostMap implements CostMap {

  @JsonProperty("alto-service:resource-id")
  public String rid = null;

  @JsonProperty("alto-service:tag")
  public String tag = null;

  @JsonProperty("alto-service:meta")
  public ModelCostMapMeta meta = null;

  @JsonProperty("alto-service:map")
  public List<ModelCostMapData> map = new LinkedList<ModelCostMapData>();

  @JsonIgnore
  @Override
  public Class<? extends DataContainer> getImplementedInterface() {
    return CostMap.class;
  }

  @JsonIgnore
  @Override
  public ResourceId getResourceId() {
    return new ResourceId(rid);
  }

  @JsonIgnore
  @Override
  public TagString getTag() {
    return new TagString(tag);
  }

  @JsonIgnore
  @Override
  public Meta getMeta() {
    return meta;
  }

  @JsonIgnore
  @Override
  public List<Map> getMap() {
    return new LinkedList<Map>(map);
  }

}
