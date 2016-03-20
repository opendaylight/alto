/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.suportservice.service;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;

public interface NetworkFlowCapableNodeService {
    public void addFlowCapableNode(FlowCapableNode node);

    public void deleteFlowCapableNode(FlowCapableNode node);

    public FlowCapableNode getFlowCapableNode(String nodeId);

    public FlowCapableNodeConnector getFlowCapableNodeConnector(String tpId);

    /**
     * Get the statistics by tpid.
     * @param tpId
     * @return a {@link FlowCapableNodeConnectorStatistics}.
     */
    public FlowCapableNodeConnectorStatistics getFlowCapableNodeConnectorStatistics(String tpId);

    /**
     * Get the consumed bandwidth by tpid.
     * @param tpId
     * @return the consumed bandwidth in kbps.
     */
    public Long getConsumedBandwidth(String tpId);

    /**
     * Get the available bandwidth by tpid.
     * @param tpId
     * @return the available bandwidth in kbps.
     */
    public Long getAvailableBandwidth(String tpId);

    /**
     * Get the available bandwidth by tpid and meter id.
     * @param tpId
     * @param meterId
     * @return the available bandwidth in kbps.
     */
    public Long getAvailableBandwidth(String tpId, Long meterId);

    /**
     * Get the capacity by tpid.
     * @param tpId
     * @return the capacity bandwidth in kbps.
     */
    public Long getCapacity(String tpId);

    /**
     * Get the capacity by tpid and meter id.
     * @param tpId
     * @param meterId
     * @return the capacity bandwidth in kbps.
     */
    public Long getCapacity(String tpId, Long meterId);
}
