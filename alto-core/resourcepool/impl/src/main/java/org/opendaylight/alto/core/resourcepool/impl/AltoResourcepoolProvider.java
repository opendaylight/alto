/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.resourcepool.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.SettableFuture;
//import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.CheckedFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
//import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;

//import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.AddResourceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.AltoResourcepoolService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.CapabilitySpec;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.RemoveResourceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.ResourceDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.ResourcePool;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.ResourcePoolBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.ResourceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.UpdateResourceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.VerifyResourceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.Resource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.ResourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.desc.Dependency;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.ResourceKey;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;

//import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
//import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.alto.core.resourcepool.impl.AltoResourcepoolServiceHelper.ResourceCallback;

public class AltoResourcepoolProvider implements BindingAwareProvider, AutoCloseable, AltoResourcepoolService {

    private static final Logger LOG = LoggerFactory.getLogger(AltoResourcepoolProvider.class);

    private InstanceIdentifier<ResourcePool> m_rpIID;

    private DataBroker m_dataBrokerService = null;
    // TODO private NotificationProviderService notificationService = null;

    private RpcRegistration<AltoResourcepoolService> m_serviceReg = null;

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoResourcesProvider Session Initiated");

        m_dataBrokerService = session.getSALService(DataBroker.class);
        m_serviceReg = session.addRpcImplementation(AltoResourcepoolService.class, this);
        assert m_dataBrokerService != null;
        assert m_serviceReg != null;

        m_rpIID = InstanceIdentifier.builder(ResourcePool.class).build();

