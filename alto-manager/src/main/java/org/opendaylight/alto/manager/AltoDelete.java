package org.opendaylight.alto.manager;

import java.io.IOException;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "alto", name = "delete", description = "Destroy a map by resourceId")
public class AltoDelete extends AltoManager {
  private static final Logger log = LoggerFactory.getLogger(AltoDelete.class);

  @Argument(index = 0, name = "resource-type", description = "Resource Type", required = true, multiValued = false)
  String resourceType = null;
  
  @Argument(index = 1, name = "resource-id", description = "Resource Id", required = false, multiValued = false)
  String resourceId = null;
  
  public AltoDelete() {
    super();
  }
  
  @Override
  protected Object doExecute() throws Exception {
    checkResourceID();
    if (networkMapType().equals(resourceType)) {
      deleteNetworkMap();
    } else if (costMapType().equals(resourceType)) {
      deleteCostMap();
    } else if (endpointPropertyMapType().equals(resourceType)) {
      deleteEndpointPropertyMap();
    } else {
      throw new UnsupportedOperationException("Unsupported resource type \"" + resourceType + "\".");
    }
    return null;
  }
  
  private void checkResourceID() throws IOException {
    if (networkMapType().equals(resourceType) && isDefaultNetworkMap(resourceId)) {
      throw new RuntimeException("Cannot destroy default network map.");
    }
    
    if (resourceId == null && !endpointPropertyMapType().equals(resourceType)) {
      throw new RuntimeException("Please specify resource id for " + resourceType + ".");
    }
    
    if (resourceId != null && endpointPropertyMapType().equals(resourceType)) {
      throw new RuntimeException("Please do not specify resource id for " + resourceType + ".");
    }
  }
  
  private boolean deleteEndpointPropertyMap() throws IOException {
    log.info("Deleting endpoint property map " + this.resourceId);
    return httpDelete(AltoManagerConstants.RESOURCES_URL + AltoManagerConstants.ENDPOINT_PROPERTY_MAP_NODE);
  }
  
  private boolean deleteCostMap() throws IOException {
    log.info("Deleting endpoint property map " + this.resourceId);
    return httpDelete(AltoManagerConstants.COST_MAP_URL + resourceId);
  }
  
  private boolean deleteNetworkMap() throws IOException {
    log.info("Deleting endpoint property map " + this.resourceId);
    return httpDelete(AltoManagerConstants.NETWORK_MAP_URL + resourceId);
  }
}
