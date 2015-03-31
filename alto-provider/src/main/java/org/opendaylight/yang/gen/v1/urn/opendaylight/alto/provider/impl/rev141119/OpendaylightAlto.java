package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.provider.impl.rev141119;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.controller.config.yang.config.alto_provider.impl.AltoProviderRuntimeMXBean;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.EndpointCostServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.EndpointCostServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.EndpointPropertyServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.EndpointPropertyServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.FilteredCostMapServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.FilteredCostMapServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.FilteredNetworkMapServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.FilteredNetworkMapServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.FilteredNetworkMapServiceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.Resources;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.ResourcesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.AltoServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.filtered.network.map.service.output.FilteredNetworkMapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.filtered.network.map.service.output.FilteredNetworkMapServiceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.resources.NetworkMaps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.resources.NetworkMapsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.resources.network.maps.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.resources.network.maps.NetworkMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev141119.resources.network.maps.NetworkMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.did.rev141101.network.map.data.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.did.rev141101.network.map.data.MapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev141101.EndpointAddressType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev141101.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev141101.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev141101.TagString;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev141101.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev141101.endpoint.address.group.EndpointAddressGroupBuilder;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class OpendaylightAlto implements AltoServiceService,
    AltoProviderRuntimeMXBean, DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory
        .getLogger(OpendaylightAlto.class);

    public static final InstanceIdentifier<Resources> ALTO_IID = InstanceIdentifier
        .builder(Resources.class).build();

    // Currently we don't have any notifications.
    private NotificationProviderService notificationProvider;
    private DataBroker dataProvider;

    private final ExecutorService executor;

    // dummy state data example
    private final AtomicLong toastsMade = new AtomicLong(0);

    public OpendaylightAlto() {
        executor = Executors.newFixedThreadPool(1);
    }

    public void setNotificationProvider(
        final NotificationProviderService salService) {
        this.notificationProvider = salService;
    }

    public void setDataProvider(final DataBroker salDataProvider) {
        this.dataProvider = salDataProvider;
        setResourcesStatusUp(null);
    }

    /**
     * Implemented from the AutoCloseable interface. Delete all resources.
     */
    @Override
    public void close() throws ExecutionException, InterruptedException {
        // When we close this service we need to shutdown our executor!
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

    private Resources buildResources() {
        return new ResourcesBuilder().setNetworkMaps(buildDummyNetworkMaps())
            .setTeststring("TestAltoRestconfDIDWired")
            .setTestdatastring("tstdatastring")
            .setNetworkMaps(buildDummyNetworkMaps()).build();
    }

    /*
     * builds a dummy container networkmaps
     */
    private NetworkMaps buildDummyNetworkMaps() {
        ResourceId dummyResourceId = new ResourceId("my-dummy-net-map-1");

        PidName pid1 = new PidName("PID1");
        PidName pid2 = new PidName("PID2");
        PidName pid3 = new PidName("PID3");

        EndpointAddressType ipv4Type = new EndpointAddressType(
            EndpointAddressType.Enumeration.Ipv4);
        EndpointAddressType ipv6Type = new EndpointAddressType(
            EndpointAddressType.Enumeration.Ipv6);

        /* for PID1 */
        List<IpPrefix> pid1list = new ArrayList<>();
        pid1list.add(new IpPrefix(new Ipv4Prefix("192.0.2.0/24")));
        pid1list.add(new IpPrefix(new Ipv4Prefix("198.51.100.0/25")));

        EndpointAddressGroup eag1 = new EndpointAddressGroupBuilder()
            .setAddressType(ipv4Type).setEndpointPrefix(pid1list).build();

        List<EndpointAddressGroup> eagList1 = new ArrayList<>();
        eagList1.add(eag1);

        Map dummyMap1 = new MapBuilder().setPid(pid1)
            .setEndpointAddressGroup(eagList1).build();

        /* for PID2 */
        List<IpPrefix> pid2list = new ArrayList<>();
        pid2list.add(new IpPrefix(new Ipv4Prefix("198.51.100.128/25")));

        EndpointAddressGroup eag2 = new EndpointAddressGroupBuilder()
            .setAddressType(ipv4Type).setEndpointPrefix(pid2list).build();

        List<EndpointAddressGroup> eagList2 = new ArrayList<>();
        eagList2.add(eag2);
        Map dummyMap2 = new MapBuilder().setPid(pid2)
            .setEndpointAddressGroup(eagList2).build();

        /* for PID3 */
        List<IpPrefix> pid3ipv4List = new ArrayList<>();
        pid3ipv4List.add(new IpPrefix(new Ipv4Prefix("0.0.0.0/0")));
        EndpointAddressGroup eag3ipv4 = new EndpointAddressGroupBuilder()
            .setAddressType(ipv4Type).setEndpointPrefix(pid3ipv4List).build();

        List<IpPrefix> pid3ipv6List = new ArrayList<>();
        pid3ipv6List.add(new IpPrefix(new Ipv6Prefix("::/0")));
        EndpointAddressGroup eag3ipv6 = new EndpointAddressGroupBuilder()
            .setAddressType(ipv6Type).setEndpointPrefix(pid3ipv6List).build();

        List<EndpointAddressGroup> eagList3 = new ArrayList<>();
        eagList3.add(eag3ipv4);
        eagList3.add(eag3ipv6);
        Map dummyMap3 = new MapBuilder().setPid(pid3)
            .setEndpointAddressGroup(eagList3).build();

        /* put together default dummy net map */
        List<Map> dummyListMap = new ArrayList<>();
        dummyListMap.add(dummyMap1);
        dummyListMap.add(dummyMap2);
        dummyListMap.add(dummyMap3);

        NetworkMapBuilder dummyNetworkMapBuilder = new NetworkMapBuilder();
        dummyNetworkMapBuilder.setResourceId(dummyResourceId)
            .setTag(new TagString("dummyv01")).setMap(dummyListMap);

        List<NetworkMap> dummyListNetworkMap = new ArrayList<>();
        dummyListNetworkMap.add(dummyNetworkMapBuilder.build());

        /* wrap with network-maps container */
        return new NetworkMapsBuilder().setNetworkMap(dummyListNetworkMap)
            .build();
    }

    /**
     * Implemented from the DataChangeListener interface.
     */
    @Override
    public void onDataChanged(
        final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        DataObject dataObject = change.getUpdatedSubtree();
        if (dataObject instanceof Resources) {
            Resources altoResources = (Resources) dataObject;
            LOG.info("onDataChanged - new ALTO config: {}", altoResources);
        }
    }

    /* dummy example state data TODO */

    /**
     * JMX RPC call implemented from the AltoProviderRuntimeMXBean interface.
     */
    @Override
    public void clearToastsMade() {
        LOG.info("clearToastsMade");
        toastsMade.set(0);
    }

    /**
     * Accesssor method implemented from the ToasterProviderRuntimeMXBean
     * interface.
     */
    @Override
    public Long getToastsMade() {
        return toastsMade.get();
    }

    private void setResourcesStatusUp(
        final Function<Boolean, Void> resultCallback) {

        WriteTransaction tx = dataProvider.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, ALTO_IID, buildResources());

        Futures.addCallback(tx.submit(), new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                notifyCallback(true);
            }

            @Override
            public void onFailure(final Throwable t) {
                // We shouldn't get an OptimisticLockFailedException (or any ex)
                // as no
                // other component should be updating the operational state.
                LOG.error("Failed to update toaster status", t);

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
    public Future<RpcResult<EndpointPropertyServiceOutput>> endpointPropertyService(
        EndpointPropertyServiceInput input) {
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

        ResourceId rid = input.getResourceId();
        NetworkMapKey networkMapKey = new NetworkMapKey(rid);

        final ReadOnlyTransaction tx = dataProvider.newReadOnlyTransaction();
        ListenableFuture<Optional<NetworkMap>> readFuture = tx.read(
            LogicalDatastoreType.OPERATIONAL, ALTO_IID.child(NetworkMaps.class)
                .child(NetworkMap.class, networkMapKey));

        final ListenableFuture<RpcResult<FilteredNetworkMapServiceOutput>> futureResult = Futures
            .transform(
                readFuture,
                new AsyncFunction<Optional<NetworkMap>, RpcResult<FilteredNetworkMapServiceOutput>>() {

                    @Override
                    public ListenableFuture<RpcResult<FilteredNetworkMapServiceOutput>> apply(
                        Optional<NetworkMap> networkMapData) throws Exception {

                        ResourceId networkMapId = new ResourceId(
                            "ResourceID Not Available");
                        TagString networkMapTag = new TagString(
                            "Tag Not Available");

                        if (networkMapData.isPresent()) {
                            networkMapId = ((NetworkMap) networkMapData.get())
                                .getResourceId();
                            networkMapTag = ((NetworkMap) networkMapData.get())
                                .getTag();
                        }

                        LOG.info("Read network map rid: {}", networkMapId);
                        LOG.info("Read network map tag: {}", networkMapTag);

                        List<PidName> pidList = input.getPids();

                        List<Map> networkMapMap = networkMapData.get().getMap();
                        List<Map> filteredMap = new ArrayList<>();

                        for (Map m : networkMapMap) {
                            PidName pid = m.getPid();
                            if (pidList.contains(pid)) {
                                filteredMap.add(m);
                            }
                        }

                        FilteredNetworkMapService serviceResult = new FilteredNetworkMapServiceBuilder()
                            .setResourceId(networkMapId).setTag(networkMapTag)
                            .setMap(filteredMap).build();

                        FilteredNetworkMapServiceOutput serviceOutput = new FilteredNetworkMapServiceOutputBuilder()
                            .setFilteredNetworkMapService(serviceResult)
                            .build();

                        return Futures.immediateFuture(RpcResultBuilder
                            .<FilteredNetworkMapServiceOutput> success()
                            .withResult(serviceOutput).build());
                    }
                });

        return futureResult;
    }
}
