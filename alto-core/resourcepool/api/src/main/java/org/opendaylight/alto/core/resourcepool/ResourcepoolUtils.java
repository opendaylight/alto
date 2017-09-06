/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.resourcepool;

import com.google.common.base.Optional;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.Context;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.ContextBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.ContextKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.ResourceType;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.ServiceContext;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.ResourceBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.ResourceKey;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.Capabilities;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.CapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTag;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTagBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.resourcepool.rev150921.context.resource.ContextTagKey;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.Tag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class ResourcepoolUtils {

    public static final String DEFAULT_CONTEXT = "00000000-0000-0000-0000-000000000000";

    public static InstanceIdentifier<Context> getDefaultContextIID() {
        return getContextIID(DEFAULT_CONTEXT);
    }

    public static InstanceIdentifier<Context> getContextIID(String cid) {
        Uuid _cid = new Uuid(cid);
        return getContextIID(_cid);
    }

    public static InstanceIdentifier<Context> getContextIID(Uuid cid) {
        ContextKey key = new ContextKey(cid);
        return getContextIID(key);
    }

    public static InstanceIdentifier<Context> getContextIID(ContextKey key) {
        return InstanceIdentifier.builder(Context.class, key).build();
    }

    public static InstanceIdentifier<Resource> getResourceIID(String cid, String rid) {
        Uuid _cid = new Uuid(cid);
        ResourceId _rid = new ResourceId(rid);

        return getResourceIID(_cid, _rid);
    }

    public static InstanceIdentifier<Resource> getResourceIID(String cid, ResourceId rid) {
        Uuid _cid = new Uuid(cid);
        return getResourceIID(_cid, rid);
    }

    public static InstanceIdentifier<Resource> getResourceIID(Uuid cid, String rid) {
        ResourceId _rid = new ResourceId(rid);
        return getResourceIID(cid, _rid);
    }

    public static InstanceIdentifier<Resource> getResourceIID(Uuid cid, ResourceId rid) {
        ContextKey contextKey = new ContextKey(cid);
        ResourceKey resourceKey = new ResourceKey(rid);

        return InstanceIdentifier.builder(Context.class, contextKey)
                                    .child(Resource.class, resourceKey).build();
    }

    public static InstanceIdentifier<ContextTag> getContextTagIID(String cid, String rid, String tag) {
        Uuid _cid = new Uuid(cid);
        ResourceId _rid = new ResourceId(rid);
        Tag _tag = new Tag(tag);

        return getContextTagIID(_cid, _rid, _tag);
    }

    public static InstanceIdentifier<ContextTag> getContextTagIID(Uuid cid, ResourceId rid, Tag tag) {
        ContextKey contextKey = new ContextKey(cid);
        ResourceKey resourceKey = new ResourceKey(rid);
        ContextTagKey contextTagKey = new ContextTagKey(tag);

        return InstanceIdentifier.builder(Context.class, contextKey)
                                    .child(Resource.class, resourceKey)
                                    .child(ContextTag.class, contextTagKey).build();
    }

    public static boolean contextExists(String cid, ReadTransaction rx)
                            throws InterruptedException, ExecutionException {
        return contextExists(new Uuid(cid), rx);
    }

    public static boolean contextExists(Uuid cid, ReadTransaction rx)
                            throws InterruptedException, ExecutionException {
        return contextExists(new ContextKey(cid), rx);
    }

    public static boolean contextExists(ContextKey key, ReadTransaction rx)
                            throws InterruptedException, ExecutionException {
        Optional<Context> context;
        context = rx.read(LogicalDatastoreType.OPERATIONAL, getContextIID(key)).get();

        return context.isPresent();
    }

    public static Uuid createRandomContext(final WriteTransaction wx) {
        String cid = UUID.randomUUID().toString();

        return createContext(cid, wx);
    }

    public static Uuid createContext(String cid, final WriteTransaction wx) {
        return createContext(new Uuid(cid), wx);
    }

    public static Uuid createContext(Uuid cid, final WriteTransaction wx) {
        ContextBuilder builder = new ContextBuilder();
        builder.setContextId(cid);
        builder.setResource(new LinkedList<Resource>());

        /* DO NOT submit because this might be just one step in a sequence of write operations */
        wx.put(LogicalDatastoreType.OPERATIONAL, getContextIID(cid), builder.build());

        return cid;
    }

    public static void deleteContext(String cid, final WriteTransaction wx) {
        deleteContext(new Uuid(cid), wx);
    }

    public static void deleteContext(Uuid cid, final WriteTransaction wx) {
        /* DO NOT submit because this might be just one step in a sequence of write operations */
        wx.delete(LogicalDatastoreType.OPERATIONAL, getContextIID(cid));
    }

    public static boolean resourceExists(String cid, String rid, ReadTransaction rx)
                            throws InterruptedException, ExecutionException {
        return resourceExists(new Uuid(cid), new ResourceId(rid), rx);
    }

    public static boolean resourceExists(Uuid cid, ResourceId rid, ReadTransaction rx)
                            throws InterruptedException, ExecutionException {
        InstanceIdentifier<Resource> iid = getResourceIID(cid, rid);

        Optional<Resource> resource;
        resource = rx.read(LogicalDatastoreType.OPERATIONAL, iid).get();

        return resource.isPresent();
    }

    public static ResourceId createResource(String cid, String rid,
                                        Class<? extends ResourceType> type,
                                        final WriteTransaction wx) {
        Uuid _cid = new Uuid(cid);
        ResourceId _rid = new ResourceId(rid);

        return createResource(_cid, _rid, type, wx);
    }

    public static ResourceId createResource(Uuid cid, ResourceId rid,
                                        Class<? extends ResourceType> type,
                                        final WriteTransaction wx) {
        return createResourceWithCapabilities(cid, rid, type, null, wx);
    }

    public static ResourceId createResourceWithCapabilities(String cid, String rid,
                                                        Class<? extends ResourceType> type,
                                                        Capabilities capabilities,
                                                        final WriteTransaction wx) {
        Uuid contextId = new Uuid(cid);
        ResourceId resourceId = new ResourceId(rid);

        return createResourceWithCapabilities(contextId, resourceId, type, capabilities, wx);
    }

    public static ResourceId createResourceWithCapabilities(Uuid cid, ResourceId rid,
                                                        Class<? extends ResourceType> type,
                                                        Capabilities capabilities,
                                                        final WriteTransaction wx) {
        ResourceBuilder builder = new ResourceBuilder();
        builder.setResourceId(rid);
        builder.setType(type);
        builder.setContextTag(new LinkedList<ContextTag>());
        if (capabilities == null) {
            capabilities = new CapabilitiesBuilder().build();
        }
        builder.setCapabilities(capabilities);

        InstanceIdentifier<Resource> iid = getResourceIID(cid, rid);

        /* DO NOT submit because this might be just one step in a sequence of write operations */
        wx.put(LogicalDatastoreType.OPERATIONAL, iid, builder.build());

        return rid;
    }

    public static void deleteResource(String cid, String rid, final WriteTransaction wx) {
        Uuid contextId = new Uuid(cid);
        ResourceId resourceId = new ResourceId(rid);

        deleteResource(contextId, resourceId, wx);
    }

    public static void deleteResource(Uuid cid, ResourceId rid, final WriteTransaction wx) {
        InstanceIdentifier<Resource> iid = getResourceIID(cid, rid);

        /* DO NOT submit because this might be just one step in a sequence of write operations */
        wx.delete(LogicalDatastoreType.OPERATIONAL, iid);
    }

    public static Tag updateResource(String cid, String rid,
                                        List<InstanceIdentifier<?>> dependencies,
                                        final WriteTransaction wx) {
        return updateResource(new Uuid(cid), new ResourceId(rid), dependencies, wx);
    }

    public static Tag updateResource(Uuid cid, ResourceId rid,
                                        List<InstanceIdentifier<?>> dependencies,
                                        final WriteTransaction wx) {
        String stripped = getUUID().replace("-", "");

        return updateResource(cid, rid, new Tag(stripped), dependencies, wx);
    }

    public static Tag updateResource(Uuid cid, ResourceId rid, Tag tag,
                                        List<InstanceIdentifier<?>> dependencies,
                                        final WriteTransaction wx) {
        ContextTagBuilder ctBuilder = new ContextTagBuilder();
        ctBuilder.setTag(tag);
        /*
         * Unfortunately the resources must handle the dependency resolving themselves
         * */
        if (dependencies == null) {
            dependencies = new LinkedList<>();
        }
        ctBuilder.setDependency(dependencies);

        ResourceBuilder rscBuilder = new ResourceBuilder();
        rscBuilder.setResourceId(rid);
        rscBuilder.setDefaultTag(tag);
        rscBuilder.setContextTag(Arrays.asList(ctBuilder.build()));

        InstanceIdentifier<Resource> iid = getResourceIID(cid, rid);

        /* DO NOT submit because this might be just one step in a sequence of write operations */
        wx.merge(LogicalDatastoreType.OPERATIONAL, iid, rscBuilder.build());

        return tag;
    }

    /*
     * For those who don't have dependencies
     * */
    public static Tag lazyUpdateResource(String cid, String rid, final WriteTransaction wx) {
        Uuid contextId = new Uuid(cid);
        ResourceId resourceId = new ResourceId(rid);

        return lazyUpdateResource(contextId, resourceId, wx);
    }

    public static Tag lazyUpdateResource(Uuid cid, ResourceId rid, final WriteTransaction wx) {
        String stripped = getUUID().replace("-", "");

        return updateResource(cid, rid, new Tag(stripped), null, wx);
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getUUID(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes()).toString();
    }

    public static final class ContextTagListener implements DataTreeChangeListener<ContextTag> {

        private RoutedRpcRegistration<? extends RpcService> m_registration = null;
        private InstanceIdentifier<Resource> m_iid = null;

        public ContextTagListener(InstanceIdentifier<Resource> resourceIID,
                                    RoutedRpcRegistration<? extends RpcService> registration) {
            m_iid = resourceIID;
            m_registration = registration;
        }

        @Override
        public void onDataTreeChanged(Collection<DataTreeModification<ContextTag>> changes) {
            for (DataTreeModification<ContextTag> change: changes) {
                final DataObjectModification<ContextTag> rootNode = change.getRootNode();
                final InstanceIdentifier<ContextTag> identifier = change.getRootPath().getRootIdentifier();
                if (!m_iid.contains(identifier)) {
                    // Only manage resource's own context tag
                    continue;
                }

                switch (rootNode.getModificationType()) {
                    case WRITE:
                        if (rootNode.getDataBefore() == null) {
                            m_registration.registerPath(ServiceContext.class, identifier);
                        }
                        break;
                    case DELETE:
                        m_registration.unregisterPath(ServiceContext.class, identifier);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
