/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.AltoBwmonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BwmonitorProvider{

    private static final Logger LOG = LoggerFactory.getLogger(BwmonitorProvider.class);
    private BindingAwareBroker.RpcRegistration<AltoBwmonitorService> bwmonitorService;

    private final DataBroker dataBroker;
    private BwmonitorImpl bwmonitorImpl;

    public BwmonitorProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("BwmonitorProvider session initiated");
        bwmonitorImpl = new BwmonitorImpl(dataBroker);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("BwmonitorProvider closed");
    }
}