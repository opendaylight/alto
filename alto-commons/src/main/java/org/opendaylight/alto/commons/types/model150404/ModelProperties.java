package org.opendaylight.alto.commons.types.model150404;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.EndpointPropertyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.EndpointPropertyValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.data.endpoint.properties.Properties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.data.endpoint.properties.PropertiesKey;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelProperties implements Properties {

  @JsonProperty("alto-service:property-type")
  public String propertyType = null;

  @JsonProperty("alto-service:property")
  public String propertyValue = null;

  @JsonIgnore
  @Override
  public Class<? extends DataContainer> getImplementedInterface() {
    return Properties.class;
  }

  @JsonIgnore
  @Override
  public <E extends Augmentation<Properties>> E getAugmentation(Class<E> arg0) {
    return null;
  }

  @JsonIgnore
  @Override
  public EndpointPropertyType getPropertyType() {
    return new EndpointPropertyType(propertyType.toCharArray());
  }

  @JsonIgnore
  @Override
  public EndpointPropertyValue getProperty() {
    return new EndpointPropertyValue(propertyValue);
  }

  @JsonIgnore
  @Override
  public PropertiesKey getKey() {
    return new PropertiesKey(getPropertyType());
  }

}
