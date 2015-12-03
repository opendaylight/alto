/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.service.model.costmap;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.CostMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.context.resource.capabilities.CostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rev151021.context.resource.capabilities.CostTypeBuilder;


public class CostmapUtils {
    public static CostType createCostTypeCapability(String metric, String mode){
        return createCostTypeCapability(new CostMetric(metric),mode);
    }

    public static CostType createCostTypeCapability(CostMetric metric,String mode){
        CostTypeBuilder builder= new CostTypeBuilder();
        builder.setCostMetric(metric);
        builder.setCostMode(mode);
        return builder.build();
    }
}
