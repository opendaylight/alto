/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyLinkListener implements DataTreeChangeListener<Link> {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyLinkListener.class);

    private final PathManagerUpdater updater;

    public TopologyLinkListener(final PathManagerUpdater updater) {
        this.updater = updater;
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Link>> changes) {
        for (DataTreeModification<Link> change : changes) {
            if (change == null) {
                continue;
            }
            final InstanceIdentifier<Link> iid = change.getRootPath().getRootIdentifier();
            final DataObjectModification<Link> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                case SUBTREE_MODIFIED:
                    onLinkUpdated(rootNode.getDataAfter());
                    break;
                case DELETE:
                    onLinkDeleted(rootNode.getDataBefore());
            }
        }
    }

    private void onLinkUpdated(Link newLink) {
        if (newLink != null) {
            updater.addLink(newLink.getSource().getSourceTp().getValue(), newLink.getLinkId());
        }
    }

    private void onLinkDeleted(Link oldLink) {
        if (oldLink != null) {
            updater.removeLink(oldLink.getSource().getSourceTp().getValue());
        }
    }
}