        try {
            createTopLevelContainers();
        } catch (Exception e) {
            LOG.error("Failed to create top-level containers for ALTO services");
        }
    }

    protected void createTopLevelContainers()
            throws TransactionCommitFailedException, InterruptedException, ExecutionException {
        WriteTransaction wx = m_dataBrokerService.newWriteOnlyTransaction();

        ResourcePool rp;

        rp = new ResourcePoolBuilder().setResource(new LinkedList<Resource>()).build();
        wx.put(LogicalDatastoreType.OPERATIONAL, m_rpIID, rp);

        wx.submit().get();
        // Only proceed if the top-level containers are created successfully
    }

    protected void deleteTopLevelContainers() throws Exception {
        WriteTransaction wx = m_dataBrokerService.newWriteOnlyTransaction();

        wx.delete(LogicalDatastoreType.OPERATIONAL, m_rpIID);

        wx.submit().get();
    }

    @Override
    public void close() throws Exception {
        try {
            deleteTopLevelContainers();
        } catch (Exception e) {
            /* Exit anyway */
        }
        if (m_serviceReg != null) {
            m_serviceReg.close();
        }
        LOG.info("AltoResourcesProvider Closed");
    }

    protected InstanceIdentifier<Resource> getResourceIID(ResourceId rid) {
        ResourceKey key = new ResourceKey(rid);
        return InstanceIdentifier.builder(ResourcePool.class).child(Resource.class, key).build();
    }

    protected ListenableFuture<RpcResult<Void>> failWithError(String errorMessage) {
        return RpcResultBuilder.<Void>failed().withError(ErrorType.APPLICATION, errorMessage).buildFuture();
    }

    @Override
    public Future<RpcResult<Void>> addResource(final AddResourceInput input) {
        final ReadWriteTransaction rwx = m_dataBrokerService.newReadWriteTransaction();

        final ResourceDesc desc = (ResourceDesc)input;
        final ResourceService service = (ResourceService)input;
        return rpcTemplate(rwx, desc, service, new ResourceCallback() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(Optional<Resource> resource) {
                if (resource.isPresent()) {
                    return failWithError("Adding resource failure: already exists");
                }

                InstanceIdentifier<Resource> rIID = getResourceIID(desc.getResourceId());

                ResourceBuilder builder = new ResourceBuilder();
                builder.fieldsFrom(desc);
                builder.fieldsFrom(service);
                builder.setRefcount(0);
                /* TODO setTag() and dynamic tag generation */

                rwx.put(LogicalDatastoreType.OPERATIONAL, rIID, builder.build());
                CheckedFuture<Void, ? extends Exception> write = rwx.submit();

                try {
                    write.get();
                    return RpcResultBuilder.<Void>success().buildFuture();
                } catch (Exception e) {
                    return failWithError("Failed to update data store: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public Future<RpcResult<Void>> removeResource(RemoveResourceInput input) {
        //TODO
        return null;
    }

    @Override
    public Future<RpcResult<Void>> updateResource(UpdateResourceInput input) {
        //TODO
        return null;
    }

    @Override
    public Future<RpcResult<Void>> verifyResource(final VerifyResourceInput input) {
        ReadTransaction rx = m_dataBrokerService.newReadOnlyTransaction();

        ListenableFuture<String> message;
        message = _verifyResource(rx, (ResourceDesc)input, (ResourceService)input);

        Future<RpcResult<Void>> result;
        result = Futures.transform(message, new AsyncFunction<String, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(String errorMessage) {
                if (errorMessage != null) {
                    return failWithError("Verification failure: " + errorMessage);
                }
                return RpcResultBuilder.<Void>success().buildFuture();
            }
        });
        return result;
    }

    protected ListenableFuture<RpcResult<Void>> rpcTemplate(final ReadTransaction rx,
                                                    final ResourceDesc desc,
                                                    final ResourceService service,
                                                    final ResourceCallback callback) {
        ListenableFuture<String> message;
        message = _verifyResource(rx, desc, service);

        ListenableFuture<RpcResult<Void>> result;
        result = Futures.transform(message, new AsyncFunction<String, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(String errorMessage) {
                if (errorMessage != null) {
                    return failWithError("Verification failure: " + errorMessage);
                }

                InstanceIdentifier<Resource> rIID = getResourceIID(desc.getResourceId());
                ListenableFuture<Optional<Resource>> resource;
                resource = rx.read(LogicalDatastoreType.OPERATIONAL, rIID);

                return Futures.transform(resource, callback);
            }
        });
        return result;
    }

    protected ListenableFuture<String> _verifyResource(ReadTransaction rx,
                                                final ResourceDesc desc,
                                                final ResourceService service) {
        ListenableFuture<Optional<ResourcePool>> pool;
        pool = rx.read(LogicalDatastoreType.OPERATIONAL, m_rpIID);

        ListenableFuture<String> result;
        result = Futures.transform(pool, new AsyncFunction<Optional<ResourcePool>, String>() {
            @Override
            public ListenableFuture<String> apply(Optional<ResourcePool> pool) {
                String message = null;

                if (!pool.isPresent()) {
                    message = "Failed to read top-level container";
                } else {
                    ResourcePool rp = pool.get();
                    message = __verifyResource(desc, service, rp);
                }

                SettableFuture<String> messageFuture = SettableFuture.<String>create();
                messageFuture.set(message);
                return messageFuture;
            }
        });
        return result;
    }

    protected String __verifyResource(ResourceDesc desc, ResourceService service, ResourcePool rp) {
        if ((desc == null) || (service == null)) {
            return "Missing desc or service";
        }

        if (desc.getResourceId() == null) {
            return "Resource Id must not be null";
        }

        /* If resource is local, the uri must be provided.  Otherwise the uri will be emitted. */
        if (Boolean.FALSE.equals(desc.isLocal())) {
            if ((desc.getUri() == null) || (desc.getUri().equals(""))) {
                return "URI must be provided for remote resourcepool";
            }
        }

        if (desc.getType() == null) {
            return "Type must not be null";
        }
        //TODO validate the type

        // TODO Use type validator to validate the capabilities
        if (desc.getCapability() == null) {
        }

        if (desc.getCapability() != null) {
            for (CapabilitySpec spec: desc.getCapability()) {
                //TODO validate the capability spec
            }
        }

        if (desc.getDependency() != null) {
            Set<ResourceId> available = new HashSet<ResourceId>();

            for (Resource resource: rp.getResource()) {
                available.add(resource.getResourceId());
            }

            for (Dependency dependency: desc.getDependency()) {
                ResourceId rid = dependency.getResourceId();
                if (!available.contains(rid)) {
                    return "Unresolved dependency: " + rid.getValue();
                }
            }
        }
        return null;
    }
}
