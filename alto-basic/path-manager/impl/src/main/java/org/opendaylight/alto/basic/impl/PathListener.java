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
import org.opendaylight.alto.basic.helper.PathManagerHelper;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathListener implements DataTreeChangeListener<Flow> {

    private static final Logger LOG = LoggerFactory.getLogger(PathListener.class);
    private static final Short DEFAULT_TABLE_ID = 0;

    private final PathManagerUpdater updater;

    public PathListener(final PathManagerUpdater updater) {
        this.updater = updater;
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Flow>> changes) {
        if (changes == null) {
            LOG.info("This line should never be reached because of @Nonnull.");
            return;
        }
        for (DataTreeModification<Flow> change : changes) {
            if (change == null) {
                continue;
            }
            final InstanceIdentifier<Flow> iid = change.getRootPath().getRootIdentifier();
            if (iid.firstKeyOf(Table.class).getId().equals(DEFAULT_TABLE_ID)) {
                final DataObjectModification<Flow> rootNode = change.getRootNode();
                switch (rootNode.getModificationType()) {
                    case WRITE:
                        onFlowRuleCreated(iid, rootNode.getDataBefore(), rootNode.getDataAfter());
                        break;
                    case SUBTREE_MODIFIED:
                        onFlowRuleUpdated(iid, rootNode.getDataBefore(), rootNode.getDataAfter());
                        break;
                    case DELETE:
                        onFlowRuleDeleted(iid, rootNode.getDataBefore());
                }
            }
        }
    }

    protected void onFlowRuleCreated(InstanceIdentifier<Flow> iid, Flow dataBefore,
            Flow dataAfter) {
        if (dataBefore == null) {
            LOG.debug("Flow rule is created: {}.", iid);
            updater.newFlowRule(iid.firstKeyOf(Node.class).getId().getValue(), dataAfter);
            return;
        } else if (PathManagerHelper.isFlowRuleDiff(dataBefore, dataAfter)) {
            LOG.debug("Flow rule changed in WRITE mod: {}.", iid);
            updater.updateFlowRule(iid.firstKeyOf(Node.class).getId().getValue(), dataBefore,
                    dataAfter);
            return;
        }
        LOG.debug("No change on this flow rule: {}.", iid);
        return;
    }

    protected void onFlowRuleUpdated(InstanceIdentifier<Flow> iid, Flow dataBefore,
            Flow dataAfter) {
        if (PathManagerHelper.isFlowRuleDiff(dataBefore, dataAfter)) {
            LOG.debug("Flow rule changed in SUBTREE_MODIFIED mod: {}.", iid);
            updater.updateFlowRule(iid.firstKeyOf(Node.class).getId().getValue(), dataBefore,
                    dataAfter);
            return;
        }
        LOG.debug("No change on this flow rule: {}.", iid);
        return;
    }

    protected void onFlowRuleDeleted(InstanceIdentifier<Flow> iid, Flow dataBefore) {
        LOG.debug("Flow rule is deleted: {}.", iid);
        updater.deleteFlowRule(iid.firstKeyOf(Node.class).getId().getValue(), dataBefore);
        return;
    }
}
