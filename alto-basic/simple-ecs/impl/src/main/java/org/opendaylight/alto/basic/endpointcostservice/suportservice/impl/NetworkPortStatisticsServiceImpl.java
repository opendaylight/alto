/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.suportservice.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.NetworkPortStatisticsService;
import org.opendaylight.alto.basic.endpointcostservice.util.InstanceIdentifierUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.Bytes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkPortStatisticsServiceImpl implements NetworkPortStatisticsService,
        DataTreeChangeListener<FlowCapableNodeConnectorStatisticsData>, AutoCloseable {
    private static final Logger logger = LoggerFactory
            .getLogger(NetworkPortStatisticsServiceImpl.class);
    private static final int CPUS = Runtime.getRuntime().availableProcessors();
    private final ExecutorService exec = Executors.newFixedThreadPool(CPUS);
    private DataBroker dataBroker = null;

    private class nodeStatistic {
        public Long rxHistory = (long) 0;
        public Long txHistory = (long) 0;
        public Long rxSpeed = (long) 0;
        public Long txSpeed = (long) 0;
        public Long timestamp = (long) 0;

        @Override
        public String toString() {
            return "rxSpeed=" + rxSpeed.toString()
                    + ";txSpeed=" + txSpeed.toString()
                    + ";timestamp=" + timestamp.toString();
        }
    }

    private final Map<String, nodeStatistic> nodeStatisticData = new ConcurrentHashMap<>();
    private ListenerRegistration<?> portListener = null;

    public NetworkPortStatisticsServiceImpl(DataBroker dataBroker) {
        this.logger.info("NetworkPortStatisticsServiceImpl initial.");
        this.dataBroker = dataBroker;
        registerPortListener();
    }

    private void registerPortListener() {
        this.portListener = this.dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, InstanceIdentifierUtils.STATISTICS), this);
    }
    @Override
    public void close() throws Exception {
        portListener.close();
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<FlowCapableNodeConnectorStatisticsData>> changes) {
        exec.submit(() -> {
            for (DataTreeModification<FlowCapableNodeConnectorStatisticsData> change: changes) {
                final DataObjectModification<FlowCapableNodeConnectorStatisticsData> rootNode = change.getRootNode();
                final InstanceIdentifier<FlowCapableNodeConnectorStatisticsData> identifier =
                        change.getRootPath().getRootIdentifier();
                switch (rootNode.getModificationType()) {
                    case WRITE:
                    case SUBTREE_MODIFIED:
                        onFlowCapableNodeConnectorStatisticsDataUpdated(identifier, rootNode.getDataAfter());
                        break;
                    case DELETE:
                        onFlowCapableNodeConnectorStatisticsDataDeleted(identifier);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void onFlowCapableNodeConnectorStatisticsDataDeleted(
            InstanceIdentifier<FlowCapableNodeConnectorStatisticsData> identifier) {
        String name = identifier.firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId().getValue();
        nodeStatisticData.remove(name);
    }

    private void onFlowCapableNodeConnectorStatisticsDataUpdated(
            InstanceIdentifier<FlowCapableNodeConnectorStatisticsData> identifier,
            FlowCapableNodeConnectorStatisticsData statistic) {
        Bytes bytes = statistic.getFlowCapableNodeConnectorStatistics().getBytes();
        if (bytes != null) {
            String id = identifier.firstKeyOf(NodeConnector.class).getId().getValue();
            nodeStatistic ns = null;
            if (nodeStatisticData.containsKey(id)) {
                ns = nodeStatisticData.get(id);
            } else {
                ns = new nodeStatistic();
                nodeStatisticData.put(id, ns);
            }
            ns.rxSpeed = (bytes.getReceived().longValue() - ns.rxHistory) /
                    (statistic.getFlowCapableNodeConnectorStatistics()
                            .getDuration().getSecond().getValue() - ns.timestamp);
            ns.txSpeed = (bytes.getTransmitted().longValue() - ns.txHistory) /
                    (statistic.getFlowCapableNodeConnectorStatistics()
                            .getDuration().getSecond().getValue() - ns.timestamp);
            ns.rxHistory = bytes.getReceived().longValue();
            ns.txHistory = bytes.getTransmitted().longValue();
            ns.timestamp =
                    statistic.getFlowCapableNodeConnectorStatistics()
                            .getDuration().getSecond().getValue();
        }
    }

    @Override
    public Long getCurrentTxSpeed(String tpId, Metric metric) {
        if (nodeStatisticData.containsKey(tpId)) {
            if (metric == Metric.BITSPERSECOND) {
                return nodeStatisticData.get(tpId).txSpeed * 8;
            } else if (metric == Metric.BYTESPERSECOND) {
                return nodeStatisticData.get(tpId).txSpeed;
            }
        }
        return null;
    }

    @Override
    public Long getCurrentRxSpeed(String tpId, Metric metric) {
        if (nodeStatisticData.containsKey(tpId)) {
            if (metric == Metric.BITSPERSECOND) {
                return nodeStatisticData.get(tpId).rxSpeed * 8;
            } else if (metric == Metric.BYTESPERSECOND) {
                return nodeStatisticData.get(tpId).rxSpeed;
            }
        }
        return null;
    }
}
