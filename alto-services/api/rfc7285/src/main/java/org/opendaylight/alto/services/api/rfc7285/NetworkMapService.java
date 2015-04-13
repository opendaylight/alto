package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.VersionTag;

public interface NetworkMapService {

    public NetworkMap getDefaultNetworkMap();

    public NetworkMap getNetworkMap(String id);

    public NetworkMap getNetworkMap(VersionTag vtag);

    public NetworkMap getNetworkMap(String id, NetworkMap.Filter filter);

    public NetworkMap getNetworkMap(VersionTag vtag, NetworkMap.Filter filter);

    public Boolean validateNetworkMapFilter(String id, NetworkMap.Filter filter);

    public Boolean validateNetworkMapFilter(VersionTag vtag, NetworkMap.Filter filter);

}
