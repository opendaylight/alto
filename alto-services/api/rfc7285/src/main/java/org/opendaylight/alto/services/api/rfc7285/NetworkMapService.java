/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;

public interface NetworkMapService {

    RFC7285NetworkMap getDefaultNetworkMap();

    RFC7285NetworkMap getNetworkMap(String id);

    RFC7285NetworkMap getNetworkMap(RFC7285VersionTag vtag);

    RFC7285NetworkMap getNetworkMap(String id, RFC7285NetworkMap.Filter filter);

    RFC7285NetworkMap getNetworkMap(RFC7285VersionTag vtag, RFC7285NetworkMap.Filter filter);

    Boolean validateNetworkMapFilter(String id, RFC7285NetworkMap.Filter filter);

    Boolean validateNetworkMapFilter(RFC7285VersionTag vtag, RFC7285NetworkMap.Filter filter);

}
