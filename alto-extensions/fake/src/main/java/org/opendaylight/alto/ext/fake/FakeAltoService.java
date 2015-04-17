package org.opendaylight.alto.ext.fake;

import org.opendaylight.alto.services.api.rfc7285.AltoService;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostType;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285IRD;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint;

public class FakeAltoService implements AltoService {

    private RFC7285JSONMapper mapper = new RFC7285JSONMapper();

    private static final String NETWORKMAP_JSON =
            "{"
        +       "\"meta\" : {"
        +           "\"vtag\": {"
        +               "\"resource-id\": \"default-networkmap\","
        +               "\"tag\": \"3ee2cb7e8d63d9fab71b9b34cbf764436315542e\""
        +           "}"
        +       "},"
        +       "\"network-map\" : {"
        +           "\"PID1\" : {"
        +               "\"ipv4\" : ["
        +                   "\"192.0.2.0/24\","
        +                   "\"198.51.100.0/25\""
        +               "]"
        +           "},"
        +           "\"PID2\" : {"
        +               "\"ipv4\" : ["
        +                   "\"198.51.100.128/25\""
        +               "]"
        +           "},"
        +           "\"PID3\" : {"
        +               "\"ipv4\" : ["
        +                   "\"0.0.0.0/0\""
        +               "],"
        +               "\"ipv6\" : ["
        +                   "\"::/0\""
        +               "]"
        +           "}"
        +       "}"
        +   "}";

    private static final String DEFAULT_NETWORKMAP_ID = "default-networkmap";
    private static final String DEFAULT_NETWORKMAP_TAG = "3ee2cb7e8d63d9fab71b9b34cbf764436315542e";
    private RFC7285NetworkMap networkMap = null;

    private static final String COSTMAP_JSON =
            "{"
        +       "\"meta\" : {"
        +           "\"dependent-vtags\" : ["
        +               "{"
        +                   "\"resource-id\": \"default-networkmap\","
        +                   "\"tag\": \"3ee2cb7e8d63d9fab71b9b34cbf764436315542e\""
        +               "}"
        +           "],"
        +           "\"cost-type\" : {"
        +               "\"cost-mode\" : \"numerical\","
        +               "\"cost-metric\": \"routingcost\""
        +           "}"
        +       "},"
        +       "\"cost-map\" : {"
        +           "\"PID1\": { \"PID1\": 1, \"PID2\": 5, \"PID3\": 10 },"
        +           "\"PID2\": { \"PID1\": 5, \"PID2\": 1, \"PID3\": 15 },"
        +           "\"PID3\": { \"PID1\": 20, \"PID2\": 15 }"
        +       "}"
        +   "}";

    private static final String DEFAULT_COSTMAP_ID = "default-costmap";

    private RFC7285CostMap costMap = null;

    public FakeAltoService() {
        try {
            networkMap = mapper.asNetworkMap(NETWORKMAP_JSON);
            costMap = mapper.asCostMap(COSTMAP_JSON);
        } catch (Exception e) {
            networkMap = null;
            costMap = null;
        }
    }

    public RFC7285CostMap getCostMap(String id) {
        if (!DEFAULT_COSTMAP_ID.equals(id))
            return null;
        return costMap;
    }

    public RFC7285CostMap getCostMap(RFC7285VersionTag vtag) {
        return getCostMap(vtag.rid);
    }

    public RFC7285CostMap getCostMap(String id, RFC7285CostType type) {
        if (!DEFAULT_COSTMAP_ID.equals(id))
            return null;
        if (!(costMap.meta.costType.equals(type)))
            return null;
        return costMap;
    }

    public RFC7285CostMap getCostMap(RFC7285VersionTag vtag, RFC7285CostType type) {
        return getCostMap(vtag.rid, type);
    }

    public RFC7285CostMap getCostMap(String id, RFC7285CostMap.Filter filter) {
        return null;
    }

    public RFC7285CostMap getCostMap(RFC7285VersionTag vtag, RFC7285CostMap.Filter filter) {
        return null;
    }

    public Boolean supportCostType(String id, RFC7285CostType type) {
        return null;
    }

    public Boolean supportCostType(RFC7285VersionTag vtag, RFC7285CostType type) {
        if (!DEFAULT_COSTMAP_ID.equals(vtag.rid))
            return new Boolean(false);
        if (!costMap.meta.costType.equals(type))
            return new Boolean(false);
        return new Boolean(true);
    }

    public Boolean validateCostMapFilter(String id, RFC7285CostMap.Filter filter) {
        return false;
    }

    public Boolean validateCostMapFilter(RFC7285VersionTag vtag, RFC7285CostMap.Filter filter) {
        return false;
    }


    public RFC7285NetworkMap getDefaultNetworkMap() {
        return networkMap;
    }

    public RFC7285NetworkMap getNetworkMap(String id) {
        if (!DEFAULT_NETWORKMAP_ID.equals(id))
            return null;
        return networkMap;
    }

    public RFC7285NetworkMap getNetworkMap(RFC7285VersionTag vtag) {
        if (!DEFAULT_NETWORKMAP_ID.equals(vtag.rid))
            return null;
        if (!DEFAULT_NETWORKMAP_TAG.equals(vtag.tag))
            return null;
        return networkMap;
    }

    public RFC7285NetworkMap getNetworkMap(String id, RFC7285NetworkMap.Filter filter) {
        return null;
    }

    public RFC7285NetworkMap getNetworkMap(RFC7285VersionTag vtag, RFC7285NetworkMap.Filter filter) {
        return null;
    }

    public Boolean validateNetworkMapFilter(String id, RFC7285NetworkMap.Filter filter) {
        return new Boolean(false);
    }

    public Boolean validateNetworkMapFilter(RFC7285VersionTag vtag, RFC7285NetworkMap.Filter filter) {
        return new Boolean(false);
    }

    public RFC7285IRD getDefaultIRD() {
        return null;
    }

    public RFC7285IRD getIRD(String id) {
        return null;
    }

    public RFC7285Endpoint.CostResponse getEndpointCost(String id, RFC7285Endpoint.CostRequest request) {
        return null;
    }

    public RFC7285Endpoint.CostResponse getEndpointCost(RFC7285VersionTag vtag, RFC7285Endpoint.CostRequest request) {
        return null;
    }

    public RFC7285Endpoint.PropertyResponse getEndpointProperty(String id, RFC7285Endpoint.PropertyRequest request) {
        return null;
    }

    public RFC7285Endpoint.PropertyResponse getEndpointProperty(RFC7285VersionTag vtag, RFC7285Endpoint.PropertyRequest request) {
        return null;
    }

}
