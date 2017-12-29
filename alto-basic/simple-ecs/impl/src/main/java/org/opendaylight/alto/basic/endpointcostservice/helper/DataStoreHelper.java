/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.helper;

import com.google.common.base.Optional;
import org.opendaylight.alto.basic.endpointcostservice.supportservice.exception.ReadDataFailedException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DataStoreHelper {
    /**
     * @param dataBroker
     * @param iid
     * @param <T>
     * @return The configuration of iid
     * @throws ReadDataFailedException
     */
    public static <T extends DataObject> T readConfiguration(
            DataBroker dataBroker, InstanceIdentifier<T> iid) throws ReadDataFailedException {
        return readFromDataStore(dataBroker, iid, LogicalDatastoreType.CONFIGURATION);
    }

    /**
     * @param dataBroker
     * @param iid
     * @param <T>
     * @return The operational data of iid
     * @throws ReadDataFailedException
     */
    public static <T extends DataObject> T readOperational(
            DataBroker dataBroker, InstanceIdentifier<T> iid) throws ReadDataFailedException {
        T data = readFromDataStore(dataBroker, iid, LogicalDatastoreType.OPERATIONAL);
        return data;
    }

    /**
     * @param dataBroker
     * @param iid
     * @param type
     * @param <T>
     * @return The data of iid
     * @throws ReadDataFailedException
     */
    public static <T extends DataObject> T readFromDataStore(
            DataBroker dataBroker, InstanceIdentifier<T> iid, LogicalDatastoreType type) throws ReadDataFailedException {
        ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
        Future<Optional<T>> future = tx.read(type, iid);
        try {
            if (future != null) {
                Optional<T> optional = future.get();
                if (optional.isPresent()) {
                    return optional.get();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            tx.close();
        }
        throw new ReadDataFailedException();
    }
}
