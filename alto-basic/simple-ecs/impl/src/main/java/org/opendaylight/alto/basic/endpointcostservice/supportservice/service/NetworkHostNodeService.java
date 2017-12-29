/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.supportservice.service;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.TypedAddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNode;

public interface NetworkHostNodeService {
    public void addHostNode(HostNode node);

    public void deleteHostNode(HostNode node);

    public HostNode getHostNodeByHostIP(TypedAddressData ip);

    public HostNode getHostNodeByHostId(String hostId);

    public boolean isValidHost(TypedAddressData ip);
}
