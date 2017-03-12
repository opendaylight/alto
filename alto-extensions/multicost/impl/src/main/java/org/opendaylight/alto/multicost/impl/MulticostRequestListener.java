/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.multicost.impl;

import java.util.Collection;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.multicost.rev170302.MulticostData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.multicost.rev170302.MulticostDataBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MulticostRequestListener implements DataTreeChangeListener<MulticostData> {

    private static final Logger LOG = LoggerFactory.getLogger(MulticostRequestListener.class);

    private final DataBroker db;

    private MulticostService service;

    public MulticostRequestListener(final DataBroker db, final MulticostService service) {
        this.db = db;
        this.service = service;
    }

    private void handleRequest(WriteTransaction tx,
                               InstanceIdentifier<?> iid, DataObject value) {
        if (iid.getTargetType() != MulticostData.class) {
            return;
        }
        if (!(value instanceof MulticostData)) {
            return;
        }
        try {
            InstanceIdentifier<MulticostData> miid = iid.firstIdentifierOf(MulticostData.class);
            MulticostData data = (MulticostData) value;
            String request = data.getRequestBody();

            String response = service.accept(request);

            MulticostDataBuilder builder = new MulticostDataBuilder(data);
            builder.setResponseBody(response);

            tx.put(LogicalDatastoreType.OPERATIONAL, miid, builder.build());
        } catch (Exception e) {
            LOG.error("Failed to handle request: ", iid.firstKeyOf(MulticostData.class));
            return;
        }
    }

    void dispatch(WriteTransaction tx, DataTreeModification<MulticostData> mod) {
        InstanceIdentifier<MulticostData> iid = mod.getRootPath().getRootIdentifier();
        if (mod.getRootNode().getModificationType()
            .equals(DataObjectModification.ModificationType.DELETE)) {
            tx.delete(LogicalDatastoreType.OPERATIONAL, iid);
        } else {
            handleRequest(tx, iid, mod.getRootNode().getDataAfter());
        }
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<MulticostData>> e) {
        WriteTransaction tx = db.newWriteOnlyTransaction();
        try {
            e.forEach(mod -> dispatch(tx, mod));
            tx.submit();
        } catch (Exception exception) {
            tx.cancel();
        }
    }

}
