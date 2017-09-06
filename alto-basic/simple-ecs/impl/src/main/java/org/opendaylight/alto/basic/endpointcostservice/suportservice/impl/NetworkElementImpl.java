/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.suportservice.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.opendaylight.alto.basic.endpointcostservice.helper.DataStoreHelper;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.exception.ReadDataFailedException;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.LinkService;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.NetworkElementService;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.NetworkFlowCapableNodeService;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.NetworkHostNodeService;
import org.opendaylight.alto.basic.endpointcostservice.util.InstanceIdentifierUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkElementImpl implements NetworkElementService, AutoCloseable {
    private static final Logger log = LoggerFactory
            .getLogger(NetworkElementImpl.class);
    private static final int CPUS = Runtime.getRuntime().availableProcessors();
    private final ExecutorService exec = Executors.newFixedThreadPool(CPUS);

    private final DataBroker dataBroker;
    private ListenerRegistration<?> hostNodeListRegistration;
    private ListenerRegistration<?> linkListRegistration;

    private final NetworkHostNodeService hostNodeService;
    private final LinkService linkService;
    private final NetworkFlowCapableNodeService flowCapableNodeService;


    public NetworkElementImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.linkService = new LinkServiceImpl();
        this.hostNodeService = new NetworkHostNodeImpl();
        this.flowCapableNodeService = new NetworkFlowCapableNodeImpl(dataBroker);
        registerAsDataChangeListener();
    }

    public void registerAsDataChangeListener() {
        addExistingNodes();
        registerHostNodeListener();
        registerLinkListener();
    }

    private void addExistingNodes() {
        try {
            Topology topology = DataStoreHelper.readOperational(this.dataBroker, InstanceIdentifierUtils.TOPOLOGY);
            addExistingHostNodes(topology);
            addExistingLinks(topology);
        } catch (ReadDataFailedException e) {
            log.error("Read topology failed", e);
        }
    }
    private void addExistingHostNodes(Topology topology) {
        log.info("Topology Null Check: " + (topology == null));
        List<Node> nodeList = topology.getNode();
        log.info("Node List Null Check: " + (nodeList == null));
        if (topology != null && nodeList != null) {
            for (int i = 0; i < nodeList.size(); ++i) {
                Node node = nodeList.get(i);
                HostNode hostNode = node.getAugmentation(HostNode.class);
                log.info("Processing node " + i + ", " + hostNode);
                hostNodeService.addHostNode(hostNode);
            }
        }
    }

    private void addExistingLinks(Topology topology) {
        log.info("Topology Null Check: " + (topology == null));
        List<Link> linkList = topology.getLink();
        log.info("Link List Null Check: " + (linkList == null));
        if (topology != null && linkList != null) {
            for (int i = 0; i < linkList.size(); i++) {
                Link link = linkList.get(i);
                log.info("Processing link " + i + ", " + link.getLinkId().getValue());
                linkService.addLink(link);
            }
        }
    }
    @Override
    public void close() {
        this.hostNodeListRegistration.close();
        this.linkListRegistration.close();
    }

    private void onHostNodeChanged(Collection<DataTreeModification<HostNode>> changes) {
        exec.submit(() -> {
            for (DataTreeModification<HostNode> change: changes) {
                final DataObjectModification<HostNode> rootNode = change.getRootNode();
                switch (rootNode.getModificationType()) {
                    case WRITE:
                    case SUBTREE_MODIFIED:
                        log.info("Mapping host nodes to switch");
                        hostNodeService.addHostNode(rootNode.getDataAfter());
                        break;
                    case DELETE:
                        hostNodeService.deleteHostNode(rootNode.getDataBefore());
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void onLinkChanged(Collection<DataTreeModification<Link>> changes) {
        exec.submit(() -> {
            for (DataTreeModification<Link> change: changes) {
                final DataObjectModification<Link> rootNode = change.getRootNode();
                switch (rootNode.getModificationType()) {
                    case WRITE:
                    case SUBTREE_MODIFIED:
                        log.info("Updating Links");
                        linkService.addLink(rootNode.getDataAfter());
                        break;
                    case DELETE:
                        linkService.deleteLink(rootNode.getDataBefore());
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public LinkService getLinkService() {
        return linkService;
    }

    @Override
    public NetworkHostNodeService getHostNodeService() {
        return hostNodeService;
    }

    @Override
    public NetworkFlowCapableNodeService getFlowCapableNodeService() {
        return flowCapableNodeService;
    }

    private void registerHostNodeListener() {
        this.hostNodeListRegistration = this.dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
            LogicalDatastoreType.OPERATIONAL, InstanceIdentifierUtils.HOSTNODE),
            changes -> onHostNodeChanged(changes));
    }

    private void registerLinkListener() {
        this.linkListRegistration = this.dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, InstanceIdentifierUtils.LINK), changes -> onLinkChanged(changes));
    }
}
