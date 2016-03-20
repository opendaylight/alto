/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.suportservice.impl;

import org.opendaylight.alto.basic.endpointcostservice.flow.FlowTableMatcher;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.LinkService;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.NetworkElementService;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.NetworkFlowCapableNodeService;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.NetworkHostNodeService;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.RoutingService;
import org.opendaylight.alto.basic.endpointcostservice.util.LinkNode;
import org.opendaylight.alto.basic.endpointcostservice.flow.MatchFields;
import org.opendaylight.alto.basic.endpointcostservice.util.NameConverter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.TypedAddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.host.AttachmentPoints;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutingServiceImpl implements RoutingService {
    private static final Logger log = LoggerFactory
            .getLogger(RoutingServiceImpl.class);

    private LinkService linkService;
    private NetworkHostNodeService hostNodeService;
    private NetworkFlowCapableNodeService flowCapableNodeService;

    private FlowTableMatcher flowTableMatcher = new FlowTableMatcher();
    private List<LinkNode> pathStack = new ArrayList<LinkNode>();
    private Map<String, LinkNode> visitedInPort = new HashMap<String, LinkNode>();

    public RoutingServiceImpl(NetworkElementService networkService) {
        this.linkService = networkService.getLinkService();
        this.hostNodeService = networkService.getHostNodeService();
        this.flowCapableNodeService = networkService.getFlowCapableNodeService();
    }

    @Override
    public LinkNode buildRoutePath(MatchFields matchFields) {
        pathStack.clear();
        visitedInPort.clear();
        return buildRoutePathByLookingUpFRM(matchFields);
    }

    private LinkNode buildRoutePathByLookingUpFRM(MatchFields matchFields) {
        log.info("Building Routing Path by looking up FRM");
        LinkNode head = new LinkNode(getLinkByHostIp(matchFields.srcIp));
        pathStack.add(head);
        while (!pathStack.isEmpty()) {
            LinkNode linkNode = pathStack.remove(pathStack.size() - 1);
            visitNode(linkNode, matchFields);
        }
        logRoutePath(head,matchFields);
        return head;
    }

    private void visitNode(LinkNode node, MatchFields matchFields) {
        if (!node.endWithHost()) {
            matchFields.inPort = node.dstTpId();
            visitSwitchNode(node, matchFields);
            visitedInPort.put(node.dstTpId(), node);
        }
    }
    private void visitSwitchNode(LinkNode node, MatchFields matchFields) {
        FlowTableMatcher.FlowTableLookUpResult result = lookUpFlowTable(node, matchFields);
        if (needReComputePath(result)) {
            //TODO: complete recomputePath function
            recomputePath(node, matchFields);
        } else if (result != null) {
            pushChildren(node, result);
        }
    }

    private FlowTableMatcher.FlowTableLookUpResult lookUpFlowTable(LinkNode node, MatchFields matchFields) {
        FlowCapableNode flowNode = flowCapableNodeService.getFlowCapableNode(node.dstNodeId());
        if (flowNode != null) {
            return flowTableMatcher.lookUpFlowTables(flowNode, matchFields);
        }
        return null;
    }
    private boolean needReComputePath(FlowTableMatcher.FlowTableLookUpResult result) {
//        return result != null &&
//                result.sendToController && result.outputNodeConnectors.size() == 0;
        return false;
    }

    private LinkNode recomputePath(LinkNode node, MatchFields matchFields) {
        return null;
    }

    private void pushChildren(LinkNode node, FlowTableMatcher.FlowTableLookUpResult result) {
        for (int i = 0; i < result.outputNodeConnectors.size(); i++) {
            String srcTpId = NameConverter.buildNodeConnectorId(
                    node.dstNodeId(), result.outputNodeConnectors.get(i).getValue());
            Link link = linkService.getLinkBySourceTpId(srcTpId);
            if (link != null) {
                LinkNode childNode = createLinkNode(link, result.meterId);
                node.addChild(childNode);
                if (!visitedInPort.containsKey(childNode.dstTpId())) {
                    pathStack.add(childNode);
                }
            }
        }
    }

    private LinkNode createLinkNode(Link link) {
        return createLinkNode(link, null);
    }

    private LinkNode createLinkNode(Link link, Long meterId) {
        String nextInPort = link.getDestination().getDestTp().getValue();
        if (visitedInPort.containsKey(nextInPort)) {
            return visitedInPort.get(nextInPort);
        } else {
            LinkNode node = new LinkNode(link);
            String srcTpId = link.getSource().getSourceTp().getValue();
            Long bandwidth = this.flowCapableNodeService.getAvailableBandwidth(srcTpId, meterId);
            log.info("createLinkNode: " + link + ":" + bandwidth);
            node.setAvailableBandwidth(bandwidth);
            return node;
        }
    }

    private void logRoutePath(LinkNode head,MatchFields matchFields) {
        String path = "Paths: \n";
        ArrayList<LinkNode> pathStack = new ArrayList<LinkNode>();
        pathStack.add(head);
        while (!pathStack.isEmpty()) {
            LinkNode top = pathStack.remove(pathStack.size() - 1);
            if(top.children().size() == 0 && top.getLink().getDestination().getDestTp().equals(getLinkByHostIp(matchFields.dstIp).getSource().getSourceTp())){
                top.setAsDestHost();
            }
            pathStack.addAll(top.children());
            path += top.srcTpId() + ", " + top.dstTpId() + "\n";
        }
        log.info(path);
    }

    private Link getLinkByHostIp(TypedAddressData ipAddr) {
        AttachmentPoints points = getAttachmentPoint(ipAddr);
        if (points != null && points.getCorrespondingTp() != null) {
            Link link = linkService.getLinkBySourceTpId(
                    points.getCorrespondingTp().getValue());
            log.info("Getting Link: " + link.getSource() + "," + link.getDestination());
            return link;
        }
        return null;
    }
    private AttachmentPoints getAttachmentPoint(TypedAddressData ip) {
        return hostNodeService.getHostNodeByHostIP(ip).getAttachmentPoints().get(0);
    }
}
