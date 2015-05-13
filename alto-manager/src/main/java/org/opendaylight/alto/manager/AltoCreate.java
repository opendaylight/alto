package org.opendaylight.alto.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.opendaylight.alto.commons.types.converter.RFC2ModelCostMapConverter;
import org.opendaylight.alto.commons.types.converter.RFC2ModelEndpointPropMapConverter;
import org.opendaylight.alto.commons.types.converter.RFC2ModelNetworkMapConverter;
import org.opendaylight.alto.commons.types.model150404.ModelCostMap;
import org.opendaylight.alto.commons.types.model150404.ModelEndpointPropertyMap;
import org.opendaylight.alto.commons.types.model150404.ModelJSONMapper;
import org.opendaylight.alto.commons.types.model150404.ModelNetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
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
  private MAP_FORMAT_TYPE format = MAP_FORMAT_TYPE.RFC;

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
      putNetworkMaps();
    } else if (costMapType().equals(resourceType)) {
      putCostMaps();
    } else if (endpointPropertyMapType().equals(resourceType)) {
      putEndpointPropertyMap();
    } else {
      throw new UnsupportedOperationException("Unsupported resource type \"" + resourceType + "\".");
    }
    return null;
  }

  private void putNetworkMaps() throws Exception {
    log.info("Loading network maps from " + this.resourceFile);
    List<ModelNetworkMap> networkMaps = getYangNetworkMaps(readFromFile(resourceFile));
    for (ModelNetworkMap map : networkMaps) {
      log.info("Putting network map \"" + map.rid + "\"...");
      String data = modelMapper.asJSON(map);
      putMap(AltoManagerConstants.NETWORK_MAP_URL, map.rid, data);
    }
  }

  private List<ModelNetworkMap> getYangNetworkMaps(String data) throws Exception {
    if (MAP_FORMAT_TYPE.RFC.equals(format)) {
      List<ModelNetworkMap> modelNetworkMaps = new ArrayList<ModelNetworkMap>();
      for (RFC7285NetworkMap networkMap : rfcMapper.asNetworkMapList(data)) {
        modelNetworkMaps.add(networkMapConverter.convert(networkMap));
      }
      return modelNetworkMaps;
    }
    return modelMapper.asNetworkMapList(data);
  }

  private void putCostMaps() throws Exception {
    log.info("Loading cost map from " + this.resourceFile);
    List<ModelCostMap> costMaps = getYangCostMaps(readFromFile(resourceFile));
    for (ModelCostMap map : costMaps) {
      log.info("Putting cost map " + map.rid + "...");
      String data = modelMapper.asJSON(map);
      putMap(AltoManagerConstants.COST_MAP_URL, map.rid, data);
    }
  }

  private List<ModelCostMap> getYangCostMaps(String data) throws Exception {
    if (MAP_FORMAT_TYPE.RFC.equals(format)) {
      List<ModelCostMap> modelCostMaps = new ArrayList<ModelCostMap>();
      for (RFC7285CostMap costMap : rfcMapper.asCostMapList(data)) {
        modelCostMaps.add(costMapConverter.convert(costMap));
      }
      return modelCostMaps;
    }
    return modelMapper.asCostMapList(data);
  }

  private void putEndpointPropertyMap() throws Exception {
    log.info("Loading endpoint property map from " + this.resourceFile);
    ModelEndpointPropertyMap endpointPropMap = getYangEndpointPropMap(readFromFile(resourceFile));
    String data = modelMapper.asJSON(endpointPropMap);
    httpPut(AltoManagerConstants.ENDPOINT_PROP_MAP_URL, wrapdata(data));
  }

  private ModelEndpointPropertyMap getYangEndpointPropMap(String data) throws Exception {
    if (MAP_FORMAT_TYPE.RFC.equals(format)) {
       return endpointPropConverter.convert(rfcMapper.asEndpointPropMap(data));
    }
    return modelMapper.asEndpointPropMap(data);
  }

  private void putMap(String baseUrl, String resourceId, String data) throws Exception {
    if (resourceId == null) {
      throw new RuntimeException("No ResourceId Specified.");
    }
    httpPut(baseUrl + resourceId, wrapdata(data));
  }

  private String wrapdata(String data) throws Exception {
    if (endpointPropertyMapType().equals(resourceType)) {
      return "{\"alto-service:" + resourceType  + "\":" + data + "}";
    } else {
      return "{\"alto-service:" + resourceType  + "\":[" + data + "]}";
    }
  }
}
