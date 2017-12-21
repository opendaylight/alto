/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.alto.basic.helper.PathManagerHelper;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.PathManager;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.PathManagerBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.path.manager.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * PathListener Tester.
 *
 * @author Jensen Zhang
 * @version 1.0
 * @since <pre>Oct 10, 2017</pre>
 */
public class PathListenerTest {

    private static final Uri DEFAULT_NODE_ID = new Uri("openflow:default");
    private static final Short DEFAULT_TABLE_ID = 0;
    private static final Uri DEFAULT_FLOW_ID = new Uri("flow:default");
    private static final InstanceIdentifier<PathManager> PATH_MANAGER_IID = InstanceIdentifier
            .create(PathManager.class);
    private static final InstanceIdentifier<Nodes> INV_NODE_IID = InstanceIdentifier
            .create(Nodes.class);

    private PathListener pathListener1;
    private PathListener pathListener2;
    private PathManagerUpdater updater1;
    private PathManagerUpdater updater2;

    @Before
    public void before() throws Exception {
        DataBroker dataBroker1 = mock(DataBroker.class);
        ReadOnlyTransaction rx1 = mock(ReadOnlyTransaction.class);
        when(rx1.read(any(LogicalDatastoreType.class), any(InstanceIdentifier.class)))
                .thenReturn(null);
        when(dataBroker1.newReadOnlyTransaction()).thenReturn(rx1);
        updater1 = new PathManagerUpdater(dataBroker1);
        pathListener1 = new PathListener(updater1);

        DataBroker dataBroker2 = mock(DataBroker.class);
        ReadOnlyTransaction rx2 = mock(ReadOnlyTransaction.class);

        CheckedFuture<Optional<Nodes>, ReadFailedException> future2 = mock(CheckedFuture.class);
        Optional<Nodes> optional2 = mock(Optional.class);
        when(optional2.isPresent()).thenReturn(true);
        when(optional2.get()).thenReturn(new NodesBuilder()
                .setNode(Arrays.asList(
                        new NodeBuilder()
                                .setId(new NodeId(new Uri("openflow1")))
                                .addAugmentation(
                                        FlowCapableNode.class,
                                        new FlowCapableNodeBuilder()
                                                .setTable(Arrays.asList(
                                                        new TableBuilder()
                                                                .setId((short) 0)
                                                                .setFlow(Arrays.asList(
                                                                        getIpv4OutputFlow(
                                                                                new FlowId(
                                                                                        new Uri(getRandomId(
                                                                                                "flow"))),
                                                                                getRandomId(
                                                                                        "openflow")),
                                                                        getIpv4DropFlow(new FlowId(
                                                                                new Uri(getRandomId(
                                                                                        "flow"))))))
                                                                .build()))
                                                .build())
                                .build()))
                .build());
        when(future2.get()).thenReturn(optional2);

        CheckedFuture<Optional<PathManager>, ReadFailedException> future3 = mock(
                CheckedFuture.class);
        Optional<PathManager> optional3 = mock(Optional.class);
        when(optional3.isPresent()).thenReturn(true);
        when(optional3.get()).thenReturn(new PathManagerBuilder()
                .setPath(Arrays.asList(
                        new PathBuilder()
                                .setId(0L)
                                .setFlowDesc(PathManagerHelper.toAltoFlowDesc(
                                        new MatchBuilder().setLayer3Match(
                                                getIpv4Match(null, new Ipv4Prefix("10.0.0.1/16")))
                                                .build()))
                                .build()))
                .build());
        when(future3.get()).thenReturn(optional3);

        when(rx2.read(any(LogicalDatastoreType.class), eq(INV_NODE_IID)))
                .thenReturn(future2);
        when(rx2.read(any(LogicalDatastoreType.class), eq(PATH_MANAGER_IID)))
                .thenReturn(future3);
        when(dataBroker2.newReadOnlyTransaction()).thenReturn(rx2);
        updater2 = new PathManagerUpdater(dataBroker2);
        pathListener2 = new PathListener(updater2);
    }

