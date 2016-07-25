/*
 * Copyright © 2015 Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl.util;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.endpoints.group.Endpoints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MeterManager {
    private static final Logger LOG = LoggerFactory.getLogger(MeterManager.class);
    private SalMeterService salMeterService;
    private DataBroker dataBroker;
    private final long MIN_METER_ID_PICA8 = 1;
    private final long MAX_METER_ID_PICA8 = 256;
    private final String DEFAULT_METER_NAME = "alto-spce rate limiting";
    private final String DEFAULT_METER_CONTAINER = "alto-spce rate limiting container";

    private HashMap<NodeRef, List<Boolean>> switchToMeterIdListMap = new HashMap<>();

    private HashMap<NodeRef, HashMap<EndpointPairAndRequirement, Long>> switchToPerFlowToMeterIdMap = new HashMap<>();

    public MeterManager(SalMeterService salMeterService, DataBroker dataBroker) {
        this.salMeterService = salMeterService;
        this.dataBroker = dataBroker;
    }

    public void removeMeterFromSwitch (Endpoints endpoint, NodeConnectorRef nodeConnectorRef, long dropRate, long burstSize) {
        EndpointPairAndRequirement epr = new EndpointPairAndRequirement(endpoint.getSrc().getValue(), endpoint.getDst().getValue(), dropRate, burstSize);
        NodeRef nodeRef = new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef));
        int meterId = switchToPerFlowToMeterIdMap.get(nodeRef).get(epr).intValue();
/*        this.salMeterService.removeMeter(new RemoveMeterInputBuilder()
                .setMeterId(new MeterId((long)meterId))
                .setNode(nodeRef)
                .build());*/

        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, buildMeterPath(meterId, nodeConnectorRef));
        try {
            writeTransaction.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("Transaction failed: {}", e.toString());
        }
        LOG.info("Transaction succeeded");

        List<Boolean> perSwitchMeterList = switchToMeterIdListMap.get(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)));
        perSwitchMeterList.set(meterId, false);
        switchToMeterIdListMap.put(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)), perSwitchMeterList);
        HashMap<EndpointPairAndRequirement, Long> deleteFlowMeterMap = switchToPerFlowToMeterIdMap.get(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)));
        deleteFlowMeterMap.remove(epr);
        switchToPerFlowToMeterIdMap.put(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)), deleteFlowMeterMap);
    }

    public long addDropMeter(String src, String dst, long dropRate, long dropBurstSize, NodeConnectorRef nodeConnectorRef) {
        LOG.info("In MeterManager.addDropMeter");
        List<Boolean> perSwitchMeterList;
        HashMap<EndpointPairAndRequirement, Long> perSwitchPerFlowToMeterIdMap;
        if (!switchToMeterIdListMap.containsKey(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)))) {
            perSwitchMeterList = new LinkedList<>();
            for (int i=0 ; i<=MAX_METER_ID_PICA8; ++i) {
                //false stands for meterId == i is free. We must use i from 1 not from 0.
                perSwitchMeterList.add(false);
            }
            switchToMeterIdListMap.put(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)), perSwitchMeterList);
        }
        if (!switchToPerFlowToMeterIdMap.containsKey(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)))) {
            perSwitchPerFlowToMeterIdMap = new HashMap<>();
            switchToPerFlowToMeterIdMap.put(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)), perSwitchPerFlowToMeterIdMap);
        }

        perSwitchMeterList = switchToMeterIdListMap.get(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)));
        perSwitchPerFlowToMeterIdMap = switchToPerFlowToMeterIdMap.get(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)));

        int firstFreeMeterId = 1;
        while (perSwitchMeterList.get(firstFreeMeterId)) {
            ++firstFreeMeterId;
        }

        Meter meter = createDropMeter(dropRate, dropBurstSize, firstFreeMeterId);
        writeMeterToConfigData(buildMeterPath(firstFreeMeterId, nodeConnectorRef),meter);
        perSwitchMeterList.set(firstFreeMeterId, true);
        EndpointPairAndRequirement epr = new EndpointPairAndRequirement(src, dst, dropRate, dropBurstSize);
        perSwitchPerFlowToMeterIdMap.put(epr, (long)firstFreeMeterId);

        switchToMeterIdListMap.put(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)), perSwitchMeterList);
        switchToPerFlowToMeterIdMap.put(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)), perSwitchPerFlowToMeterIdMap);
        return firstFreeMeterId;
    }

    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter> buildMeterPath(long meterIdLong, NodeConnectorRef nodeConnectorRef) {
        MeterId meterId = new MeterId(meterIdLong);
        MeterKey meterKey = new MeterKey(meterId);
        return InstanceIdentifierUtils.generateMeterInstanceIdentifier(nodeConnectorRef, meterKey);
    }

    private Meter createDropMeter(long dropRate, long dropBurstSize, long meterId) {
        //LOG.info("nodeConnectorRef is" + nodeConnectorRef.toString());
        DropBuilder dropBuilder = new DropBuilder();
        dropBuilder
                .setDropBurstSize(dropBurstSize)
                .setDropRate(dropRate);

        MeterBandHeaderBuilder mbhBuilder = new MeterBandHeaderBuilder()
                .setBandType(dropBuilder.build())
                .setBandId(new BandId(0L))
                .setMeterBandTypes(new MeterBandTypesBuilder()
                        .setFlags(new MeterBandType(true, false, false)).build())
                .setBandRate(dropRate)
                .setBandBurstSize(dropBurstSize);

        LOG.info("In createDropMeter, MeterBandHeaderBuilder is" + mbhBuilder.toString());

        List<MeterBandHeader> mbhList = new LinkedList<>();
        mbhList.add(mbhBuilder.build());

        MeterBandHeadersBuilder mbhsBuilder = new MeterBandHeadersBuilder()
                .setMeterBandHeader(mbhList);

        LOG.info("In createDropMeter, MeterBandHeader is " + mbhBuilder.build().toString());
        MeterBuilder meterBuilder = new MeterBuilder()
                .setFlags(new MeterFlags(true, true, false, false))
                .setMeterBandHeaders(mbhsBuilder.build())
                .setMeterId(new MeterId(meterId))
                .setMeterName(DEFAULT_METER_NAME)
                .setContainerName(DEFAULT_METER_CONTAINER);
        return meterBuilder.build();
    }

/*    private Future<RpcResult<AddMeterOutput>> writeMeterToConfigData(InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter> meterPath,
                                                                     Meter meter) {
        LOG.info("In writeMeterToConfigData");
        final InstanceIdentifier<Node> nodeInstanceId = meterPath.<Node>firstIdentifierOf(Node.class);
        final AddMeterInputBuilder builder = new AddMeterInputBuilder(meter);
        builder.setNode(new NodeRef(nodeInstanceId));
        builder.setMeterRef(new MeterRef(meterPath));
        builder.setTransactionUri(new Uri(meter.getMeterId().getValue().toString()));
        LOG.info("AddMeterInput: " + builder.build());
        return salMeterService.addMeter(builder.build());
    }*/

    private void writeMeterToConfigData(InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter> meterPath,
                                                                     Meter meter) {

        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder mb = new  org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder(meter);

        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, meterPath, mb.build(), true);
        try {
            writeTransaction.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("Transaction failed: {}", e.toString());
        }
        LOG.info("Transaction succeed");
    }
}
