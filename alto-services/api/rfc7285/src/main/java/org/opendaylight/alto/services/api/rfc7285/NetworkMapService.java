package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;

public interface NetworkMapService {

    public RFC7285NetworkMap getDefaultNetworkMap();

    public RFC7285NetworkMap getNetworkMap(String id);

    public RFC7285NetworkMap getNetworkMap(RFC7285VersionTag vtag);

    public RFC7285NetworkMap getNetworkMap(String id, RFC7285NetworkMap.Filter filter);

    public RFC7285NetworkMap getNetworkMap(RFC7285VersionTag vtag, RFC7285NetworkMap.Filter filter);

    public Boolean validateNetworkMapFilter(String id, RFC7285NetworkMap.Filter filter);

    public Boolean validateNetworkMapFilter(RFC7285VersionTag vtag, RFC7285NetworkMap.Filter filter);

}
