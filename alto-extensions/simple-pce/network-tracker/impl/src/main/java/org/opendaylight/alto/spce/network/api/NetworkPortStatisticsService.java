/*
 * Copyright (c) 2015 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.spce.network.api;

public interface NetworkPortStatisticsService {
    /**
     * @param tpId
     * @param metric
     * @return current Tx speed in bps or Bps.
     */
    Long getCurrentTxSpeed(String tpId, Metric metric);

    /**
     * @param tpId
     * @param metric
     * @return current Rx speed in bps of Bps.
     */
    Long getCurrentRxSpeed(String tpId, Metric metric);

    Long getAvailableTxBandwidth(String tpId, Long meterId);

    enum Metric {BITSPERSECOND, BYTESPERSECOND};
}
