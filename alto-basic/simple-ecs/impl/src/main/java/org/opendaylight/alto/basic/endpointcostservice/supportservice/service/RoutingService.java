/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.supportservice.service;

import org.opendaylight.alto.basic.endpointcostservice.util.LinkNode;
import org.opendaylight.alto.basic.endpointcostservice.flow.MatchFields;

public interface RoutingService {
    /**
     * Find route path in RFM by match fields.
     * @param matchFields
     * @return the actual routing path.
     */
    public LinkNode buildRoutePath(MatchFields matchFields);
}
