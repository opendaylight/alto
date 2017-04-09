/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.multicost.impl;

import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;
import org.opendaylight.alto.core.northbound.api.utils.rfc7285.RFC7285CostType;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.multicost.rev170302.MulticostData;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class MulticostProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MulticostProvider.class);

    private MulticostRequestListener listener;

    private ListenerRegistration<?> reg;

    private final DataBroker dataBroker;

    private final AltoNorthboundRouter router;

    private MulticostService service;

    private static final DataTreeIdentifier<MulticostData> MULTICOST_DATAROOT;

    private static final String NUMERTICAL = "numerical";

    private static final String ROUTINGCOST = "routingcost";

    private static final String HOPCOUNT = "hopcount";

    private static final String BANDWIDTH = "bandwidth";

    static {
        InstanceIdentifier<MulticostData> iid = InstanceIdentifier.create(MulticostData.class);
        MULTICOST_DATAROOT = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, iid);
    }

    public MulticostProvider(final DataBroker dataBroker,
                             final AltoNorthboundRouter router) {
        this.dataBroker = dataBroker;
        this.router = router;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("MulticostProvider Session Initiated");

        RFC7285CostType routingcost = new RFC7285CostType(ROUTINGCOST, NUMERTICAL);
        RFC7285CostType hopcount = new RFC7285CostType(HOPCOUNT, NUMERTICAL);
        RFC7285CostType bandwidth = new RFC7285CostType(BANDWIDTH, NUMERTICAL);

        List<RFC7285CostType> types = Arrays.asList(routingcost, hopcount, bandwidth);

        service = new MulticostService(types, false);
        listener = new MulticostRequestListener(dataBroker, service);
        reg = dataBroker.registerDataTreeChangeListener(MULTICOST_DATAROOT, listener);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("MulticostProvider Closed");
        try {
            reg.close();
        } catch (Exception e) {
            // ignore
        }
    }
}
