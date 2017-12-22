/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.BytesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatisticsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * BwFetchingService Tester.
 *
 * @author Jensen Zhang
 * @version 1.0
 * @since <pre>Oct 20, 2017</pre>
 */
public class BwFetchingServiceTest {

  private final DataBroker dataBroker = mock(DataBroker.class);
  private BwFetchingService service;

  private DataTreeIdentifier<NodeConnector> getMockRootPath(String portId) {
    InstanceIdentifier<NodeConnector> iid = InstanceIdentifier
        .builder(Nodes.class).child(Node.class)
        .child(NodeConnector.class,
            new NodeConnectorKey(new NodeConnectorId(portId)))
        .build();
    DataTreeIdentifier<NodeConnector> path = new DataTreeIdentifier<>(
        LogicalDatastoreType.OPERATIONAL, iid);

    return path;
  }

  private DataObjectModification<NodeConnector> getMockRootNode(ModificationType type,
      String portId) {
    DataObjectModification<NodeConnector> node = mock(DataObjectModification.class);
    NodeConnector afterData = new NodeConnectorBuilder()
        .setId(new NodeConnectorId(portId))
        .addAugmentation(FlowCapableNodeConnectorStatisticsData.class,
            new FlowCapableNodeConnectorStatisticsDataBuilder()
                .setFlowCapableNodeConnectorStatistics(
                    new FlowCapableNodeConnectorStatisticsBuilder()
                        .setDuration(new DurationBuilder()
                            .setSecond(new Counter32(1L))
                            .build())
                        .setBytes(new BytesBuilder()
                            .setReceived(BigInteger.ZERO)
                            .setTransmitted(BigInteger.ZERO)
                            .build())
                        .build())
                .build())
        .addAugmentation(FlowCapableNodeConnector.class,
            new FlowCapableNodeConnectorBuilder()
                .setCurrentSpeed(1000000L)
                .build())
        .build();
    when(node.getModificationType()).thenReturn(type);
    when(node.getDataAfter()).thenReturn(afterData);

    return node;
  }

  private DataTreeModification<NodeConnector> getMockDataTreeModification(ModificationType type,
      String portId) {
    DataTreeModification<NodeConnector> change = mock(DataTreeModification.class);
    when(change.getRootPath()).thenReturn(getMockRootPath(portId));
    DataObjectModification<NodeConnector> rootNode = getMockRootNode(type, portId);
    when(change.getRootNode()).thenReturn(rootNode);
    return change;
  }

  @Before
  public void before() throws Exception {
    service = new BwFetchingService(dataBroker);
  }

  /**
   * Method: onDataTreeChanged(@Nonnull Collection<DataTreeModification<NodeConnector>> changes)
   * When Node is updated in DataTree.
   */
  @Test
  public void testOnDataTreeNodeUpdated() throws Exception {
    Collection<DataTreeModification<NodeConnector>> changes = new ArrayList<>();

    DataTreeModification<NodeConnector> change0 = getMockDataTreeModification(
        ModificationType.WRITE, "testPort1");
    changes.add(change0);

    service.onDataTreeChanged(changes);
    service.addListeningPort("testPort1");
    service.onDataTreeChanged(changes);
  }

  /**
   * Method: onDataTreeChanged(@Nonnull Collection<DataTreeModification<NodeConnector>> changes)
   * When Node is deleted in DataTree.
   */
  @Test
  public void testOnDataTreeNodeDeleted() throws Exception {
    Collection<DataTreeModification<NodeConnector>> changes = new ArrayList<>();

    DataTreeModification<NodeConnector> change0 = getMockDataTreeModification(
        ModificationType.DELETE, "testPort2");
    changes.add(change0);

    service.onDataTreeChanged(changes);
    service.addListeningPort("testPort2");
    service.onDataTreeChanged(changes);
  }

  /**
   * Method: addListeningPort(String portId)
   */
  @Test
  public void testAddListeningPort() throws Exception {
    service.addListeningPort("testPort1");
    service.addListeningPort("testPort1");
  }

  /**
   * Method: removeListeningPort(String portId)
   */
  @Test
  public void testRemoveListeningPort() throws Exception {
    service.addListeningPort("testPort2");
    service.removeListeningPort("testPort2");
    service.removeListeningPort("testPort2");
  }

  /**
   * Method: close()
   */
  @Test
  public void testClose() throws Exception {
    service.close();
  }
}
