package org.opendaylight.alto.manager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.opendaylight.alto.commons.types.converter.RFC2ModelCostMapConverter;
import org.opendaylight.alto.commons.types.converter.RFC2ModelEndpointPropMapConverter;
import org.opendaylight.alto.commons.types.converter.RFC2ModelNetworkMapConverter;
import org.opendaylight.alto.commons.types.model150404.ModelCostMap;
import org.opendaylight.alto.commons.types.model150404.ModelEndpointPropertyMap;
import org.opendaylight.alto.commons.types.model150404.ModelJSONMapper;
import org.opendaylight.alto.commons.types.model150404.ModelNetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;
import org.opendaylight.alto.manager.AltoManagerConstants.MAP_FORMAT_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "alto", name = "create", description = "Create resource by resource type and resource file")
public class AltoCreate extends AltoManager {
  private static final Logger log = LoggerFactory.getLogger(AltoCreate.class);
  
  private RFC7285JSONMapper rfcMapper = new RFC7285JSONMapper();
  private ModelJSONMapper modelMapper = new ModelJSONMapper();
  private RFC2ModelNetworkMapConverter networkMapConverter = new RFC2ModelNetworkMapConverter();
  private RFC2ModelCostMapConverter costMapConverter = new RFC2ModelCostMapConverter();
  private RFC2ModelEndpointPropMapConverter endpointPropConverter = new RFC2ModelEndpointPropMapConverter();

  private String data;
  private MAP_FORMAT_TYPE format = MAP_FORMAT_TYPE.YANG;
  
  @Argument(index = 0, name = "resource-type", description = "Resource Type", required = true, multiValued = false)
  String resourceType = null;

  @Argument(index = 1, name = "resource-file", description = "Resource File", required = true, multiValued = false)
  String resourceFile = null;
  
  public AltoCreate() {
    super();
  }
  
  @Override
  protected Object doExecute() throws Exception {
    if (networkMapType().equals(resourceType)) {
      putNetworkMap();
    } else if (costMapType().equals(resourceType)) {
      putCostMap();
    } else if (endpointPropertyMapType().equals(resourceType)) {
      putEndpointPropertyMap();
    } else {
      log.warn("Not supported resource type " + resourceType + ". Aborting...");
    }
    return null;
  }
  
  private void putNetworkMap() throws Exception {
    log.info("Loading network map from " + this.resourceFile);
    ModelNetworkMap networkMap = getYangNetworkMap(readFromFile(resourceFile));
    data = modelMapper.asJSON(networkMap);
    putMap(AltoManagerConstants.NETWORK_MAP_URL, networkMap.rid);
  }
  
  private ModelNetworkMap getYangNetworkMap(String data) throws Exception {
    if (MAP_FORMAT_TYPE.RFC.equals(format)) {
      return networkMapConverter.convert(rfcMapper.asNetworkMap(data));
    }
    return modelMapper.asNetworkMap(data);
  }
  
  private void putCostMap() throws Exception {
    log.info("Loading cost map from " + this.resourceFile);
    ModelCostMap costMap = getYangCostMap(data);
    data = modelMapper.asJSON(costMap);
    putMap(AltoManagerConstants.COST_MAP_URL, costMap.rid);
  }
  
  private ModelCostMap getYangCostMap(String data) throws Exception {
    if (MAP_FORMAT_TYPE.RFC.equals(format)) {
      return costMapConverter.convert(rfcMapper.asCostMap(data));
    }
    return modelMapper.asCostMap(data);
  }
  
  private void putEndpointPropertyMap() throws Exception {
    log.info("Loading endpoint property map from " + this.resourceFile);
    ModelEndpointPropertyMap endpointPropMap = getYangEndpointPropMap(readFromFile(resourceFile));
    data = modelMapper.asJSON(endpointPropMap);
    putMap(AltoManagerConstants.RESOURCES_URL, AltoManagerConstants.ENDPOINT_PROPERTY_MAP_NODE);
  }
  
  private ModelEndpointPropertyMap getYangEndpointPropMap(String data) throws Exception {
    if (MAP_FORMAT_TYPE.RFC.equals(format)) {
       endpointPropConverter.convert(rfcMapper.asEndpointPropMap(data));
    }
    return modelMapper.asEndpointPropMap(data);
  }
  
  private void putMap(String baseUrl, String resourceId) throws Exception {
    if (resourceId == null) {
      log.info("Cannot parse resourceId. Aborting");
      return;
    }
    httpPut(baseUrl + resourceId, wrapdata());
  }
  
  private String wrapdata() throws Exception {
    if (endpointPropertyMapType().equals(resourceType)) {
      return "{\"alto-service:resourceType\":" + data + "}";
    } else {
      return "{\"alto-service:resourceType\":[" + data + "]}";
    }
  }
}
