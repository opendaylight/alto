/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.ConfigCapacity;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.Speeds;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.config.capacity.Capacity;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.config.capacity.CapacityKey;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.speeds.Port;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.speeds.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.speeds.PortKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BwmonitorUtils {
    private static final Logger LOG = LoggerFactory.getLogger(BwmonitorUtils.class);

    private BwmonitorUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static InstanceIdentifier<Port> toInstanceIdentifier(String nodeId) {
        InstanceIdentifier<Port> iid = InstanceIdentifier.create(Speeds.class)
                .child(Port.class, new PortKey(nodeId));
        return iid;
    }

    public static boolean writeToSpeeds(String portId, Long rxSpeed, Long txSpeed,
            Long capacity, Long availBw, DataBroker db) {
        WriteTransaction transaction = db.newWriteOnlyTransaction();
        InstanceIdentifier<Port> iid = BwmonitorUtils.toInstanceIdentifier(portId);
        Port node = new PortBuilder().setPortId(portId)
                .setRxSpeed(BigInteger.valueOf(rxSpeed))
                .setTxSpeed(BigInteger.valueOf(txSpeed))
                .setCapacity(capacity)
                .setAvailBw(availBw)
                .build();
        transaction.put(LogicalDatastoreType.OPERATIONAL, iid, node);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        Futures.addCallback(future, new LoggingFuturesCallBack<>("Failed to write node to speeds", LOG));

        try {
            future.checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error(e.getMessage());
            return false;
        }
        return true;
    }

    private static InstanceIdentifier<Capacity> getConfigCapacityIid(String portId) {
        return InstanceIdentifier.builder(ConfigCapacity.class)
                .child(Capacity.class, new CapacityKey(portId))
                .build();
    }

    public static Long readConfiguredCapacity(String portId, DataBroker dataBroker) {
        if (dataBroker == null) {
            return -1L;
        }

        ReadOnlyTransaction rx = dataBroker.newReadOnlyTransaction();

        try {
            Future<Optional<Capacity>> future = rx.read(LogicalDatastoreType.CONFIGURATION,
                    getConfigCapacityIid(portId));
            if (future != null) {
                Optional<Capacity> optional = future.get();
                if (optional.isPresent()) {
                    return optional.get().getCapacity();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Data read is interrupted: ", e);
        } catch (NullPointerException e) {
            LOG.error("Cannot start a new read transaction: ", e);
        } finally {
            if (rx != null) {
                rx.close();
            }
        }
        return -1L;
    }
}

