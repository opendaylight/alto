package org.opendaylight.alto.commons.types.converter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Map;

import org.opendaylight.alto.commons.helper.Converter;
import org.opendaylight.alto.commons.types.model150404.ModelDependentVtag;
import org.opendaylight.alto.commons.types.model150404.ModelEndpointProperties;
import org.opendaylight.alto.commons.types.model150404.ModelEndpointPropertyMap;
import org.opendaylight.alto.commons.types.model150404.ModelEndpointPropertyMeta;
import org.opendaylight.alto.commons.types.model150404.ModelProperties;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285EndpointPropertyMap;

public class RFC2ModelEndpointPropMapConverter 
  extends Converter<RFC7285EndpointPropertyMap, ModelEndpointPropertyMap> {
  
  @Override
  protected Object _convert() {
    ModelEndpointPropertyMap out = new ModelEndpointPropertyMap();
    out.endpointPropertyMeta = convertMeta(in().meta);
    out.properties = new LinkedList<ModelEndpointProperties>();
    for (String endpoint : in().map.keySet()) {
      out.properties.add(convertEndpointProperty(endpoint, in().map.get(endpoint)));
    }
    return out;
  }
  
  protected static String readFromFile(String path) throws IOException {
    return new String(Files.readAllBytes(Paths.get(path)),
        StandardCharsets.UTF_8);
  }
  
  private ModelEndpointPropertyMeta convertMeta(RFC7285EndpointPropertyMap.Meta meta) {
    ModelEndpointPropertyMeta endpointPropertyMeta = new ModelEndpointPropertyMeta();
    
    endpointPropertyMeta.dependentVtags = new LinkedList<ModelDependentVtag>();
    ModelDependentVtag dependentVtag = new ModelDependentVtag();
    dependentVtag.rid = meta.netmap_tags.get(0).rid;
    dependentVtag.vTag = meta.netmap_tags.get(0).tag;
    
    endpointPropertyMeta.dependentVtags.add(dependentVtag);
    return endpointPropertyMeta;
  }
  
  private ModelEndpointProperties 
    convertEndpointProperty(String endpoint, Map<String, String> property) {
      ModelEndpointProperties endpointProperty = new ModelEndpointProperties();
      endpointProperty.endpoint = endpoint;
      endpointProperty.properties = new LinkedList<ModelProperties>();
      for (String propertyType : property.keySet()) {
        endpointProperty.properties.add(convertProperty(propertyType, property.get(propertyType)));
      }
      return endpointProperty;
  }
  
  private ModelProperties convertProperty(String propertyType, String propertyValue) {
    ModelProperties property = new ModelProperties();
    property.propertyType = propertyType;
    property.propertyValue = propertyValue;
    return property;
  }

}
