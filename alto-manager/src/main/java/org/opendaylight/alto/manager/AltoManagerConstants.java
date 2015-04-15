package org.opendaylight.alto.manager;

public class AltoManagerConstants {
  public static final String HOST = "http://127.0.0.1:8181/restconf/config/";
  public static final String MODULE = "alto-service";
  public static final String JSON_CONTENT_TYPE = "application/yang.data+json";

  public static final String RESOURCES_LABEL = MODULE + ":resources";
  public static final String NETWORK_MAPS_LABEL = MODULE + ":network-maps";
  public static final String NETWORK_MAP_LABEL = MODULE + ":network-map";

  public static final String RESOURCE_ID_LABEL = MODULE + ":resource-id";

  public static final String RESOURCES_HOST = HOST + RESOURCES_LABEL + "/";
  public static final String NETWORK_MAPS_HOST = RESOURCES_HOST + NETWORK_MAPS_LABEL + "/";
  public static final String NETWORK_MAP_HOST = NETWORK_MAPS_HOST + NETWORK_MAP_LABEL + "/";

  public static final String DEFAULT_RESOURCES_PATH = "configuration/default.networkmap";

  public static enum SERVICE_TYPE{
    RESOURCE, NETWORK, COST
  }
}
