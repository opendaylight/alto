package org.opendaylight.alto.manager;

import java.io.IOException;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.opendaylight.alto.commons.types.converter.RFC2ModelNetworkMapConverter;
import org.opendaylight.alto.commons.types.model150404.ModelJSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "alto", name = "create", description = "Create resource by resource type and resource file")
public class AltoCreate extends AltoManager {
  private static final Logger log = LoggerFactory.getLogger(AltoCreate.class);
  
  private RFC7285JSONMapper rfcMapper = new RFC7285JSONMapper();
  private ModelJSONMapper modelMapper = new ModelJSONMapper();
  private RFC2ModelNetworkMapConverter converter = new RFC2ModelNetworkMapConverter();
  private String data;
  
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
    RFC7285NetworkMap rfcNetworkMap = rfcMapper.asNetworkMap(readFromFile(resourceFile));
    data = createNetworkMapData(rfcNetworkMap);
    putMap(AltoManagerConstants.NETWORK_MAP_URL, rfcNetworkMap.meta.vtag.rid);
  }
  
  private String createNetworkMapData(RFC7285NetworkMap rfcNetworkMap) throws Exception {
    String networkMapJson = modelMapper.asJSON(converter.convert(rfcNetworkMap));
    return "{\"alto-service:network-map\":[" + networkMapJson + "]}";
  }
  
  private void putCostMap() throws Exception {
    log.info("Loading cost map from " + this.resourceFile);
    RFC7285CostMap costMap = rfcMapper.asCostMap(readFromFile(resourceFile));
    String resourceId = getCostMapResourceId(costMap);
    //TODO: transform it to yang format json
    data = "";
    putMap(AltoManagerConstants.COST_MAP_URL, resourceId);
  }
  
  private void putEndpointPropertyMap() throws IOException {
    log.info("Loading endpoint property map from " + this.resourceFile);
    //TODO: get RFC7285EndpointPropertyMap object and transform it to yang format json
    data = "";
    putMap(AltoManagerConstants.ENDPOINT_PROPERTY_MAP_URL, AltoManagerConstants.ENDPOINT_PROPERTY_MAP_NAME);
  }
  
  private void putMap(String baseUrl, String resourceId) throws IOException {
    if (resourceId == null) {
      log.info("Cannot parse resourceId. Aborting");
      return;
    }
    httpPut(baseUrl + resourceId, data);
  }
  
  private String getCostMapResourceId(RFC7285CostMap costMap) throws Exception {
    String networkMapRID = costMap.meta.netmap_tags.get(0).rid;
    String costMetric = costMap.meta.costType.metric;
    String costMode = costMap.meta.costType.mode;
    return networkMapRID + AltoManagerConstants.DELIMETER + costMetric
         + AltoManagerConstants.DELIMETER + costMode;
  }
}
