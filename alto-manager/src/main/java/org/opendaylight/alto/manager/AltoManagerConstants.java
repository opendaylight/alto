package org.opendaylight.alto.manager;

public class AltoManagerConstants {
  public static final String HOST = "http://127.0.0.1:8181/restconf/config/";
  public static final String MODULE = "alto-service";
  public static final String JSON_CONTENT_TYPE = "application/yang.data+json";
  
  public static final String RESOURCES_NODE = MODULE + ":resources";
  public static final String IRD_NODE = MODULE + ":IRD";
  public static final String META_NODE = MODULE + ":meta";
  public static final String DEFAULT_NETWORK_MAP_NODE = MODULE + ":default-alto-network-map";
  
  public static final String NETWORK_MAPS_NODE = MODULE + ":network-maps";
  public static final String NETWORK_MAP_NODE = MODULE + ":network-map";
  public static final String COST_MAPS_NODE = MODULE + ":cost-maps";
  public static final String COST_MAP_NODE = MODULE + ":cost-map";
  public static final String ENDPOINT_PROPERTY_MAP_NODE = MODULE + ":endpoint-property-map";
  public static final String RESOURCE_ID_NODE = MODULE + ":resource-id";

  public static final String RESOURCES_URL = HOST + RESOURCES_NODE + "/";
  public static final String NETWORK_MAP_URL = RESOURCES_URL + NETWORK_MAPS_NODE + "/" + NETWORK_MAP_NODE + "/";
  public static final String COST_MAP_URL = RESOURCES_URL + COST_MAPS_NODE + "/" + COST_MAP_NODE + "/";
  public static final String IRD_DEFAULT_NETWORK_MAP_URL = RESOURCES_URL + IRD_NODE + "/" + META_NODE + "/" + DEFAULT_NETWORK_MAP_NODE + "/";
  
  public static final String DEFAULT_NETWORK_MAP_PROPERTY = "default-network-map";
  public static final String DELIMETER = "-";
  
  public static enum COST_MODE {
    Numerical, Ordinal
  }
  
  public static enum SERVICE_TYPE{
    NETWORK_MAP, COST_MAP, ENDPOINT_PROPERTY_MAP
  }
  
  public static enum MAP_FORMAT_TYPE {
    YANG, RFC
  }
}