/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.suportservice.impl;

import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.LinkService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class LinkServiceImpl implements LinkService {
    private static final Logger log = LoggerFactory
            .getLogger(LinkServiceImpl.class);
    private Map<String, Set<String>> srcTpIdIndexedLinks = new ConcurrentHashMap<String, Set<String>>();
    private Map<String, Set<String>> dstTpIdIndexedLinks = new ConcurrentHashMap<String, Set<String>>();
    private Map<String, Set<String>> srcNodeIdIndexedLinks = new ConcurrentHashMap<String, Set<String>>();
    private Map<String, Set<String>> dstNodeIdIndexedLinks = new ConcurrentHashMap<String, Set<String>>();
    private Map<String, Link> linkIdIndexedLinks = new ConcurrentHashMap<String, Link>();
    @Override
    public void addLink(Link link) {
        synchronized (this) {
            this.linkIdIndexedLinks.put(link.getLinkId().getValue(), link);
            addLinkToTpIdIndexedLinks(srcTpIdIndexedLinks, link);
            addLinkToTpIdIndexedLinks(dstTpIdIndexedLinks, link);
            addLinkToNodeIdIndexedLinks(srcNodeIdIndexedLinks, link);
            addLinkToNodeIdIndexedLinks(dstNodeIdIndexedLinks, link);
            log.info("Adding link with source as " + link.getSource().getSourceTp().getValue()
                    + " and link id as " + link.getLinkId().getValue());
        }
    }

    @Override
    public void deleteLink(Link link) {
        synchronized (this) {
            this.linkIdIndexedLinks.remove(link.getLinkId().getValue());
            deleteLinkFromTpIdIndexedLinks(srcTpIdIndexedLinks, link);
            deleteLinkFromTpIdIndexedLinks(dstTpIdIndexedLinks, link);
            deleteLinkFromNodeIdIndexedLinks(srcNodeIdIndexedLinks, link);
            deleteLinkFromNodeIdIndexedLinks(dstNodeIdIndexedLinks, link);
            log.info("Remove link with source as " + link.getSource().getSourceTp().getValue()
                    + " and link id as "+ link.getLinkId().getValue());
        }
    }

    @Override
    public Link getLinkByLinkId(String linkId) {
        return linkIdIndexedLinks.get(linkId);
    }

    @Override
    public List<Link> getLinksBySourceNodeId(String srcNodeId) {
        return getLinksByNodeId(srcNodeIdIndexedLinks, srcNodeId);
    }

    @Override
    public List<Link> getLinksByDestinationNodeId(String dstNodeId) {
        return getLinksByNodeId(dstNodeIdIndexedLinks, dstNodeId);
    }

    @Override
    public Link getLinkBySourceTpId(String srcTpId) {
        return getLinksByNodeConnectorTpId(srcTpIdIndexedLinks, srcTpId).get(0);
    }

    @Override
    public Link getLinkByDestinationTpId(String dstTpId) {
        return getLinksByNodeConnectorTpId(dstTpIdIndexedLinks, dstTpId).get(0);
    }

    @Override
    public Link getRevertedLink(Link link) {
        return null;
    }


    private void addLinkToTpIdIndexedLinks(Map<String, Set<String>> tpIdIndexedLinks, Link link) {
        String key = link.getSource().getSourceTp().getValue();
        String value = link.getLinkId().getValue();
        addLinkToIndexedLinks(tpIdIndexedLinks, key, value);
    }

    private void addLinkToNodeIdIndexedLinks(Map<String, Set<String>> nodeIdIndexedLinks, Link link) {
        String key = link.getSource().getSourceNode().getValue();
        String value = link.getLinkId().getValue();
        addLinkToIndexedLinks(nodeIdIndexedLinks, key, value);
    }

    private void addLinkToIndexedLinks(Map<String, Set<String>> indexedLinks, String key, String value) {
        if (!indexedLinks.containsKey(key)) {
            Set<String> linkIdSet = new HashSet<String>();
            indexedLinks.put(key, linkIdSet);
        }
        indexedLinks.get(key).add(value);
    }

    private void deleteLinkFromTpIdIndexedLinks(Map<String, Set<String>> tpIdIndexedLinks, Link link) {
        String key = link.getSource().getSourceTp().getValue();
        String value = link.getLinkId().getValue();
        deleteLinkFromIndexedLinks(tpIdIndexedLinks, key, value);
    }

    private void deleteLinkFromNodeIdIndexedLinks(Map<String, Set<String>> nodeIdIndexedLinks, Link link) {
        String key = link.getSource().getSourceNode().getValue();
        String value = link.getLinkId().getValue();
        deleteLinkFromIndexedLinks(nodeIdIndexedLinks, key, value);
    }

    private void deleteLinkFromIndexedLinks(Map<String, Set<String>> indexedLinks, String key, String value) {
        if (indexedLinks.containsKey(key)) {
            indexedLinks.get(key).remove(value);
        }
    }

    private List<Link> getLinksByNodeConnectorTpId(Map<String, Set<String>> tpIdIndexedLinks, String tpId) {
        return getLinks(tpIdIndexedLinks, tpId);
    }

    private List<Link> getLinksByNodeId(Map<String, Set<String>> nodeIdIndexedLinks, String nodeId) {
        return getLinks(nodeIdIndexedLinks, nodeId);
    }

    private List<Link> getLinks(Map<String, Set<String>> indexedLinks, String key) {
        List<Link> links = new ArrayList<Link>();
        if (indexedLinks.containsKey(key)) {
            for (String linkId : indexedLinks.get(key)) {
                links.add(getLinkByLinkId(linkId));
            }
        }
        return links;
    }
}
