/*
 * Copyright © 2015 Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.spce.impl.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FlowInfoKey {
    private InstanceIdentifier<Node> nodeIID;
    private Match match;

    public FlowInfoKey(InstanceIdentifier<Node> nodeIID, Match match) {
        this.nodeIID = nodeIID;
        this.match = match;
    }

    public int hashCode() {
        return this.nodeIID.hashCode()+this.match.hashCode();
    }

    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FlowInfoKey flowInfoKey = (FlowInfoKey) obj;

        if (null == flowInfoKey.nodeIID || null == flowInfoKey.match
                || !(this.nodeIID.toString().equals(flowInfoKey.nodeIID.toString()))
                || !(this.match.toString().equals(flowInfoKey.match.toString()))
        ) {
            return false;
        }

        return true;
    }

}