    private DataTreeIdentifier<Flow> getMockRootPath(NodeKey nodeKey, TableKey tableKey,
            FlowKey flowKey) {
        InstanceIdentifier<Flow> iid = InstanceIdentifier
                .builder(Nodes.class).child(Node.class, nodeKey)
                .augmentation(FlowCapableNode.class).child(Table.class, tableKey)
                .child(Flow.class, flowKey).build();
        DataTreeIdentifier<Flow> path = new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL,
                iid);

        return path;
    }

    private DataObjectModification<Flow> getMockRootNode(ModificationType type, Flow beforeData,
            Flow afterData) {
        DataObjectModification<Flow> node = mock(DataObjectModification.class);
        when(node.getModificationType()).thenReturn(type);
        when(node.getDataBefore()).thenReturn(beforeData);
        when(node.getDataAfter()).thenReturn(afterData);

        return node;
    }

    private String getRandomId(String prefix) {
        return prefix + ":" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Layer3Match getIpv4Match(Ipv4Prefix srcPrefix, Ipv4Prefix dstPrefix) {
        return new Ipv4MatchBuilder()
                .setIpv4Source(srcPrefix)
                .setIpv4Destination(dstPrefix)
                .build();
    }

    private FlowBuilder getIpv4FlowBuilder(FlowId id) {
        return new FlowBuilder().setId(id)
                .setMatch(new MatchBuilder().setLayer3Match(
                        getIpv4Match(null, new Ipv4Prefix("10.0.0.1/24")))
                        .build());
    }

    private Flow getIpv4DropFlow(FlowId id) {
        return getIpv4FlowBuilder(id).build();
    }

    private Flow getIpv4OutputFlow(FlowId id, String output) {
        return getIpv4FlowBuilder(id)
                .setInstructions(new InstructionsBuilder()
                        .setInstruction(Arrays.asList(new InstructionBuilder()
                                .setInstruction(new ApplyActionsCaseBuilder()
                                        .setApplyActions(new ApplyActionsBuilder()
                                                .setAction(Arrays.asList(new ActionBuilder()
                                                        .setAction(new OutputActionCaseBuilder()
                                                                .setOutputAction(
                                                                        new OutputActionBuilder()
                                                                                .setOutputNodeConnector(
                                                                                        new Uri(output))
                                                                                .build())
                                                                .build())
                                                        .build()))
                                                .build())
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    /**
     * Method: onDataTreeChanged(@Nonnull Collection<DataTreeModification<Flow>> changes)
     */
    @Test
    public void testNullValueChanged() throws Exception {
        try {
            Collection<DataTreeModification<Flow>> nullChanges = null;
            pathListener1.onDataTreeChanged(nullChanges);
            pathListener2.onDataTreeChanged(nullChanges);
        } catch (Exception e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
        }

        Collection<DataTreeModification<Flow>> changes = new ArrayList<>();

        DataTreeModification<Flow> change0 = null;
        changes.add(change0);

        pathListener1.onDataTreeChanged(changes);
        pathListener2.onDataTreeChanged(changes);
    }

    /**
     * Method: onFlowRuleCreated(InstanceIdentifier<Flow> iid, Flow dataBefore, Flow dataAfter)
     */
    @Test
    public void testOnFlowRuleCreated() throws Exception {
        Collection<DataTreeModification<Flow>> changes = new ArrayList<>();

        DataTreeModification<Flow> change0 = mock(DataTreeModification.class);
        when(change0.getRootPath())
                .thenReturn(getMockRootPath(new NodeKey(new NodeId(DEFAULT_NODE_ID)),
                        new TableKey(DEFAULT_TABLE_ID), new FlowKey(new FlowId(DEFAULT_FLOW_ID))));
        DataObjectModification<Flow> rootNode = getMockRootNode(ModificationType.WRITE, null,
                getIpv4DropFlow(new FlowId(DEFAULT_FLOW_ID)));
        when(change0.getRootNode()).thenReturn(rootNode);
        changes.add(change0);

        DataTreeModification<Flow> change1 = mock(DataTreeModification.class);
        NodeId nodeId1 = new NodeId(new Uri(getRandomId("openflow")));
        FlowId flowId1 = new FlowId(new Uri(getRandomId("flow")));
        when(change1.getRootPath()).thenReturn(getMockRootPath(new NodeKey(nodeId1),
                new TableKey(DEFAULT_TABLE_ID), new FlowKey(flowId1)));
        String output1 = getRandomId("openflow");
        DataObjectModification<Flow> rootNode1 = getMockRootNode(ModificationType.WRITE,
                getIpv4OutputFlow(flowId1, output1), getIpv4DropFlow(flowId1));
        when(change1.getRootNode()).thenReturn(rootNode1);
        changes.add(change1);

        pathListener1.onDataTreeChanged(changes);
        updater2.addLink(output1, new LinkId(getRandomId("link")));
        pathListener2.onDataTreeChanged(changes);
        updater2.removeLink(output1);
    }

    /**
     * Method: onFlowRuleUpdated(InstanceIdentifier<Flow> iid, Flow dataBefore, Flow dataAfter)
     */
    @Test
    public void testOnFlowRuleUpdated() throws Exception {
        Collection<DataTreeModification<Flow>> changes = new ArrayList<>();

        DataTreeModification<Flow> change0 = mock(DataTreeModification.class);
        NodeId nodeId0 = new NodeId(new Uri(getRandomId("openflow")));
        FlowId flowId0 = new FlowId(new Uri(getRandomId("flow")));
        when(change0.getRootPath()).thenReturn(getMockRootPath(new NodeKey(nodeId0),
                new TableKey(DEFAULT_TABLE_ID), new FlowKey(flowId0)));
        DataObjectModification<Flow> rootNode0 = getMockRootNode(ModificationType.WRITE,
                getIpv4DropFlow(flowId0), getIpv4OutputFlow(flowId0, getRandomId("openflow")));
        when(change0.getRootNode()).thenReturn(rootNode0);
        changes.add(change0);

        DataTreeModification<Flow> change1 = mock(DataTreeModification.class);
        NodeId nodeId1 = new NodeId(new Uri(getRandomId("openflow")));
        FlowId flowId1 = new FlowId(new Uri(getRandomId("flow")));
        when(change1.getRootPath()).thenReturn(getMockRootPath(new NodeKey(nodeId1),
                new TableKey(DEFAULT_TABLE_ID), new FlowKey(flowId1)));
        DataObjectModification<Flow> rootNode1 = getMockRootNode(ModificationType.SUBTREE_MODIFIED,
                getIpv4OutputFlow(flowId1, getRandomId("openflow")), getIpv4DropFlow(flowId1));
        when(change1.getRootNode()).thenReturn(rootNode1);
        changes.add(change1);

        pathListener1.onDataTreeChanged(changes);
        pathListener2.onDataTreeChanged(changes);
    }

    /**
     * Method: onFlowRuleDeleted(InstanceIdentifier<Flow> iid, Flow dataBefore)
     */
    @Test
    public void testOnFlowRuleDeleted() throws Exception {
        Collection<DataTreeModification<Flow>> changes = new ArrayList<>();

        DataTreeModification<Flow> change0 = mock(DataTreeModification.class);
        NodeId nodeId0 = new NodeId(new Uri(getRandomId("openflow")));
        FlowId flowId0 = new FlowId(new Uri(getRandomId("flow")));
        when(change0.getRootPath()).thenReturn(getMockRootPath(new NodeKey(nodeId0),
                new TableKey(DEFAULT_TABLE_ID), new FlowKey(flowId0)));
        DataObjectModification<Flow> rootNode0 = getMockRootNode(ModificationType.DELETE,
                getIpv4OutputFlow(flowId0, getRandomId("openflow")), null);
        when(change0.getRootNode()).thenReturn(rootNode0);
        changes.add(change0);

        pathListener1.onDataTreeChanged(changes);
        pathListener2.onDataTreeChanged(changes);
    }

}
