/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.suportservice.service;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import java.util.List;

public interface LinkService {

    public void addLink(Link link);

    public void deleteLink(Link link);

    public Link getLinkByLinkId(String linkId);

    public List<Link> getLinksBySourceNodeId(String srcNodeId);

    public List<Link> getLinksByDestinationNodeId(String dstNodeId);

    public Link getLinkBySourceTpId(String srcTpId);

    public Link getLinkByDestinationTpId(String dstTpId);

    public Link getRevertedLink(Link link);
}
