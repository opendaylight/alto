package org.opendaylight.alto.commons.types.model150404;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TagString;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.dependent.vtags.DependentVtags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.dependent.vtags.DependentVtagsKey;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelDependentVtag implements DependentVtags {

  @JsonProperty("alto-service:resource-id")
  public String rid = "";
  
  @JsonProperty("alto-service:tag")
  public String vTag = "";
  
  @JsonIgnore
  @Override
  public Class<? extends DataContainer> getImplementedInterface() {
    return DependentVtags.class;
  }

  @JsonIgnore
  @Override
  public <E extends Augmentation<DependentVtags>> E getAugmentation(
      Class<E> arg0) {
    return null;
  }

  @JsonIgnore
  @Override
  public ResourceId getResourceId() {
    return new ResourceId(rid);
  }

  @JsonIgnore
  @Override
  public TagString getTag() {
    return new TagString(vTag);
  }

  @JsonIgnore
  @Override
  public DependentVtagsKey getKey() {
    return new DependentVtagsKey(getResourceId());
  }

}
