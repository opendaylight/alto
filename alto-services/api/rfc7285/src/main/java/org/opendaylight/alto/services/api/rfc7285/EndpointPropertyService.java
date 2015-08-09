/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.services.api.rfc7285;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.PropertyRequest;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.PropertyResponse;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;

public interface EndpointPropertyService {

    public PropertyResponse getEndpointProperty(String id, PropertyRequest request);

    public PropertyResponse getEndpointProperty(RFC7285VersionTag vtag, PropertyRequest request);

}
