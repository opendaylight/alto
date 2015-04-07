package org.opendaylight.alto.commons.types.mapper;

import org.opendaylight.alto.commons.types.rfc7285.AltoNetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.CostMap;
import org.opendaylight.alto.commons.types.rfc7285.CostType;
import org.opendaylight.alto.commons.types.rfc7285.Endpoint;
import org.opendaylight.alto.commons.types.rfc7285.Extensible;
import org.opendaylight.alto.commons.types.rfc7285.IRD;
import org.opendaylight.alto.commons.types.rfc7285.VersionTag;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONMapper {

    private ObjectMapper mapper = new ObjectMapper()
                            .setSerializationInclusion(Include.NON_DEFAULT)
                            .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);

    public Endpoint.AddressGroup asAddressGroup(String json) throws Exception {
        return mapper.readValue(json, Endpoint.AddressGroup.class);
    }

    public Endpoint.PropertyRequest asPropertyRequest(String json) throws Exception {
        return mapper.readValue(json, Endpoint.PropertyRequest.class);
    }

    public Endpoint.PropertyRespond asPropertyRespond(String json) throws Exception {
        return mapper.readValue(json, Endpoint.PropertyRespond.class);
    }

    public Endpoint.CostRequest asCostRequest(String json) throws Exception {
        return mapper.readValue(json, Endpoint.CostRequest.class);
    }

    public Endpoint.CostRespond asCostRespond(String json) throws Exception {
        return mapper.readValue(json, Endpoint.CostRespond.class);
    }

    public CostMap asCostMap(String json) throws Exception {
        return mapper.readValue(json, CostMap.class);
    }

    public CostType asCostType(String json) throws Exception {
        return mapper.readValue(json, CostType.class);
    }

    public Endpoint asEndpoint(String json) throws Exception {
        return mapper.readValue(json, Endpoint.class);
    }

    public Extensible asExtensible(String json) throws Exception {
        return mapper.readValue(json, Extensible.class);
    }

    public IRD asIRD(String json) throws Exception {
        return mapper.readValue(json, IRD.class);
    }

    public AltoNetworkMap asNetworkMap(String json) throws Exception {
        return mapper.readValue(json, AltoNetworkMap.class);
    }

    public VersionTag asVersionTag(String json) throws Exception {
        return mapper.readValue(json, VersionTag.class);
    }

    public String asJSON(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }
}
