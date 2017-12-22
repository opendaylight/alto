/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.Bytes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BwFetchingService implements DataTreeChangeListener<NodeConnector> {
    /**
     * Global settings
     */
    private DataBroker dataBroker;
    private Logger LOG = LoggerFactory.getLogger(BwFetchingService.class);
    private ListenerRegistration<?> portListner = null;
    private final ExecutorService exec = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());

    /**
     * Parameter to calculate the speed (magic number?)
     */
    private final static Integer TIME_SPAN = 1000;
    private final static Long MILLISECOND_PER_SECOND = 1000L;
    private final static Integer BYTES_PER_KILOBITS = 128;
    private final static Long DEFAULT_ASSUME_CAPACITY = 100000000L; // Magic?

    /**
     * Contructor to get essential variables
     * @param dataBroker Given by Provider, to help r/w data store
     */
    public BwFetchingService(DataBroker dataBroker){
        this.dataBroker = dataBroker;
        registerPortListener();
        LOG.info("BwFetchingService initialized");
    }

    class Statistic {
        /**
         * Timestamp -> Bytes
         */
        Map<Long, Long> rxHistory;
        Map<Long, Long> txHistory;

        Long rxSpeed;
        Long txSpeed;
        Long capacity;
        Long availBw;

        public Statistic() {
            rxHistory = new HashMap<>();
            txHistory = new HashMap<>();
            rxSpeed = 0L;
            txSpeed = 0L;
            capacity = 0xffffffffL;
            availBw = 0xffffffffL;
        }
    }

    Map<String, Statistic> statisticData = new HashMap<>();

    private void syncToDataBroker(String name, Statistic statistic) {
        BwmonitorUtils.writeToSpeeds(name, statistic.rxSpeed, statistic.txSpeed,
                statistic.capacity, statistic.availBw, dataBroker);
        LOG.debug("Bwmonitor speeds updated: rxSpeed={}, txSpeed={}, capacity={}, availBw={}",
                statistic.rxSpeed, statistic.txSpeed, statistic.capacity, statistic.availBw);
    }

    private void registerPortListener() {
        InstanceIdentifier<NodeConnector> iid = InstanceIdentifier
                .builder(Nodes.class)
                .child(Node.class)
                .child(NodeConnector.class).build();
        this.portListner = this.dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, iid), this);
        LOG.info("BwFetchingService register successfully");
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<NodeConnector>> changes) {
        exec.submit(() -> {
            for (DataTreeModification<NodeConnector> change: changes) {
                DataObjectModification<NodeConnector> rootNode = change.getRootNode();
                InstanceIdentifier<NodeConnector> iid = change.getRootPath().getRootIdentifier();
                String id = iid.firstKeyOf(NodeConnector.class).getId().getValue();
                // Only handle the registered port
                if (statisticData.containsKey(id)) {
                    switch (rootNode.getModificationType()) {
                        case WRITE:
                        case SUBTREE_MODIFIED:
                            onFlowCapableNodeConnectorStatisticsDataUpdated(iid, rootNode.getDataAfter());
                            break;
                        case DELETE:
                            onFlowCapableNodeConnectorStatisticsDataDeleted(iid);
                    }
                }
            }
        });
    }

    private void onFlowCapableNodeConnectorStatisticsDataDeleted(InstanceIdentifier<NodeConnector> identifier) {
        String name = identifier.firstKeyOf(NodeConnector.class).getId().getValue();
        this.removeListeningPort(name);
    }

    private void onFlowCapableNodeConnectorStatisticsDataUpdated(
            InstanceIdentifier<NodeConnector> identifier,
            NodeConnector updatedPort) {
        FlowCapableNodeConnectorStatisticsData updatedStatistic = updatedPort
                .getAugmentation(FlowCapableNodeConnectorStatisticsData.class);
        Statistic statistic;
        String id = identifier.firstKeyOf(NodeConnector.class).getId().getValue();
        if (statisticData.containsKey(id)) {
            statistic = statisticData.get(id);
            LOG.debug("Get historical statistic of port {}: {}", id, statistic);
        } else {
            LOG.debug("Port is not subscribed for monitoring!");
            return;
        }
        if (updatedStatistic != null) {
            LOG.debug("Reading new statistic of port {}", id);
            Bytes bytes = updatedStatistic.getFlowCapableNodeConnectorStatistics().getBytes();
            LOG.debug("Done to read port statistic");
            if (bytes != null) {
                LOG.debug("Processing new statistic");
                Long timestamp = System.currentTimeMillis();
                statistic.rxHistory.put(timestamp, bytes.getReceived().longValue());
                statistic.txHistory.put(timestamp, bytes.getTransmitted().longValue());
                statistic.rxSpeed = computeStatisticFromHistory(statistic.rxHistory, timestamp);
                statistic.txSpeed = computeStatisticFromHistory(statistic.txHistory, timestamp);
                LOG.debug("Compute new rate of port {}: rx={}, tx={}", id, statistic.rxSpeed, statistic.txSpeed);
            }
        }
        LOG.debug("Done to process new statistic");
        statistic = updateAvailBw(updatedPort, statistic);
        if (statistic != null) {
            syncToDataBroker(id, statistic);
        }
    }

    private Statistic updateAvailBw(NodeConnector updatedPort, Statistic statistic) {
        String portId = updatedPort.getKey().getId().getValue();
        Long configuredCapacity = BwmonitorUtils.readConfiguredCapacity(portId, dataBroker);
        LOG.debug("Reading configured capacity of port {}", portId);
        if (configuredCapacity >= 0) {
            statistic.capacity = configuredCapacity;
            LOG.debug("Applied configured capacity");
        } else {
            FlowCapableNodeConnector updatedFlowPort = updatedPort.getAugmentation(FlowCapableNodeConnector.class);
            if (updatedFlowPort != null) {
                LOG.debug("Reading capacity of port {}", portId);
                statistic.capacity = updatedFlowPort.getCurrentSpeed();
                if (statistic.capacity.equals(0L)) {
                    statistic.capacity = DEFAULT_ASSUME_CAPACITY;
                }
                LOG.debug("Done to read capacity");
            }
        }
        statistic.availBw = statistic.capacity - statistic.txSpeed / BYTES_PER_KILOBITS;
        return statistic;
    }

    private void cleanStatisticHistory(Map<Long, Long> history, Long timestamp, boolean inTimeSpan) {
        if (inTimeSpan) {
            history.entrySet().removeIf(e -> e.getKey() < (timestamp - TIME_SPAN));
        } else {
            Long maxTime = Long.valueOf(0);
            for (Map.Entry<Long, Long> item : history.entrySet()) {
                if(item.getKey() != timestamp && item.getKey() > maxTime)
                    maxTime = item.getKey();
            }
            for (Iterator<Map.Entry<Long, Long>> it = history.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Long, Long> entry = it.next();
                if(entry.getKey() < maxTime) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Compute ewma of port statistic.
     * @param history the historical rate statistic of this port (bytes)
     * @param timestamp the duration time since start statistic (nanoseconds)
     * @return the ewma of port statistic
     */
    private Long computeStatisticFromHistory(Map<Long, Long> history, Long timestamp) {
        /**
         * current speed = (average speed in timeSpan) * 0.8 + (speed from last history record) * 0.2
         */
        boolean inTimeSpan = false;
        for (Map.Entry<Long, Long> item : history.entrySet()) {
            if (!item.getKey().equals(timestamp) && item.getKey() > timestamp - TIME_SPAN) {
                inTimeSpan = true;
                break;
            }
        }
        cleanStatisticHistory(history, timestamp, inTimeSpan);
        Long minTime = Long.MAX_VALUE;
        Long maxTime = 0L;
        for (Map.Entry<Long, Long> item : history.entrySet()) {
            if (item.getKey() != timestamp && item.getKey() < minTime)
                minTime = item.getKey();
            if (item.getKey() != timestamp && item.getKey() > maxTime)
                maxTime = item.getKey();
        }
        Long speedFromLastRecord = (history.get(timestamp) - history.get(maxTime)) * MILLISECOND_PER_SECOND / (timestamp - maxTime);
        if (inTimeSpan) {
            Long speedFromTimeSpan = (history.get(timestamp) - history.get(timestamp)) * MILLISECOND_PER_SECOND / (timestamp - minTime);
            return (long)(speedFromTimeSpan * 0.8 + speedFromLastRecord * 0.2);
        } else {
            return speedFromLastRecord;
        }
    }

    public void addListeningPort(String portId) {
        if (!this.statisticData.containsKey(portId)) {
            statisticData.put(portId, new Statistic());
            LOG.debug("Add listening port: {}", portId);
        } else {
            LOG.debug("Try to add existent listening port: {}", portId);
        }
    }

    public void removeListeningPort(String portId) {
        if (this.statisticData.containsKey(portId)) {
            statisticData.remove(portId);
            LOG.debug("Remove listening port: {}", portId);
        } else {
            LOG.debug("Try to remove nonexistent listening port: {}", portId);
        }
    }

    public void close() {
        if (portListner != null) {
            LOG.info("Closing portListener...");
            portListner.close();
        }
        if (!exec.isShutdown()) {
            LOG.info("Stopping executor service...");
            exec.shutdownNow();
        }
        LOG.info("BwFetchingService closed");
    }
}
