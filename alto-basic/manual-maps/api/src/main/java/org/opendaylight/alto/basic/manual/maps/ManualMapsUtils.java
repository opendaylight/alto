/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.basic.manual.maps;

import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.costmap.rev151021.cost.map.Meta;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.networkmap.rev151021.network.map.Map;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.ConfigContext;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.ConfigContextBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.ConfigContextKey;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.config.context.ResourceCostMap;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.config.context.ResourceCostMapBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.config.context.ResourceCostMapKey;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.config.context.ResourceNetworkMap;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.config.context.ResourceNetworkMapBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.manual.maps.rev151021.config.context.ResourceNetworkMapKey;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.Tag;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ManualMapsUtils {

    public static final String DEFAULT_CONTEXT = "00000000-0000-0000-0000-000000000000";

    public static InstanceIdentifier<ConfigContext> getDefaultContextIID() {
        return getContextIID(DEFAULT_CONTEXT);
    }

    public static InstanceIdentifier<ConfigContext> getContextIID(String cid) {
        return getContextIID(new Uuid(cid));
    }

    public static InstanceIdentifier<ConfigContext> getContextIID(Uuid cid) {
        return getContextIID(new ConfigContextKey(cid));
    }

    public static InstanceIdentifier<ConfigContext> getContextIID(ConfigContextKey key) {
        return InstanceIdentifier.builder(ConfigContext.class, key).build();
    }

    public static InstanceIdentifier<ConfigContext> getContextListIID() {
        return InstanceIdentifier.builder(ConfigContext.class).build();
    }

    public static InstanceIdentifier<ResourceNetworkMap> getResourceNetworkMapIID(String rid) {
        return getResourceNetworkMapIID(DEFAULT_CONTEXT, rid);
    }

    public static InstanceIdentifier<ResourceNetworkMap> getResourceNetworkMapIID(String cid, String rid) {
        return getResourceNetworkMapIID(new Uuid(cid), new ResourceId(rid));
    }

    public static InstanceIdentifier<ResourceNetworkMap> getResourceNetworkMapIID(Uuid cid, ResourceId rid) {
        return getResourceIID(new ConfigContextKey(cid), new ResourceNetworkMapKey(rid),
            ResourceNetworkMap.class);
    }

    public static InstanceIdentifier<ResourceCostMap> getResourceCostMapIID(String rid) {
        return getResourceCostMapIID(DEFAULT_CONTEXT, rid);
    }

    public static InstanceIdentifier<ResourceCostMap> getResourceCostMapIID(String cid, String rid) {
        return getResourceCostMapIID(new Uuid(cid), new ResourceId(rid));
    }

    public static InstanceIdentifier<ResourceCostMap> getResourceCostMapIID(Uuid cid, ResourceId rid) {
        ConfigContextKey ckey = new ConfigContextKey(cid);
        ResourceCostMapKey rkey = new ResourceCostMapKey(rid);
        return getResourceIID(new ConfigContextKey(cid), new ResourceCostMapKey(rid),
            ResourceCostMap.class);
    }

    public static <T extends Identifiable<K> & ChildOf<? super ConfigContext>, K extends Identifier<T>>
    InstanceIdentifier<T> getResourceIID(ConfigContextKey ckey, K rkey, Class<T> resourceType) {
        return InstanceIdentifier.builder(ConfigContext.class, ckey)
                .child(resourceType ,rkey).build();
    }

    public static Uuid createContext(final WriteTransaction wx) {
        return createContext(DEFAULT_CONTEXT, wx);
    }

    public static Uuid createContext(String cid, final WriteTransaction wx) {
        return createContext(new Uuid(cid), wx);
    }

    public static Uuid createContext(Uuid cid, final WriteTransaction wx) {
        ConfigContextBuilder builder = new ConfigContextBuilder();
        builder.setContextId(cid);
        builder.setResourceNetworkMap(new LinkedList<>());
        builder.setResourceCostMap(new LinkedList<>());

        wx.put(LogicalDatastoreType.CONFIGURATION, getContextIID(cid), builder.build());
        return cid;
    }

    public static InstanceIdentifier<ResourceNetworkMap> createResourceNetworkMap(String rid,
            List<Map> networkMap,
            WriteTransaction wx) {
        InstanceIdentifier<ResourceNetworkMap> iid = getResourceNetworkMapIID(rid);
        ResourceNetworkMapBuilder builder = new ResourceNetworkMapBuilder()
            .setTag(new Tag(UUID.nameUUIDFromBytes(rid.getBytes())
                .toString()
                .replaceAll("-", "")))
            .setResourceId(new ResourceId(rid))
            .setMap(networkMap);
        wx.put(LogicalDatastoreType.CONFIGURATION, iid, builder.build());
        return iid;
    }

    public static InstanceIdentifier<ResourceCostMap> createResourceCostMap(String rid,
            Meta meta,
            List<org.opendaylight.yang.gen.v1.urn.alto.manual.maps.costmap.rev151021.cost.map.Map> costMap,
            WriteTransaction wx) {
        InstanceIdentifier<ResourceCostMap> iid = getResourceCostMapIID(rid);
        ResourceCostMapBuilder builder = new ResourceCostMapBuilder()
            .setTag(new Tag(UUID.nameUUIDFromBytes(rid.getBytes())
                .toString()
                .replaceAll("-", "")))
            .setResourceId(new ResourceId(rid))
            .setMap(costMap)
            .setMeta(meta);
        wx.put(LogicalDatastoreType.CONFIGURATION, iid, builder.build());
        return iid;
    }

    public static void deleteContext(String cid, final WriteTransaction wx) {
        deleteContext(new Uuid(cid), wx);
    }

    public static void deleteContext(Uuid cid, final WriteTransaction wx) {
        /* DO NOT submit because this might be just one step in a sequence of write operations */
        wx.delete(LogicalDatastoreType.CONFIGURATION, getContextIID(cid));
    }

    public static void deleteResourceNetworkMap(String rid, final WriteTransaction wx) {
        deleteResourceNetworkMap(DEFAULT_CONTEXT, rid, wx);
    }

    public static void deleteResourceNetworkMap(String cid, String rid, final WriteTransaction wx) {
        deleteResourceNetworkMap(new Uuid(cid), new ResourceId(cid), wx);
    }

    public static void deleteResourceNetworkMap(Uuid cid, ResourceId rid,
            final WriteTransaction wx) {
        wx.delete(LogicalDatastoreType.CONFIGURATION, getResourceNetworkMapIID(cid, rid));
    }

    public static void deleteResourceCostMap(String rid, final WriteTransaction wx) {
        deleteResourceCostMap(DEFAULT_CONTEXT, rid, wx);
    }

    public static void deleteResourceCostMap(String cid, String rid, final WriteTransaction wx) {
        deleteResourceCostMap(new Uuid(cid), new ResourceId(cid), wx);
    }

    public static void deleteResourceCostMap(Uuid cid, ResourceId rid, final WriteTransaction wx) {
        wx.delete(LogicalDatastoreType.CONFIGURATION, getResourceCostMapIID(cid, rid));
    }
}
