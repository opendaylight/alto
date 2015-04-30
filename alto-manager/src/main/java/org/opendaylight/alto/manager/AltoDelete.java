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
  
  @Argument(index = 1, name = "resource-id", description = "Resource Id", required = true, multiValued = false)
  String resourceId = null;
  
  public AltoDelete() {
    super();
  }
  
  @Override
  protected Object doExecute() throws Exception {
    if (isDefaultNetworkMap(resourceId)) {
      log.info("Cannot destroy default network map. Aborting");
    }
    
    if (networkMapType().equals(resourceType)) {
      deleteNetworkMap();
    } else if (costMapType().equals(resourceType)) {
      deleteCostMap();
    } else if (endpointPropertyMapType().equals(resourceType)) {
      deleteEndpointPropertyMap();
    } else {
      log.warn("Not supported resource type " + resourceType + ". Aborting...");
    }
    return null;
  }
  
  private boolean deleteEndpointPropertyMap() throws IOException {
    log.info("Deleting endpoint property map " + this.resourceId);
    return httpDelete(AltoManagerConstants.ENDPOINT_PROPERTY_MAP_URL + resourceId);
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
