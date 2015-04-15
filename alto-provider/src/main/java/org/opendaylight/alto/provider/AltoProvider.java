package org.opendaylight.alto.provider;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opendaylight.controller.config.yang.config.alto_provider.impl.AltoProviderRuntimeMXBean;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.AltoServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.EndpointCostServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.EndpointCostServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.FilteredCostMapServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.FilteredCostMapServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.FilteredNetworkMapServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.FilteredNetworkMapServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.Resources;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class AltoProvider implements
            AltoServiceService, DataChangeListener,
            AltoProviderRuntimeMXBean, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(AltoProvider.class);

    public static final InstanceIdentifier<Resources> ALTO_IID
                        = InstanceIdentifier.builder(Resources.class).build();

    private DataBroker dataProvider;
    private final ExecutorService executor;

    public AltoProvider() {
        this.executor = Executors.newFixedThreadPool(1);
    }

    public void setDataProvider(final DataBroker salDataProvider) {
        this.dataProvider = salDataProvider;
        log.info(this.getClass().getName() + " data provider initiated");
    }

    @Override
    public void onDataChanged(
            final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        DataObject dataObject = change.getUpdatedSubtree();
        if (dataObject instanceof Resources) {
            Resources altoResources = (Resources) dataObject;
            log.info("onDataChanged - new ALTO config: {}", altoResources);
        }
    }

    @Override
    public Future<RpcResult<EndpointCostServiceOutput>> endpointCostService(
            EndpointCostServiceInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<FilteredCostMapServiceOutput>> filteredCostMapService(
            FilteredCostMapServiceInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<FilteredNetworkMapServiceOutput>> filteredNetworkMapService(
            final FilteredNetworkMapServiceInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() throws ExecutionException, InterruptedException {
        executor.shutdown();
        if (dataProvider != null) {
            WriteTransaction tx = dataProvider.newWriteOnlyTransaction();
            tx.delete(LogicalDatastoreType.CONFIGURATION, ALTO_IID);
            Futures.addCallback(tx.submit(), new FutureCallback<Void>() {
                @Override
                public void onSuccess(final Void result) {
                    log.debug("Delete ALTO commit result: " + result);
                }

            @Override
            public void onFailure(final Throwable t) {
                log.error("Delete of ALTO failed", t);
            }
            });
        }
    }
}
