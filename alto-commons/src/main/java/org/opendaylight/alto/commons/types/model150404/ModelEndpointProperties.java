/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.model150404;

import java.util.LinkedList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TypedEndpointAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.data.EndpointProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.data.EndpointPropertiesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.data.endpoint.properties.Properties;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelEndpointProperties implements EndpointProperties {

  @JsonProperty("alto-service:endpoint")
  public String endpoint = null;

  @JsonProperty("alto-service:properties")
  public List<ModelProperties> properties = new LinkedList<ModelProperties>();

  @JsonIgnore
  @Override
  public Class<? extends DataContainer> getImplementedInterface() {
    return EndpointProperties.class;
  }

  @JsonIgnore
  @Override
  public <E extends Augmentation<EndpointProperties>> E getAugmentation(
      Class<E> arg0) {
    return null;
  }

  @JsonIgnore
  @Override
  public TypedEndpointAddress getEndpoint() {
    return new TypedEndpointAddress(endpoint.toCharArray());
  }

  @JsonIgnore
  @Override
  public List<Properties> getProperties() {
    return new LinkedList<Properties>(properties);
  }

  @JsonIgnore
  @Override
  public EndpointPropertiesKey getKey() {
    return new EndpointPropertiesKey(getEndpoint());
  }

}
