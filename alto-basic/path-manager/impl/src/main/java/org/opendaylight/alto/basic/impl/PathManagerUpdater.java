/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.alto.basic.helper.PathManagerHelper;
import org.opendaylight.alto.basic.impl.helper.DataStoreHelper;
import org.opendaylight.alto.basic.impl.helper.ReadDataFailedException;
import org.opendaylight.alto.basic.impl.helper.WriteDataFailedException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.PathManager;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.PathManagerBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.path.manager.Path;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.path.manager.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.path.manager.path.FlowDesc;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.path.manager.path.Links;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.path.manager.path.LinksBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathManagerUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(PathManagerUpdater.class);
    private static final Short DEFAULT_TABLE_ID = 0;
    private static final InstanceIdentifier<PathManager> PATH_MANAGER_IID = InstanceIdentifier
            .create(PathManager.class);
    private static final InstanceIdentifier<Path> PATH_WILDCARD_IID = PATH_MANAGER_IID
            .child(Path.class);
    private static final InstanceIdentifier<Nodes> INV_NODE_IID = InstanceIdentifier
            .create(Nodes.class);

    private final DataBroker dataBroker;
    private final AtomicLong currentFlowId = new AtomicLong();
    private Map<String, LinkId> linkIdMap = new HashMap<>();

    public PathManagerUpdater(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    /**
     * Initialize path manager updater from FRM.
     */
    public void initiate() {
        LOG.debug("Initializing path manager updater from FRM.");
        Nodes nodes = null;
        try {
            nodes = DataStoreHelper.readOperational(dataBroker, INV_NODE_IID);
        } catch (ReadDataFailedException e) {
            LOG.error("Read inv:node failed: ", e);
        }
        if (nodes != null && nodes.getNode() != null) {
            for (Node node : nodes.getNode()) {
                String nodeId = node.getId().getValue();
                FlowCapableNode flowNode = node.getAugmentation(FlowCapableNode.class);
                if (flowNode != null && flowNode.getTable() != null) {
                    Table defaultTable = flowNode.getTable().get(DEFAULT_TABLE_ID);
                    if (defaultTable != null && defaultTable.getFlow() != null) {
                        for (Flow flow : defaultTable.getFlow()) {
                            newFlowRule(nodeId, flow);
                        }
                    }
                }
            }
        }
        LOG.debug("Initialized path manager updater from FRM.");
    }

    /**
     * Add a new mapping.
     *
     * @param egressPort the node connector id of the link source tp.
     * @param linkId the link id.
     */
    public void addLink(String egressPort, LinkId linkId) {
        linkIdMap.put(egressPort, linkId);
    }

    /**
     * Remove a mapping.
     *
     * @param egressPort the node connector id of the link source tp.
     */
    public void removeLink(String egressPort) {
        linkIdMap.remove(egressPort);
    }

    public void newFlowRule(String nodeId, Flow flow) {
        LOG.debug("Flow rule of node {} created:\n{}.", nodeId, flow);
        FlowDesc flowDesc = PathManagerHelper.toAltoFlowDesc(flow.getMatch());
        List<Uri> egressPorts = PathManagerHelper.toOutputNodeConnector(flow.getInstructions());
        if (egressPorts == null) {
            LOG.debug("No Egress Ports of this flow. Skip updating.");
            return;
        }
        List<LinkId> linkIds = new ArrayList<>();
        for (Uri port : egressPorts) {
            LinkId linkId = linkIdMap.getOrDefault(port.getValue(), null);
            if (linkId != null) {
                linkIds.add(linkId);
            }
        }
        if (linkIds.isEmpty()) {
            LOG.debug("No available link to the next hop of this flow. Skip updating.");
            return;
        }

        PathManager pathManager = null;
        try {
            pathManager = DataStoreHelper.readOperational(dataBroker, PATH_MANAGER_IID);
        } catch (ReadDataFailedException e) {
            LOG.error("Read pathmanager:path-manager failed: ", e);
        }
        if (pathManager == null) {
            pathManager = new PathManagerBuilder().setPath(new ArrayList<>()).build();
        }

        List<Path> paths = pathManager.getPath();
        insertFlowToPaths(flowDesc, linkIds, paths);
        try {
            DataStoreHelper.writeOperational(dataBroker, PATH_MANAGER_IID, pathManager);
        } catch (WriteDataFailedException e) {
            LOG.error("Fail to write path manager back.", e);
        }
    }

    private List<Path> insertFlowToPaths(FlowDesc flowDesc, List<LinkId> linkIds,
            List<Path> paths) {
        paths.sort(Comparator.comparing(Path::getPriority).reversed());
        int i = 0;
        for (Path path : paths) {
            LOG.debug("Compare flowDesc of path and inserted flow: {} and {}.", flowDesc,
                    path.getFlowDesc());
            FlowDescSplitter splitter = getUnionFlowDesc(path.getFlowDesc(), flowDesc);
            if (splitter.unionFlowDescs == null) {
                LOG.debug("FlowDesc does not match this path.");
            } else {
                List<Links> pathLinks = new ArrayList<>(path.getLinks());
                for (LinkId linkId : linkIds) {
                    pathLinks.add(new LinksBuilder().setLink(linkId).build());
                }
                paths.remove(i);
                paths.add(new PathBuilder()
                        .setId(path.getId())
                        .setFlowDesc(splitter.unionFlowDescs)
                        .setLinks(pathLinks)
                        .build());
                for (FlowDesc fd : splitter.nonunionFlowDescs) {
                    paths.add(new PathBuilder()
                            .setId(currentFlowId.getAndIncrement())
                            .setFlowDesc(fd)
                            .setLinks(path.getLinks())
                            .build());
                }
                for (FlowDesc fd : splitter.unhandledFlowDescs) {
                    paths = insertFlowToPaths(fd, linkIds, paths);
                }
                break;
            }
            i++;
        }
        return paths;
    }

    private FlowDescSplitter getUnionFlowDesc(FlowDesc ruleFlow, FlowDesc testFlow) {
        FlowDescSplitter splitter = new FlowDescSplitter();
        if (PathManagerHelper.isFlowMatch(ruleFlow, testFlow)) {
            splitter.unionFlowDescs = testFlow;
        } else {
            splitter.unhandledFlowDescs.add(testFlow);
        }
        // TODO: Handle the intersection between testFlow and ruleFlow.
        // Although it works when there is no wildcard flow rule.
        return splitter;
    }

    public void updateFlowRule(String nodeId, Flow before, Flow after) {
        LOG.debug("Flow rule of node {} updated:\nFrom: {};\nTo: {}.", nodeId, before, after);
        // TODO: Handle flow rule update
    }

    public void deleteFlowRule(String nodeId, Flow flow) {
        LOG.debug("Flow rule of node {} deleted:\n{}.", nodeId, flow);
        // TODO: Handle flow rule delete
    }

    private class FlowDescSplitter {

        FlowDesc unionFlowDescs;
        List<FlowDesc> nonunionFlowDescs = new ArrayList<>();
        List<FlowDesc> unhandledFlowDescs = new ArrayList<>();
    }
}
