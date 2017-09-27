/*
 * Copyright © 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoAutoMapsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AltoAutoMapsProvider.class);

    private AltoAutoMapsUpdateListener listener;

    public AltoAutoMapsProvider(final DataBroker dataBroker) {
        this.listener = new AltoAutoMapsUpdateListener(dataBroker);
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("AltoAutoMapsProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() throws Exception {
        closeListener();
        LOG.info("AltoAutoMapsProvider Closed");
    }

    private void closeListener() throws Exception {
        if (listener != null) {
            this.listener.close();
        }
    }
}
