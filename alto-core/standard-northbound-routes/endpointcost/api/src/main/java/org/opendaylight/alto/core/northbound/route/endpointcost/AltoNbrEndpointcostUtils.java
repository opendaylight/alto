/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.northbound.route.endpointcost;

/**
 * Created by wukunheng00 on 12/3/15.
 */

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.endpointcost.rev151021.Records;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.endpointcost.rev151021.records.Record;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.endpointcost.rev151021.records.RecordKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AltoNbrEndpointcostUtils {

    public static InstanceIdentifier<Record> getRecordIID(String path) {
        RecordKey key = new RecordKey(new Uri(path));
        return InstanceIdentifier.builder(Records.class).child(Record.class, key).build();
    }
}

