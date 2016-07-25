/*
 * Copyright © 2015 Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl.util;

import com.google.common.collect.ImmutableList;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FlowManager {
    private static final Logger LOG = LoggerFactory.getLogger(FlowManager.class);
    private DataBroker dataBroker;
    private short flowTableId;
    private int flowPriority;
    private int flowIdleTimeout;
    private int flowHardTimeout;


    protected HashMap<FlowInfoKey, InstanceIdentifier<Flow>> flowInfo = new HashMap<>();

    private AtomicLong flowIdInc = new AtomicLong(10L);
    private AtomicLong flowCookieInc = new AtomicLong(0x3a00000000000000L);
    private final short DEFAULT_TABLE_ID = 0;
    private final Integer DEFAULT_PRIORITY = 20;
    private final Integer DEFAULT_HARD_TIMEOUT = 0;
    private final Integer DEFAULT_IDLE_TIMEOUT = 0;
    private final Long OFP_NO_BUFFER = Long.valueOf(4294967295L);

    private final String DEFAULT_SUBNET_MASK = "/32";
    private final Long IP_ETHER_TYPE = 0x0800L;
    private final Long NO_METER_SPECIFIED = -1L;

    public FlowManager(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        setFlowTableId(DEFAULT_TABLE_ID);
        setFlowPriority(DEFAULT_PRIORITY);
        setFlowIdleTimeout(DEFAULT_IDLE_TIMEOUT);
        setFlowHardTimeout(DEFAULT_HARD_TIMEOUT);
       }

    private Match createMacMatch(MacAddress srcMac, MacAddress dstMac) {
        EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
        if (dstMac != null) {
            ethernetMatchBuilder.setEthernetDestination(new EthernetDestinationBuilder()
                    .setAddress(dstMac)
                    .build());
        }
        if (srcMac != null) {
            ethernetMatchBuilder.setEthernetSource(new EthernetSourceBuilder()
                    .setAddress(srcMac)
                    .build());
        }
        EthernetMatch ethernetMatch = ethernetMatchBuilder.build();
        if (ethernetMatch != null) {
            return new MatchBuilder()
                    .setEthernetMatch(ethernetMatch)
                    .build();
        } else {
            return null;
        }
    }

    protected Match createIpv4Match(Ipv4Address srcIP, Ipv4Address dstIP) {
        Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
        if (dstIP != null) {
            ipv4MatchBuilder.setIpv4Destination(new Ipv4Prefix(dstIP.getValue() + DEFAULT_SUBNET_MASK));
        }
        if(srcIP != null) {
            ipv4MatchBuilder.setIpv4Source(new Ipv4Prefix(srcIP.getValue() + DEFAULT_SUBNET_MASK));
        }
        Layer3Match layer3Match = ipv4MatchBuilder.build();
        if (layer3Match != null) {
            return new MatchBuilder()
                    .setLayer3Match(layer3Match)
                    .setEthernetMatch(new EthernetMatchBuilder()
                            .setEthernetType(new EthernetTypeBuilder()
                                    .setType(new EtherType(IP_ETHER_TYPE))
                                    .build())
                            .build())
                    .build();

        } else {
            return null;
        }
    }

    private Instruction createApplyActionsInstruction(Uri dstPortUri, int order) {
        Action outputToControllerAction = new ActionBuilder()
                .setOrder(0)
                .setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setMaxLength(0xffff)
                                .setOutputNodeConnector(dstPortUri)
                                .build())
                        .build())
                .build();
        ApplyActions applyActions = new ApplyActionsBuilder().setAction(ImmutableList.of(outputToControllerAction))
                .build();
        return new InstructionBuilder()
                .setOrder(order)
                .setInstruction(new ApplyActionsCaseBuilder()
                        .setApplyActions(applyActions)
                        .build())
                .build();
    }

    private Instruction createMeterInstruction(long meterId, int order) {
        MeterBuilder meterBuilder = new MeterBuilder()
                .setMeterId(new MeterId(meterId));
        return new InstructionBuilder()
                .setOrder(order)
                .setInstruction(new MeterCaseBuilder()
                        .setMeter(meterBuilder.build())
                        .build())
                .build();
    }

    private Instructions createInstructions(NodeConnectorRef dstPort, Long meterId) {
        List<Instruction> instructionList = new LinkedList<>();
        Uri dstPortUri = dstPort.getValue().firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId();
        if (meterId != null && meterId > 0) {
            instructionList.add(createMeterInstruction(meterId, 0));
            instructionList.add(createApplyActionsInstruction(dstPortUri, 1));
        } else {
            instructionList.add(createApplyActionsInstruction(dstPortUri, 0));
        }
        return new InstructionsBuilder()
                .setInstruction(instructionList)
                .build();
    }

    private Flow createFlow(FlowId flowId, String flowName, Short tableId, int priority, Match match, Instructions instructions) {
        return new FlowBuilder()
                .setId(flowId)
                .setFlowName(flowName)
                .setTableId(tableId)
                .setMatch(match)
                .setInstructions(instructions)
                .setPriority(priority)
                .setBufferId(OFP_NO_BUFFER)
                .setHardTimeout(DEFAULT_HARD_TIMEOUT)
                .setIdleTimeout(DEFAULT_IDLE_TIMEOUT)
                .setFlags(new FlowModFlags(false,false,false,false,false))
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())))
                .build();
    }

    private Flow createMacToMacFlow(FlowId flowId, Short tableId, int priority,
                                    MacAddress srcMac, MacAddress dstMac, Long meterId, NodeConnectorRef dstPort) {
        String flowName = srcMac.getValue() + "|" + dstMac.getValue();

        Match match = createMacMatch(srcMac, dstMac);

        Instructions instructions;
        if (meterId != null && meterId > 0) {
            instructions = createInstructions(dstPort, meterId);
        } else {
            instructions = createInstructions(dstPort, NO_METER_SPECIFIED);
        }

        return createFlow(flowId, flowName, tableId, priority, match, instructions);
    }

    private Flow createIpv4ToIpv4Flow(FlowId flowId, Short tableId, int priority,
                                      Ipv4Address srcIp, Ipv4Address dstIp, Long meterId, NodeConnectorRef dstPort) {
        String flowName = srcIp.getValue() + "|" + dstIp.getValue();

        Match match = createIpv4Match(srcIp, dstIp);

        Instructions instructions;
        if (meterId != null && meterId > 0) {
            instructions = createInstructions(dstPort, meterId);
        } else {
            instructions = createInstructions(dstPort, NO_METER_SPECIFIED);
        }

        return createFlow(flowId,flowName, tableId, priority, match, instructions);
    }


    public void setFlowTableId(short flowTableId) {
        this.flowTableId = flowTableId;
    }

    public void setFlowPriority(int flowPriority) {
        this.flowPriority = flowPriority;
    }

    public void setFlowIdleTimeout(int flowIdleTimeout) {
        this.flowIdleTimeout = flowIdleTimeout;
    }

    public void setFlowHardTimeout(int flowHardTimeout) {
        this.flowHardTimeout = flowHardTimeout;
    }

    public void removeFlow(MacAddress srcMac, MacAddress dstMac, NodeConnectorRef dstNodeConnectorRef) {
        LOG.info("Removing Flow from " + dstNodeConnectorRef.toString());
        TableKey flowTableKey = new TableKey(flowTableId);
        InstanceIdentifier<Flow> flowPath = buildFlowPath(createMacMatch(srcMac, dstMac), dstNodeConnectorRef, flowTableKey);
        LOG.info("Removed flow path: " + flowPath.toString());
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, flowPath);
        try {
            writeTransaction.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("Transaction failed: {}", e.toString());
        }
        LOG.info("Transaction succeeded");
        this.flowInfo.remove(new FlowInfoKey(InstanceIdentifierUtils.generateNodeInstanceIdentifier(dstNodeConnectorRef), createMacMatch(srcMac, dstMac)));
    }

    public void removeFlow(Ipv4Address srcIp, Ipv4Address dstIp, NodeConnectorRef dstNodeConnectorRef) {
        LOG.info("Removing Flow from " + dstNodeConnectorRef.toString());
        TableKey flowTableKey = new TableKey(flowTableId);
        InstanceIdentifier<Flow> flowPath = buildFlowPath(createIpv4Match(srcIp, dstIp), dstNodeConnectorRef, flowTableKey);
        LOG.info("Removed flow path: " + flowPath.toString());
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, flowPath);
        try {
            writeTransaction.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("Transaction failed: {}", e.toString());
        }
        LOG.info("Transaction succeeded");
        this.flowInfo.remove(new FlowInfoKey(InstanceIdentifierUtils.generateNodeInstanceIdentifier(dstNodeConnectorRef), createIpv4Match(srcIp, dstIp)));
    }

    public void writeFlow(MacAddress srcMac, MacAddress dstMac, NodeConnectorRef dstNodeConnectorRef, Long meterId) {

        LOG.info("in flowManager.addFlow");
        if(srcMac != null && dstMac.equals(srcMac)) {
            LOG.info("In addMacToMacFlow: No flows added. Source and Destination MAC are same.");
            return;
        }

        TableKey flowTableKey = new TableKey(flowTableId);

        InstanceIdentifier<Flow> flowPath = buildFlowPath(createMacMatch(srcMac, dstMac), dstNodeConnectorRef, flowTableKey);
        FlowId flowId = flowPath.firstKeyOf(Flow.class).getId();

        LOG.info("flowPath: " + flowPath.toString());
        LOG.info("flowId: " + flowId.toString());

        Flow flowBody = createMacToMacFlow(flowId, flowTableKey.getId(), DEFAULT_PRIORITY, srcMac, dstMac, meterId, dstNodeConnectorRef);

        LOG.info("flowBody: " + flowBody.toString());

        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, flowPath, flowBody, true);
        try {
            writeTransaction.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("Transaction failed: {}", e.toString());
        }
        LOG.info("Transaction succeeded");

    }

    public void writeFlow(Ipv4Address srcIp, Ipv4Address dstIp, NodeConnectorRef dstNodeConnectorRef, Long meterId) {

        LOG.info("in flowManager.addFlow");
        if(srcIp != null && dstIp.equals(srcIp)) {
            LOG.info("In addIpv4ToIpv4Flow: No flows added. Source and Destination mac are same.");
            return;
        }

        TableKey flowTableKey = new TableKey(flowTableId);

        InstanceIdentifier<Flow> flowPath = buildFlowPath(createIpv4Match(srcIp, dstIp), dstNodeConnectorRef, flowTableKey);
        FlowId flowId = flowPath.firstKeyOf(Flow.class).getId();

        LOG.info("flowPath: " + flowPath.toString());
        LOG.info("flowId: " + flowId.toString());

        Flow flowBody = createIpv4ToIpv4Flow(flowId, flowTableKey.getId(), DEFAULT_PRIORITY, srcIp, dstIp, meterId, dstNodeConnectorRef);

        LOG.info("flowBody: " + flowBody.toString());

        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, flowPath, flowBody, true);
        try {
            writeTransaction.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("Transaction failed: {}", e.toString());
        }
        LOG.info("Transaction succeed");

    }

    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow> getFlowInstanceIdentifier(NodeRef nodeRef, Flow flow) {
        return ((InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node>) nodeRef.getValue())
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(DEFAULT_TABLE_ID))
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow.class,
                        new FlowKey(new FlowId(flow.getId())));
    }

    private InstanceIdentifier<Flow> buildFlowPath(Match match, NodeConnectorRef nodeConnectorRef, TableKey flowTableKey) {

        InstanceIdentifier<Node> nodeIID = InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef);
        FlowInfoKey flowInfoKey = new FlowInfoKey(nodeIID, match);
        InstanceIdentifier<Flow> flowPath;
        if (!this.flowInfo.containsKey(flowInfoKey)) {
            FlowKey flowKey = new FlowKey(new FlowId(String.valueOf(this.flowIdInc.getAndIncrement())));
            flowPath = InstanceIdentifierUtils.generateFlowInstanceIdentifier(nodeConnectorRef, flowTableKey, flowKey);
            this.flowInfo.put(flowInfoKey, flowPath);
        } else {
            LOG.info("You should be here");
            flowPath = this.flowInfo.get(flowInfoKey);
        }

        return flowPath;
    }
}
