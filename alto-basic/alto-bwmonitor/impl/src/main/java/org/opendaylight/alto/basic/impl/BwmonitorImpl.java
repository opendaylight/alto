/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.basic.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.AltoBwmonitorService;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorUnsubscribeInput;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorUnsubscribeOutput;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorUnsubscribeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorQueryInput;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorQueryOutput;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorQueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorSubscribeInput;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorSubscribeOutput;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorSubscribeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.Speeds;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.SpeedsBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.bwmonitor.query.output.PortSpeed;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.bwmonitor.query.output.PortSpeedBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.speeds.Port;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.speeds.PortKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BwmonitorImpl implements AltoBwmonitorService {
    private DataBroker db;
    private BwFetchingService bwFetchingService;
    private static final Logger LOG = LoggerFactory.getLogger(BwmonitorImpl.class);
    private boolean dataTreeInitialized = false;

    public BwmonitorImpl(DataBroker dataBroker) {
        db = dataBroker;
        bwFetchingService = new BwFetchingService(dataBroker);
    }

    private boolean initializeDataTree() {
        WriteTransaction transaction = db.newWriteOnlyTransaction();
        InstanceIdentifier<Speeds> iid = InstanceIdentifier.create(Speeds.class);
        Speeds speeds = new SpeedsBuilder().build();
        transaction.put(LogicalDatastoreType.OPERATIONAL, iid, speeds);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        Futures.addCallback(future, new LoggingFuturesCallBack<>("Failed to create speeds", LOG));

        try {
            future.checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error(e.getMessage());
            return false;
        }
        dataTreeInitialized = true;
        return true;
    }

    @Override
    public Future<RpcResult<BwmonitorSubscribeOutput>> bwmonitorSubscribe(BwmonitorSubscribeInput input) {
        boolean success = true;
        if(!dataTreeInitialized) {
            success = initializeDataTree();
        }
        if (success) {
            List<String> ports = input.getPortId();
            if (ports != null) {
                for (String port : ports) {
                    success = BwmonitorUtils.writeToSpeeds(port, 0L, 0L, 0L, 0L, db);
                    if (success) {
                        /**
                         * Deliver databroker into utility function is inefficient
                         * TODO: try to deliver transaction instead
                         */
                        bwFetchingService.addListeningPort(port);
                        LOG.debug("Subscribe node: {}", port);
                    } else {
                        LOG.debug("Fail to register node: {}", port);
                        break;
                    }
                }
            }
        }
        return RpcResultBuilder.success(new BwmonitorSubscribeOutputBuilder()
                .setResult(success).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<BwmonitorQueryOutput>> bwmonitorQuery(BwmonitorQueryInput input) {
        ReadTransaction transaction = db.newReadOnlyTransaction();
        InstanceIdentifier<Speeds> iid = InstanceIdentifier.create(Speeds.class);
        BwmonitorQueryOutputBuilder builder = new BwmonitorQueryOutputBuilder();
        List<String> queriedPorts = input.getPortId();
        if (queriedPorts == null) {
            queriedPorts = new ArrayList<>();
        }
        try {
            Optional<Speeds> nodeData = transaction.read(LogicalDatastoreType.OPERATIONAL, iid).get();
            if(nodeData.isPresent()){
                List<PortSpeed> portSpeeds = new ArrayList<>();
                builder.setPortSpeed(portSpeeds);
                List<Port> allPorts = nodeData.get().getPort();
                if (allPorts != null) {
                    for (Port port : allPorts) {
                        if (queriedPorts.contains(port.getPortId())) {
                            portSpeeds.add(new PortSpeedBuilder()
                                .setPortId(port.getPortId())
                                .setRxSpeed(port.getRxSpeed())
                                .setTxSpeed(port.getTxSpeed())
                                .setCapacity(port.getCapacity())
                                .setAvailBw(port.getAvailBw())
                                .build());
                            LOG.debug("Query node: {}, RxSpeed: {}, TxSpeed: {}, Capacity: {}, "
                                    + "AvailBw: {}", port.getPortId(), port.getRxSpeed(),
                                    port.getTxSpeed(), port.getCapacity(), port.getAvailBw());
                        }
                    }
                }
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
        } catch (InterruptedException|ExecutionException e){
            LOG.error(e.getMessage());
        }
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<BwmonitorUnsubscribeOutput>> bwmonitorUnsubscribe(BwmonitorUnsubscribeInput input) {
        List<String> ports = input.getPortId();
        BwmonitorUnsubscribeOutputBuilder builder = new BwmonitorUnsubscribeOutputBuilder()
                .setResult(true);
        if (ports != null) {
            WriteTransaction transaction = db.newWriteOnlyTransaction();
            for (String port : ports) {
                bwFetchingService.removeListeningPort(port);
                InstanceIdentifier<Port> iid = InstanceIdentifier.create(Speeds.class)
                        .child(Port.class, new PortKey(port));
                transaction.delete(LogicalDatastoreType.OPERATIONAL, iid);
            }
            transaction.submit();
        }
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public void close() {
        if (bwFetchingService != null) {
            bwFetchingService.close();
        }
        LOG.info("BwmonitorImpl closed");
    }
}
