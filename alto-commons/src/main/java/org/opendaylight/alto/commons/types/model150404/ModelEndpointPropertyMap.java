package org.opendaylight.alto.commons.types.model150404;

import java.util.LinkedList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.EndpointPropertyMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.Meta;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.data.EndpointProperties;
import org.opendaylight.yangtools.yang.binding.DataContainer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelEndpointPropertyMap implements EndpointPropertyMap {

  @JsonProperty("alto-service:meta")
  public ModelEndpointPropertyMeta endpointPropertyMeta = new ModelEndpointPropertyMeta();

  @JsonProperty("alto-service:endpoint-properties")
  public List<ModelEndpointProperties> properties = new LinkedList<ModelEndpointProperties>();

  @JsonIgnore
  @Override
  public Class<? extends DataContainer> getImplementedInterface() {
    return EndpointPropertyMap.class;
  }

  @JsonIgnore
  @Override
  public List<EndpointProperties> getEndpointProperties() {
    return new LinkedList<EndpointProperties>(properties);
  }

  @JsonIgnore
  @Override
  public Meta getMeta() {
    return endpointPropertyMeta;
  }
}
