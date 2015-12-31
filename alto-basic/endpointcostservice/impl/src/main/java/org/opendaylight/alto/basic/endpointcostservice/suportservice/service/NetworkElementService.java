/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.suportservice.service;

public interface NetworkElementService {
    public LinkService getLinkService();

    public NetworkHostNodeService getHostNodeService();

    public NetworkFlowCapableNodeService getFlowCapableNodeService();
}
