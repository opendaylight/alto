/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl.helper;

import com.google.common.base.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataStoreHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DataStoreHelper.class);

    private DataStoreHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @return The operational data of iid
     */
    public static <T extends DataObject> T readOperational(
            DataBroker dataBroker, InstanceIdentifier<T> iid) throws ReadDataFailedException {
        return readFromDataStore(dataBroker, iid, LogicalDatastoreType.OPERATIONAL);
    }

    /**
     * @return The configuration data of iid
     */
    public static <T extends DataObject> T readConfiguration(
            DataBroker dataBroker, InstanceIdentifier<T> iid) throws ReadDataFailedException {
        return readFromDataStore(dataBroker, iid, LogicalDatastoreType.CONFIGURATION);
    }

    public static <T extends DataObject> T readFromDataStore(
            DataBroker dataBroker, InstanceIdentifier<T> iid, LogicalDatastoreType type)
            throws ReadDataFailedException {
        if (dataBroker == null) {
            throw new ReadDataFailedException("No DataBroker in the context.");
        }

        ReadOnlyTransaction rx = dataBroker.newReadOnlyTransaction();

        try {
            Future<Optional<T>> future = rx.read(type, iid);
            if (future != null) {
                Optional<T> optional = future.get();
                if (optional.isPresent()) {
                    return optional.get();
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

        throw new ReadDataFailedException("Maybe data read is interrupted.");
    }

    public static <T extends DataObject> void writeOperational(
            DataBroker dataBroker, InstanceIdentifier<T> iid, T data)
            throws WriteDataFailedException {
        writeToDataStore(dataBroker, iid, LogicalDatastoreType.OPERATIONAL, data);
    }

    public static <T extends DataObject> void writeConfiguration(
            DataBroker dataBroker, InstanceIdentifier<T> iid, T data)
            throws WriteDataFailedException {
        writeToDataStore(dataBroker, iid, LogicalDatastoreType.CONFIGURATION, data);
    }

    public static <T extends DataObject> void writeToDataStore(
            DataBroker dataBroker, InstanceIdentifier<T> iid, LogicalDatastoreType type,
            T data) throws WriteDataFailedException {
        if (dataBroker == null) {
            throw new WriteDataFailedException("No DataBroker in the context.");
        }

        WriteTransaction wx = dataBroker.newWriteOnlyTransaction();

        try {
            wx.merge(type, iid, data, true);
            wx.submit();
            return;
        } catch (NullPointerException e) {
            LOG.error("Cannot start a new read transaction:", e);
        }
        throw new WriteDataFailedException("Fail to write data for some reason.");
    }
}
