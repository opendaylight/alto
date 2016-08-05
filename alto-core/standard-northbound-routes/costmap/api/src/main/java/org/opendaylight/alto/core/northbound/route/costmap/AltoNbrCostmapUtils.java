/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.northbound.route.costmap;

import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.costmap.rev151021.Records;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.costmap.rev151021.records.Record;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.costmap.rev151021.records.RecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.costmap.rev151021.records.RecordKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AltoNbrCostmapUtils {

    public static final String BASE_URL = "/alto/costmap";

    public static InstanceIdentifier<Record> getRecordIID(String path) {
        RecordKey key = new RecordKey(new Uri(path));
        return InstanceIdentifier.builder(Records.class).child(Record.class, key).build();
    }

    public static String createRecord(String path, ResourceId rid, final WriteTransaction wx) {
        return createRecord(path, rid, new Uuid(ResourcepoolUtils.DEFAULT_CONTEXT), wx);
    }

    public static String createRecord(String path, ResourceId rid, Uuid cid, final WriteTransaction wx) {
        InstanceIdentifier<Record> iid = getRecordIID(path);
        RecordBuilder builder = new RecordBuilder();
        builder.setPath(new Uri(path));
        builder.setResourceIid(ResourcepoolUtils.getResourceIID(cid, rid));

        wx.put(LogicalDatastoreType.CONFIGURATION, iid, builder.build());

        return path;
    }

    public static void deleteRecord(String path, final WriteTransaction wx) {
        InstanceIdentifier<Record> iid = getRecordIID(path);

        wx.delete(LogicalDatastoreType.CONFIGURATION, iid);
    }
}

