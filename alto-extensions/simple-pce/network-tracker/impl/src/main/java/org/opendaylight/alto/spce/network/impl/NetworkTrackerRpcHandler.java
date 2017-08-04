/*
 * Copyright (c) 2015 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.spce.network.impl;

import java.math.BigInteger;
import java.util.concurrent.Future;
import org.opendaylight.alto.spce.network.api.AddressConvertService;
import org.opendaylight.alto.spce.network.api.NetworkPortStatisticsService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetIpByMacInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetIpByMacOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetIpByMacOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetMacByIpInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetMacByIpOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetMacByIpOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetRxSpeedInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetRxSpeedOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetRxSpeedOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetTxBandwidthInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetTxBandwidthOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetTxBandwidthOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetTxSpeedInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetTxSpeedOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.AltoSpceGetTxSpeedOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.NetworkTrackerService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkTrackerRpcHandler implements NetworkTrackerService {

    private static final Logger logger = LoggerFactory
            .getLogger(NetworkTrackerRpcHandler.class);
    private DataBroker dataBroker = null;
    private final NetworkPortStatisticsService networkPortStatisticsService;
    private final AddressConvertService addressConvertService;

    public NetworkTrackerRpcHandler(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.networkPortStatisticsService = new NetworkPortStatisticsServiceImpl(this.dataBroker);
        this.addressConvertService = new AddressConvertServiceImpl(this.dataBroker);
    }

    @Override
    public Future<RpcResult<AltoSpceGetRxSpeedOutput>> altoSpceGetRxSpeed(AltoSpceGetRxSpeedInput input) {
        String tpId = input.getTpId();
        AltoSpceGetRxSpeedOutput output = new AltoSpceGetRxSpeedOutputBuilder()
                .setSpeed(BigInteger.valueOf(this.networkPortStatisticsService.getCurrentRxSpeed(tpId, NetworkPortStatisticsService.Metric.BITSPERSECOND)))
                .build();
        return RpcResultBuilder.success(output).buildFuture();
    }

    @Override
    public Future<RpcResult<AltoSpceGetTxBandwidthOutput>> altoSpceGetTxBandwidth(AltoSpceGetTxBandwidthInput input) {
        String tpId = input.getTpId();
        AltoSpceGetTxBandwidthOutput output = new AltoSpceGetTxBandwidthOutputBuilder()
                .setSpeed(BigInteger.valueOf(this.networkPortStatisticsService.getAvailableTxBandwidth(tpId, null)))
                .build();
        return RpcResultBuilder.success(output).buildFuture();
    }

    @Override
    public Future<RpcResult<AltoSpceGetTxSpeedOutput>> altoSpceGetTxSpeed(AltoSpceGetTxSpeedInput input) {
        String tpId = input.getTpId();
        AltoSpceGetTxSpeedOutput output = new AltoSpceGetTxSpeedOutputBuilder()
                .setSpeed(BigInteger.valueOf(this.networkPortStatisticsService.getCurrentTxSpeed(tpId, NetworkPortStatisticsService.Metric.BITSPERSECOND)))
                .build();
        return RpcResultBuilder.success(output).buildFuture();
    }

    @Override
    public Future<RpcResult<AltoSpceGetIpByMacOutput>> altoSpceGetIpByMac(AltoSpceGetIpByMacInput input) {
        String mac = input.getMacAddress();
        AltoSpceGetIpByMacOutput output = new AltoSpceGetIpByMacOutputBuilder()
                .setIpAddress(this.addressConvertService.getIpByMac(mac)).build();
        return RpcResultBuilder.success(output).buildFuture();
    }

    @Override
    public Future<RpcResult<AltoSpceGetMacByIpOutput>> altoSpceGetMacByIp(AltoSpceGetMacByIpInput input) {
        String ip = input.getIpAddress();
        AltoSpceGetMacByIpOutput output = new AltoSpceGetMacByIpOutputBuilder()
                .setMacAddress(this.addressConvertService.getMacByIp(ip)).build();
        return RpcResultBuilder.success(output).buildFuture();
    }
}
