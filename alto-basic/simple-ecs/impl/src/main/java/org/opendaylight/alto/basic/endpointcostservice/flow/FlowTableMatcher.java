/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.flow;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowTableMatcher {
    private static final Logger log = LoggerFactory
            .getLogger(FlowTableMatcher.class);

    private Map<Short, Table> indexTables = new HashMap<Short, Table>();
    private Map<Long, Group> indexedGroups = new HashMap<Long, Group>();

    private List<Action> applyActions = new ArrayList<Action>();
    private List<Action> pipelineActions = new ArrayList<Action>();
    private Long meterId;

    private FlowEntryMatcher flowEntryMatcher = new FlowEntryMatcher();

    public class FlowTableLookUpResult {
        public List<Uri> outputNodeConnectors = new ArrayList<Uri>();
        public boolean sendToController = false;
        public long meterId = -1;
    }

    private void prepare(FlowCapableNode node) {
        applyActions.clear();
        pipelineActions.clear();
        //meterId = null;
        meterId = 123L;
        indexTables.clear();
        indexTables = indexByTableId(node.getTable());
        indexedGroups = indexByGroupId(node.getGroup());
    }

    /**
     * Look up flow table by given fields in a certain switch.
     * @param node define the node where to search.
     * @param matchFields define the given fields to find.
     * @return the result of look up.
     */
    public FlowTableLookUpResult lookUpFlowTables(FlowCapableNode node, MatchFields matchFields) {
        log.info("Looking up flow table for " + matchFields.inPort);
        prepare(node);
        short currentTableId = 0;
        short nextTableId = 0;
        do {
            currentTableId = nextTableId;
            nextTableId = lookUpFlowTable(currentTableId, matchFields);
        } while (nextTableId != currentTableId);
        return processActions(node);
    }

    private short lookUpFlowTable(short tableId, MatchFields matchFields) {
        List<Flow> flows = indexTables.get(tableId).getFlow();
        sortFlowByPriority(flows);
        for (Flow flow : flows) {
            if (flowEntryMatcher.match(flow.getMatch(), matchFields)) {
                log.info("Priority: " + flow.getPriority());
                Instructions inss = flow.getInstructions();
                if (inss == null) {
                    return tableId;
                }
                short nextTableId = lookUpInstructions(inss.getInstruction());
                return (nextTableId != -1) ? nextTableId : tableId;
            }
        }
        return tableId;
    }

    private FlowTableLookUpResult processActions(FlowCapableNode node) {
        FlowTableLookUpResult result = new FlowTableLookUpResult();
        processActionList(applyActions, result);
        processActionSet(pipelineActions, result);
        processMeterResult(result);
        log.info("FlowTableLookUpResult: " + result.outputNodeConnectors.size());
        return result;
    }

    private short lookUpInstructions(List<Instruction> inns) {
        short nextTableId = -1;
        Map<Integer, Instruction> indexedInss = sortAndIndexInstructionsByOrder(inns);
        for (int i = 0; i < indexedInss.size(); i++) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction ins = indexedInss.get(i).getInstruction();
            if (ins instanceof MeterCase) {
                meterCase((MeterCase) ins);
            }else if (ins instanceof ApplyActionsCase) {
                applyActionsCase((ApplyActionsCase) ins);
            } else if (ins instanceof ClearActionsCase) {
                clearActionsCase((ClearActionsCase) ins);
            } else if (ins instanceof WriteActionsCase) {
                writeActionsCase((WriteActionsCase) ins);
            } else if (ins instanceof WriteMetadataCase) {
                writeMetadataCase((WriteMetadataCase) ins);
            } else if (ins instanceof GoToTableCase) {
                nextTableId = ((GoToTableCase) ins).getGoToTable().getTableId();
            }
        }
        return nextTableId;
    }

    private void applyActionsCase(ApplyActionsCase applyActionCase) {
        List<Action> applyActions = applyActionCase.getApplyActions().getAction();
        sortActionsByOrder(applyActions);
        this.applyActions.addAll(applyActions);
    }

    private void clearActionsCase(ClearActionsCase clearActions) {
        this.pipelineActions.clear();
    }

    private void writeActionsCase(WriteActionsCase writeActionCase) {
        List<Action> writeActions = writeActionCase.getWriteActions().getAction();
        sortActionsByOrder(writeActions);
        this.pipelineActions.addAll(writeActions);
    }

    private void writeMetadataCase(WriteMetadataCase writeMetadata) {}

    private void meterCase(MeterCase meterCase) {
        Meter meter = meterCase.getMeter();
        this.meterId = meter.getMeterId().getValue();
    }

    private void processActionSet(List<Action> actions, FlowTableLookUpResult result) {
        List<Action> actionsToProcess = selectValidActions(actions);
        processActionList(actionsToProcess, result);
    }

    private List<Action> selectValidActions(List<Action> actions) {
        boolean groupActionCase = false;
        List<Action> actionsToProcess = new ArrayList<Action>();
        for (Action action : actions) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionValue = action.getAction();
            if (actionValue instanceof GroupActionCase) {
                actionsToProcess.clear();
                actionsToProcess.add(action);
                groupActionCase = true;
            } else if (actionValue instanceof OutputActionCase) {
                if (!groupActionCase) {
                    actionsToProcess.add(action);
                }
            }
        }
        return actionsToProcess;
    }

    private void processActionList(List<Action> actionList, FlowTableLookUpResult result) {
        for (Action act : actionList) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action = act.getAction();
            if (action instanceof GroupActionCase) {
                processGroupAction((GroupActionCase) action, result);
            } else if (action instanceof OutputActionCase) {
                processOutputAction((OutputActionCase) action, result);
            }
        }
    }

    private void processOutputAction(OutputActionCase action, FlowTableLookUpResult result) {
        Uri nodeConnector = action.getOutputAction().getOutputNodeConnector();
        if (nodeConnector.getValue().matches("\\d+")) {
            result.outputNodeConnectors.add(nodeConnector);
        } else if (nodeConnector.getValue().matches("CONTROLLER")) {
            result.sendToController = true;
        }
    }

    private void processGroupAction(GroupActionCase actionCase, FlowTableLookUpResult result) {
        GroupAction groupAction = actionCase.getGroupAction();
        Group group = indexedGroups.get(groupAction.getGroupId());
        GroupTypes groupType = group.getGroupType();
        List<Bucket> buckets = group.getBuckets().getBucket();
        if (groupType.equals(GroupTypes.GroupAll)) {
            for (Bucket bucket : buckets) {
                processActionSet(bucket.getAction(), result);
            }
        } else if (groupType.equals(GroupTypes.GroupIndirect) && buckets.size() == 1) {
            processActionSet(buckets.get(0).getAction(), result);
        }
    }

    private void processMeterResult(FlowTableLookUpResult result) {
        result.meterId = this.meterId;
    }

    private Map<Short, Table> indexByTableId(List<Table> tables) {
        Map<Short, Table> indexedTables = new HashMap<Short, Table>();
        for (Table table : tables) {
            short id = table.getId();
            indexedTables.put(id, table);
        }
        return indexedTables;
    }

    private Map<Integer, Instruction> sortAndIndexInstructionsByOrder(List<Instruction> inns) {
        sortInstructionByOrder(inns);
        return indexInstructionsByOrder(inns);
    }

    private Map<Integer, Instruction> indexInstructionsByOrder(List<Instruction> inns) {
        Map<Integer, Instruction> indexedInss = new HashMap<Integer, Instruction>();
        for (Instruction ins : inns) {
            int id = ins.getOrder();
            indexedInss.put(id, ins);
        }
        return indexedInss;
    }

    private void sortInstructionByOrder(List<Instruction> inns) {
        Collections.sort(inns, new Comparator<Instruction>() {
            @Override
            public int compare(Instruction i1, Instruction i2) {
                if (i1.getOrder() < i2.getOrder()){
                    return -1;
                }else if (i1.getOrder() > i2.getOrder()){
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    private Map<Long, Group> indexByGroupId(List<Group> groups) {
        Map<Long, Group> groupMaps = new HashMap<Long, Group>();
        if (groups == null) {
            return groupMaps;
        }
        for (Group group: groups) {
            groupMaps.put(group.getGroupId().getValue(), group);
        }
        return groupMaps;
    }

    private void sortFlowByPriority(List<Flow> flows) {
        Collections.sort(flows, new Comparator<Flow>() {
            @Override
            public int compare(Flow f1, Flow f2) {
                if (f1.getPriority() < f2.getPriority()){
                    return 1;
                } else if (f1.getPriority() > f2.getPriority()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }

    private void sortActionsByOrder(List<Action> actions) {
        Collections.sort(actions, new Comparator<Action>() {
            @Override
            public int compare(Action a1, Action a2) {
                if (a1.getOrder() < a2.getOrder()){
                    return -1;
                }else if (a1.getOrder() > a2.getOrder()){
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }
}