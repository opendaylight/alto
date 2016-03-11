/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.util;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import java.util.ArrayList;
import java.util.List;

public class LinkNode {
    private Link link;
    private boolean isDestHost = false;
    private Long bandwidth;
    private List<LinkNode> children = new ArrayList<LinkNode>();

    public LinkNode(Link link) {
        this.link = link;
    }

    public LinkNode(Link link, LinkNode child) {
        this(link);
        this.children.add(child);
    }

    public LinkNode(String inPort, Link link, List<LinkNode> children) {
        this(link);
        this.children.addAll(children);
    }

    public String id() {
        return this.link.getLinkId().getValue();
    }

    public String srcTpId() {
        return this.link.getSource().getSourceTp().getValue();
    }

    public String dstTpId() {
        return this.link.getDestination().getDestTp().getValue();
    }

    public String srcNodeId() {
        return this.link.getSource().getSourceNode().getValue();
    }

    public String dstNodeId() {
        return this.link.getDestination().getDestNode().getValue();
    }

    public boolean startWithHost() {
        return this.srcTpId().toLowerCase().startsWith("host:");
    }

    public boolean endWithHost() {
        return this.dstTpId().toLowerCase().startsWith("host:");
    }

    public boolean isDestHost() {
        return this.isDestHost;
    }

    public List<LinkNode> children() {
        return this.children;
    }

    public void addChild(LinkNode child) {
        this.children.add(child);
    }

    public void addChildren(List<LinkNode> children) {
        this.children.addAll(children);
    }

    public void setAsDestHost() {
        this.isDestHost = true;
    }

    public Long availableBandwidth() {
        return this.bandwidth;
    }

    public void setAvailableBandwidth(Long bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Link getLink(){return this.link;}
}
