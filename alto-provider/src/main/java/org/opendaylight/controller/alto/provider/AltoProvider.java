package org.opendaylight.controller.alto.provider;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opendaylight.alto.commons.types.mapper.JSONMapper;
import org.opendaylight.controller.config.yang.config.alto_provider.impl.AltoProviderRuntimeMXBean;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.ResourcesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.NetworkMaps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.NetworkMapsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMap;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class AltoProvider implements AltoServiceService, DataChangeListener,
  AltoProviderRuntimeMXBean, AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(AltoProvider.class);

  public static final InstanceIdentifier<Resources> ALTO_IID = InstanceIdentifier
      .builder(Resources.class).build();
  
  private static final Path configFilePath = Paths.get("configuration/default.networkmap");
  private JSONMapper jsonMapper = new JSONMapper();
  
  private DataBroker dataProvider;
  private final ExecutorService executor;
  
  public AltoProvider() {
    this.executor = Executors.newFixedThreadPool(1);    
  }

  public void setDataProvider(final DataBroker salDataProvider) {
    this.dataProvider = salDataProvider;
    this.dataProvider.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, ALTO_IID, this, DataChangeScope.SUBTREE);
    setResourcesStatusUp(null);
  }

  private Resources buildResources() {
    try {
      return new ResourcesBuilder()
          .setNetworkMaps(loadNetworkMaps())
          .build();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  private NetworkMaps loadNetworkMaps() throws Exception {
    return new NetworkMapsBuilder()
        .setNetworkMap(loadNetworkMapList())
        .build();
  }
  
  private List<NetworkMap> loadNetworkMapList() {
    List<NetworkMap> networkMapList = new ArrayList<NetworkMap>();
    try {
      String content = new String(Files.readAllBytes(configFilePath), StandardCharsets.UTF_8);
      networkMapList.add(jsonMapper.asNetworkMap(content).asYangNetworkMap());
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return networkMapList;
  }

  @Override
  public void onDataChanged(
      final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
    DataObject dataObject = change.getUpdatedSubtree();
    if (dataObject instanceof Resources) {
      Resources altoResources = (Resources) dataObject;
      LOG.info("onDataChanged - new ALTO config: {}", altoResources);
    }
  }

  private void setResourcesStatusUp(final Function<Boolean, Void> resultCallback) {
    WriteTransaction tx = dataProvider.newWriteOnlyTransaction();
    tx.put(LogicalDatastoreType.OPERATIONAL, ALTO_IID, buildResources());

    Futures.addCallback(tx.submit(), new FutureCallback<Void>() {
      @Override
      public void onSuccess(final Void result) {
        notifyCallback(true);
      }

      @Override
      public void onFailure(final Throwable t) {
        LOG.error("Failed to initiate resources", t);
        notifyCallback(false);
      }

      void notifyCallback(final boolean result) {
        if (resultCallback != null) {
          resultCallback.apply(result);
        }
      }
    });
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
  public Long getToastsMade() {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public void close() throws ExecutionException, InterruptedException {
    executor.shutdown();
    if (dataProvider != null) {
      WriteTransaction tx = dataProvider.newWriteOnlyTransaction();
      tx.delete(LogicalDatastoreType.OPERATIONAL, ALTO_IID);
      Futures.addCallback(tx.submit(), new FutureCallback<Void>() {
        @Override
        public void onSuccess(final Void result) {
          LOG.debug("Delete ALTO commit result: " + result);
        }

        @Override
        public void onFailure(final Throwable t) {
          LOG.error("Delete of ALTO failed", t);
        }
      });
    }
  }
}
