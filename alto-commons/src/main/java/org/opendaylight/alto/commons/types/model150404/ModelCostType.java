package org.opendaylight.alto.commons.types.model150404;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMetricBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.cost.map.meta.CostType;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelCostType implements CostType {

  @JsonIgnore
  public static final String NUMERICAL = "Numerical";
  
  @JsonIgnore
  public static final String ORDINAL = "Ordinal";
  
  @JsonProperty("alto-service:cost-mode")
  public String costMode = null;
  
  @JsonProperty("alto-service:cost-metric")
  public String costMetric = null;
  
  @JsonProperty("alto-service:description")
  public String description = null;
  
  @JsonIgnore
  @Override
  public Class<? extends DataContainer> getImplementedInterface() {
    return CostType.class;
  }

  @JsonIgnore
  @Override
  public <E extends Augmentation<CostType>> E getAugmentation(Class<E> arg0) {
    return null;
  }

  @JsonIgnore
  @Override
  public CostMode getCostMode() {
    switch(costMode) {
    case NUMERICAL:
      return CostMode.Numerical;
    case ORDINAL:
      return CostMode.Ordinal;
    default:
      throw new RuntimeException("Non-Supported cost mode."); 
    }
  }

  @JsonIgnore
  @Override
  public CostMetric getCostMetric() {
    return CostMetricBuilder.getDefaultInstance(costMetric);
  }

  @JsonIgnore
  @Override
  public String getDescription() {
    return description;
  }

}
