package org.opendaylight.alto.commons.types.model150404;

import java.util.LinkedList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.MapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.map.DstCosts;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.PidName;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelCostMapData implements Map {

  @JsonProperty("alto-service:src")
  public String src = null;
  
  @JsonProperty("alto-service:dst-costs")
  public List<ModelDstCosts> dstCosts = new LinkedList<ModelDstCosts>();
  
  @JsonIgnore
  @Override
  public Class<? extends DataContainer> getImplementedInterface() {
    return Map.class;
  }

  @JsonIgnore
  @Override
  public <E extends Augmentation<Map>> E getAugmentation(Class<E> arg0) {
    return null;
  }

  @JsonIgnore
  @Override
  public PidName getSrc() {
    return new PidName(src);
  }

  @JsonIgnore
  @Override
  public List<DstCosts> getDstCosts() {
    return new LinkedList<DstCosts>(dstCosts);
  }
  
  @JsonIgnore
  @Override
  public MapKey getKey() {
    return new MapKey(getSrc());
  }

}
