package org.opendaylight.alto.services.ext.fake;

import org.opendaylight.alto.services.api.rfc7285.AltoService;
import org.opendaylight.alto.commons.types.rfc7285.NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.CostMap;
import org.opendaylight.alto.commons.types.rfc7285.CostType;
import org.opendaylight.alto.commons.types.rfc7285.VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.IRD;
import org.opendaylight.alto.commons.types.rfc7285.JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.Endpoint;

public class FakeAltoService implements AltoService {

    private JSONMapper mapper = new JSONMapper();

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
    private NetworkMap networkMap = null;

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

    private CostMap costMap = null;

    public FakeAltoService() {
        try {
            networkMap = mapper.asNetworkMap(NETWORKMAP_JSON);
            costMap = mapper.asCostMap(COSTMAP_JSON);
        } catch (Exception e) {
            networkMap = null;
            costMap = null;
        }
    }

    public CostMap getCostMap(String id) {
        if (!DEFAULT_COSTMAP_ID.equals(id))
            return null;
        return costMap;
    }

    public CostMap getCostMap(VersionTag vtag) {
        return getCostMap(vtag.rid);
    }

    public CostMap getCostMap(String id, CostType type) {
        if (!DEFAULT_COSTMAP_ID.equals(id))
            return null;
        if (!(costMap.meta.costType.equals(type)))
            return null;
        return costMap;
    }

    public CostMap getCostMap(VersionTag vtag, CostType type) {
        return getCostMap(vtag.rid, type);
    }

    public CostMap getCostMap(String id, CostMap.Filter filter) {
        return null;
    }

    public CostMap getCostMap(VersionTag vtag, CostMap.Filter filter) {
        return null;
    }

    public Boolean supportCostType(String id, CostType type) {
        return null;
    }

    public Boolean supportCostType(VersionTag vtag, CostType type) {
        if (!DEFAULT_COSTMAP_ID.equals(vtag.rid))
            return new Boolean(false);
        if (!costMap.meta.costType.equals(type))
            return new Boolean(false);
        return new Boolean(true);
    }

    public Boolean validateCostMapFilter(String id, CostMap.Filter filter) {
        return false;
    }

    public Boolean validateCostMapFilter(VersionTag vtag, CostMap.Filter filter) {
        return false;
    }


    public NetworkMap getDefaultNetworkMap() {
        return networkMap;
    }

    public NetworkMap getNetworkMap(String id) {
        if (!DEFAULT_NETWORKMAP_ID.equals(id))
            return null;
        return networkMap;
    }

    public NetworkMap getNetworkMap(VersionTag vtag) {
        if (!DEFAULT_NETWORKMAP_ID.equals(vtag.rid))
            return null;
        if (!DEFAULT_NETWORKMAP_TAG.equals(vtag.tag))
            return null;
        return networkMap;
    }

    public NetworkMap getNetworkMap(String id, NetworkMap.Filter filter) {
        return null;
    }

    public NetworkMap getNetworkMap(VersionTag vtag, NetworkMap.Filter filter) {
        return null;
    }

    public Boolean validateNetworkMapFilter(String id, NetworkMap.Filter filter) {
        return new Boolean(false);
    }

    public Boolean validateNetworkMapFilter(VersionTag vtag, NetworkMap.Filter filter) {
        return new Boolean(false);
    }

    public IRD getDefaultIRD() {
        return null;
    }

    public IRD getIRD(String id) {
        return null;
    }

    public Endpoint.CostResponse getEndpointCost(String id, Endpoint.CostRequest request) {
        return null;
    }

    public Endpoint.CostResponse getEndpointCost(VersionTag vtag, Endpoint.CostRequest request) {
        return null;
    }

    public Endpoint.PropertyResponse getEndpointProperty(String id, Endpoint.PropertyRequest request) {
        return null;
    }

    public Endpoint.PropertyResponse getEndpointProperty(VersionTag vtag, Endpoint.PropertyRequest request) {
        return null;
    }

}
