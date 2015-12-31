/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.suportservice.impl;

import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.NetworkPortStatisticsService;
import org.opendaylight.alto.basic.endpointcostservice.util.InstanceIdentifierUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.Bytes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;

public class NetworkPortStatisticsServiceImpl implements NetworkPortStatisticsService, DataChangeListener, AutoCloseable{
    private static final Logger logger = LoggerFactory
            .getLogger(NetworkPortStatisticsServiceImpl.class);
    private static final int CPUS = Runtime.getRuntime().availableProcessors();
    private ExecutorService exec = Executors.newFixedThreadPool(CPUS);
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

    private Map<String, nodeStatistic> nodeStatisticData = null;
    private ListenerRegistration<DataChangeListener> portListener = null;
    public NetworkPortStatisticsServiceImpl(DataBroker dataBroker) {
        this.logger.info("NetworkPortStatisticsServiceImpl initial.");
        this.dataBroker = dataBroker;
        nodeStatisticData = new HashMap<String, nodeStatistic>();
        registerPortListener();
    }

    private void registerPortListener() {
        this.portListener = this.dataBroker.registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL, InstanceIdentifierUtils.STATISTICS,
                this, AsyncDataBroker.DataChangeScope.SUBTREE
        );
    }
    @Override
    public void close() throws Exception {
        portListener.close();
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        exec.submit(new Runnable() {
            @Override
            public void run() {
                onDataUpdated(change);
                onDataDeleted(change);
            }

            private void onDataUpdated(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
                Map<InstanceIdentifier<?>, DataObject> updated = change.getUpdatedData();

                for (Map.Entry<InstanceIdentifier<?>, DataObject> eachEntry : updated.entrySet()) {
                    final DataObject dataObject = eachEntry.getValue();
                    if (dataObject instanceof FlowCapableNodeConnectorStatisticsData) {
                        final FlowCapableNodeConnectorStatisticsData statistic =
                                (FlowCapableNodeConnectorStatisticsData) dataObject;
                        Bytes bytes = statistic.getFlowCapableNodeConnectorStatistics().getBytes();
                        if (bytes != null) {
                            String id = eachEntry.getKey()
                                    .firstKeyOf(NodeConnector.class)
                                    .getId().getValue();
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
                }
            }

            private void onDataDeleted(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
                Set<InstanceIdentifier<?>> removed = change.getRemovedPaths();
                for (InstanceIdentifier<?> eachPath : removed) {
                    if (eachPath.getTargetType() == FlowCapableNodeConnectorStatisticsData.class) {
                        String name =
                                eachPath.firstKeyOf(NodeConnector.class).getId().getValue();
                        nodeStatisticData.remove(name);
                    }
                }
            }
        });
    }

    @Override
    public Long getCurrentTxSpeed(String tpId, Metric metric) {
        if (nodeStatisticData.containsKey(tpId)) {
            if (metric == Metric.BITSPERSECOND)
                return nodeStatisticData.get(tpId).txSpeed * 8;
            else if (metric == Metric.BYTESPERSECOND)
                return nodeStatisticData.get(tpId).txSpeed;
        }
        return null;
    }

    @Override
    public Long getCurrentRxSpeed(String tpId, Metric metric) {
        if (nodeStatisticData.containsKey(tpId)) {
            if (metric == Metric.BITSPERSECOND)
                return nodeStatisticData.get(tpId).rxSpeed * 8;
            else if (metric == Metric.BYTESPERSECOND)
                return nodeStatisticData.get(tpId).rxSpeed;
        }
        return null;
    }
}
