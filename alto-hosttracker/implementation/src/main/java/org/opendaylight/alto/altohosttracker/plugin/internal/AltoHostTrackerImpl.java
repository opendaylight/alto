package org.opendaylight.alto.altohosttracker.plugin.internal;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.CheckedFuture;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.hosttracker.rev150416.DstCosts1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.hosttracker.rev150416.DstCosts1Builder;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.address.tracker.rev140617.AddressCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.address.tracker.rev140617.address.node.connector.Addresses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.Resources;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.ResourcesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.NetworkMaps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.NetworkMapsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.CostMaps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.CostMapsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.cost.maps.CostMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.cost.maps.CostMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.EndpointPropertyMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.EndpointPropertyMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TypedIpv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TypedEndpointAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.EndpointPropertyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.EndpointPropertyValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.data.EndpointProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.data.EndpointPropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.data.EndpointPropertiesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.data.endpoint.properties.Properties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.data.endpoint.properties.PropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceSpecificEndpointProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.MapBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TagString;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ValidIdString;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.cost.map.meta.CostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.cost.map.meta.CostTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.dependent.vtags.DependentVtags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.dependent.vtags.DependentVtagsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.map.DstCosts;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.map.DstCostsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.address.group.EndpointAddressGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.EndpointAddressType;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoHostTrackerImpl implements DataChangeListener {

    private Pattern p;
    private static final int CPUS = Runtime.getRuntime().availableProcessors();

    /**
     * As defined on
     * controller/opendaylight/md-sal/topology-manager/src/main/java
     * /org/opendaylight
     * /md/controller/topology/manager/FlowCapableTopologyProvider.java
     */
    private static final String TOPOLOGY_NAME = "flow:1";

    private static final String NMRESOURCEID = "hosttracker-network-map";

    private static final String CMRESOURCEID = "hosttracker-cost-map";

    private static final String EPMRESOURCEID = "hosttracker-endpoint-property-map";

    private static final Logger log = LoggerFactory
            .getLogger(AltoHostTrackerImpl.class);

    private static Map<String, String> networkMap;
    private static Map<String, String> endpointPropertyMap;

    private final DataBroker dataService;
    private final String topologyId;

    private String networkTag=null;
    // public static final InstanceIdentifier<Resources> ALTO_IID =
    // InstanceIdentifier.builder(Resources.class).toInstance();

    ExecutorService exec = Executors.newFixedThreadPool(CPUS);

    private ListenerRegistration<DataChangeListener> hostNodeListerRegistration;

    private ListenerRegistration<DataChangeListener> networkMapListerRegistration;

    public AltoHostTrackerImpl(DataBroker dataService, String topologyId) {
        networkMap = new HashMap<String, String>();
        endpointPropertyMap = new HashMap<String, String>();

        p = Pattern.compile("[0-9]+.[0-9]+.[0-9]+.[0-9]+");

        Preconditions.checkNotNull(dataService,
                "dataBrokerService should not be null.");
        this.dataService = dataService;
        if (topologyId == null || topologyId.isEmpty()) {
            this.topologyId = TOPOLOGY_NAME;
        } else {
            this.topologyId = topologyId;
        }
    }

    public void submit(final WriteTransaction writeTx) {
        final CheckedFuture writeTxResultFuture = writeTx.submit();
        Futures.addCallback(writeTxResultFuture, new FutureCallback() {
            @Override
            public void onSuccess(Object o) {
                log.debug("ConcurrentHashMap write successful for tx :{}",
                        writeTx.getIdentifier());
            }

            @Override
            public void onFailure(Throwable throwable) {
                log.error("ConcurrentHashMap write transaction {} failed",
                        writeTx.getIdentifier(), throwable.getCause());
            }
        });
    }

    public void writeDefaultCostMaps() {
        ResourceId nm_rid = new ResourceId(new ValidIdString(NMRESOURCEID));
        ResourceId rid = new ResourceId(new ValidIdString(CMRESOURCEID));

        InstanceIdentifier<CostMaps> ALTO_CMS = InstanceIdentifier
                .builder(Resources.class).child(CostMaps.class).build();

        TagString tag = new TagString(TagGenerator.getTag(32));
        ValidIdString vis0 = new ValidIdString("pid0");
        PidName pid0 = new PidName(vis0);
        ValidIdString vis1 = new ValidIdString("pid1");
        PidName pid1 = new PidName(vis1);

        TagString dtag = new TagString(this.networkTag);
        DependentVtags dv = new DependentVtagsBuilder().setResourceId(nm_rid)
                .setTag(dtag).build();
        List<DependentVtags> dvList = new ArrayList<DependentVtags>();
        dvList.add(dv);
        CostType ct = new CostTypeBuilder().setCostMode(CostMode.Numerical)
                .setCostMetric(new CostMetric("hcm"))
                .setDescription("hosttracker cost metric").build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Meta meta = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.MetaBuilder()
                .setDependentVtags(dvList).setCostType(ct).build();

        DstCosts1 dcs11 = new DstCosts1Builder().setCostInHosttracker(10)
                .build();
        DstCosts1 dcs12 = new DstCosts1Builder().setCostInHosttracker(0)
                .build();
        DstCosts dcs1 = new DstCostsBuilder().setDst(pid1)
                .addAugmentation(DstCosts1.class, dcs12).build();
        DstCosts dcs2 = new DstCostsBuilder().setDst(pid0)
                .addAugmentation(DstCosts1.class, dcs11).build();
        List<DstCosts> dcsList = new ArrayList<DstCosts>();
        dcsList.add(dcs1);
        dcsList.add(dcs2);

        org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Map map = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.MapBuilder()
                .setSrc(pid1).setDstCosts(dcsList).build();

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Map> mapList = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Map>();

        mapList.add(map);

        CostMap cm = new CostMapBuilder().setResourceId(rid).setTag(tag)
                .setMeta(meta).setMap(mapList).build();

        List<CostMap> cmList = new ArrayList<CostMap>();

        cmList.add(cm);

        CostMaps cms = new CostMapsBuilder().setCostMap(cmList).build();

        final WriteTransaction writeTx = this.dataService
                .newWriteOnlyTransaction();
        try {
            writeTx.put(LogicalDatastoreType.CONFIGURATION, ALTO_CMS, cms, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        submit(writeTx);
    }

    public void writeDefaultNetworkMaps() {
        InstanceIdentifier<NetworkMaps> ALTO_NM = InstanceIdentifier
                .builder(Resources.class).child(NetworkMaps.class).build();
        final WriteTransaction tx = dataService.newWriteOnlyTransaction();
        try {
            tx.put(LogicalDatastoreType.CONFIGURATION, ALTO_NM,
                    loadNetworkMaps(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        submit(tx);
    }

    public void writeDefaultEndpointpropertyMap() {
        InstanceIdentifier<EndpointPropertyMap> ALTO_EPM = InstanceIdentifier
                .builder(Resources.class).child(EndpointPropertyMap.class)
                .build();
        ResourceId rid = new ResourceId(new ValidIdString(
                "default-endpoint-property-map"));
        TagString tag = new TagString(TagGenerator.getTag(32));

        DependentVtags dv = new DependentVtagsBuilder().setResourceId(rid)
                .setTag(tag).build();
        List<DependentVtags> dvList = new ArrayList<DependentVtags>();
        dvList.add(dv);

        org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.Meta meta = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.endpoint.property.map.MetaBuilder()
                .setDependentVtags(dvList).build();
        TypedIpv4Address ti4 = new TypedIpv4Address("ipv4:0.0.0.0");
        TypedEndpointAddress tea = new TypedEndpointAddress(ti4);
        EndpointPropertyType etp = new EndpointPropertyType(
                new ResourceSpecificEndpointProperty(
                        "default-endpoint-property-map.property"));
        EndpointPropertyValue epv = new EndpointPropertyValue("PID1");

        endpointPropertyMap.put("0.0.0.0", "ipv4");

        Properties ps = new PropertiesBuilder().setPropertyType(etp)
                .setProperty(epv).build();
        List<Properties> psList = new ArrayList<Properties>();
        psList.add(ps);

        EndpointProperties ep = new EndpointPropertiesBuilder()
                .setEndpoint(tea).setProperties(psList).build();
        List<EndpointProperties> epList = new ArrayList<EndpointProperties>();
        epList.add(ep);

        EndpointPropertyMap epm = new EndpointPropertyMapBuilder()
                .setMeta(meta).setEndpointProperties(epList).build();

        final WriteTransaction tx = dataService.newWriteOnlyTransaction();
        try {
            tx.put(LogicalDatastoreType.CONFIGURATION, ALTO_EPM, epm, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        submit(tx);

    }

    private Resources buildResources() {
        try {
            return new ResourcesBuilder().setNetworkMaps(loadNetworkMaps())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private NetworkMaps loadNetworkMaps() throws Exception {
        return new NetworkMapsBuilder().setNetworkMap(loadNetworkMapList())
                .build();
    }

    private List<NetworkMap> loadNetworkMapList() {
        List<NetworkMap> networkMapList = new ArrayList<NetworkMap>();
        ResourceId rid = new ResourceId(new ValidIdString(NMRESOURCEID));
        this.networkTag = TagGenerator.getTag(32);
        TagString tag = new TagString(this.networkTag);
        ValidIdString vis = new ValidIdString("pid0");
        PidName pid = new PidName(vis);
        IpPrefix ep = new IpPrefix(new Ipv4Prefix("0.0.0.0/0"));

        this.networkMap.put("0.0.0.0/0", "pid0");

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map> mapList = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map>();

        List<IpPrefix> epList = new ArrayList<IpPrefix>();
        epList.add(ep);

        EndpointAddressGroup eag = new EndpointAddressGroupBuilder()
                .setAddressType(
                        new EndpointAddressType(
                                EndpointAddressType.Enumeration.Ipv4))
                .setEndpointPrefix(epList).build();

        List<EndpointAddressGroup> eagList = new ArrayList<EndpointAddressGroup>();
        eagList.add(eag);

        org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map map = new MapBuilder()
                .setPid(pid).setEndpointAddressGroup(eagList).build();

        mapList.add(map);

        NetworkMap networkMap = new NetworkMapBuilder().setResourceId(rid)
                .setTag(tag).setMap(mapList).build();

        networkMapList.add(networkMap);

        return networkMapList;
    }

    public void writeTest() {
        InstanceIdentifier<NetworkMaps> ALTO_IID = InstanceIdentifier
                .builder(Resources.class).child(NetworkMaps.class).build();
        final WriteTransaction tx = dataService.newWriteOnlyTransaction();
        try {
            tx.put(LogicalDatastoreType.CONFIGURATION, ALTO_IID,
                    loadNetworkMaps(), true);
        } catch (Exception e) {
        }
        final CheckedFuture writeTxResultFuture = tx.submit();
        Futures.addCallback(writeTxResultFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                log.info("write success.");
                notifyCallback(true);
            }

            @Override
            public void onFailure(final Throwable t) {
                log.error("Failed to initiate resources", t);
                notifyCallback(false);
            }

            void notifyCallback(final boolean result) {
            }
        });
    }

    public void readTest() {
        InstanceIdentifier<Resources> resources = InstanceIdentifier.builder(
                Resources.class).build();
        ListenableFuture<Optional<Resources>> futureResources;
        try (ReadOnlyTransaction readTx = dataService.newReadOnlyTransaction()) {
            futureResources = readTx.read(LogicalDatastoreType.OPERATIONAL,
                    resources);
            readTx.close();
        }
        Optional<Resources> opNodes = null;
        try {
            opNodes = futureResources.get();
        } catch (ExecutionException | InterruptedException ex) {
            log.warn(ex.getLocalizedMessage());
        }
        if (opNodes != null && opNodes.isPresent())
            log.info("resources:" + opNodes.get());
    }

    public void mergeEndpointPropertyMapForAddresses(Addresses addrs) {
        if (addrs == null) {// || addrs.getIp() == null || addrs.getMac() ==
                            // null
            // IpPrefix ep = new IpPrefix(new Ipv4Prefix("1.1.1.1/32"));
            // epList.add(ep);
            return;
        } else {
            String ipAddress = addrs.getIp().toString();
            String mac = addrs.getMac().toString();
            Matcher m = p.matcher(ipAddress);
            if (m.find())
                ipAddress = m.group();
            else
                return;
            if (endpointPropertyMap.containsKey(ipAddress))
                return;

            TypedIpv4Address ti4 = new TypedIpv4Address("ipv4:" + ipAddress);
            TypedEndpointAddress tea = new TypedEndpointAddress(ti4);

            EndpointPropertyType etp1 = new EndpointPropertyType(
                    new ResourceSpecificEndpointProperty(
                            "default-endpoint-property-map.pid"));
            EndpointPropertyValue epv1 = new EndpointPropertyValue("PID1");

            EndpointPropertyType etp2 = new EndpointPropertyType(
                    new ResourceSpecificEndpointProperty("priv:ietf-mac.prop"));
            EndpointPropertyValue epv2 = new EndpointPropertyValue(mac);

            endpointPropertyMap.put(ipAddress, "ipv4");

            Properties ps1 = new PropertiesBuilder().setPropertyType(etp1)
                    .setProperty(epv1).build();
            Properties ps2 = new PropertiesBuilder().setPropertyType(etp2)
                    .setProperty(epv2).build();

            List<Properties> psList = new ArrayList<Properties>();
            psList.add(ps1);
            psList.add(ps2);

            EndpointProperties ep = new EndpointPropertiesBuilder()
                    .setEndpoint(tea).setProperties(psList).build();

            InstanceIdentifier<EndpointProperties> ALTO_EP = InstanceIdentifier
                    .builder(Resources.class)
                    .child(EndpointPropertyMap.class)
                    .child(EndpointProperties.class,
                            new EndpointPropertiesKey(tea)).build();

            final WriteTransaction tx = dataService.newWriteOnlyTransaction();
            if (tx == null)
                return;
            try {
                tx.merge(LogicalDatastoreType.CONFIGURATION, ALTO_EP, ep, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            submit(tx);
        }

    }

    public void mergeNetworkMapForAddressesList(List<Addresses> addrsList,
            String resourceIdString, String pidString, String addressType) {
        ResourceId rid = new ResourceId(new ValidIdString(resourceIdString));
        ValidIdString vis = new ValidIdString(pidString);
        PidName pid = new PidName(vis);

        EndpointAddressType eat;
        if (addressType == "ipv4") {
            eat = new EndpointAddressType(EndpointAddressType.Enumeration.Ipv4);
        } else {
            eat = new EndpointAddressType(EndpointAddressType.Enumeration.Ipv6);
        }

        InstanceIdentifier<EndpointAddressGroup> ALTO_EAG = InstanceIdentifier
                .builder(Resources.class)
                .child(NetworkMaps.class)
                .child(NetworkMap.class, new NetworkMapKey(rid))
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map.class,
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.MapKey(
                                pid))
                .child(EndpointAddressGroup.class,
                        new EndpointAddressGroupKey(eat)).build();

        List<IpPrefix> epList = new ArrayList<IpPrefix>();

        if (addrsList == null) {
            // IpPrefix ep = new IpPrefix(new Ipv4Prefix("1.1.1.1/32"));
            // epList.add(ep);
            return;
        } else {
            for (int i = 0; i < addrsList.size(); i++) {
                Addresses addrs = addrsList.get(i);
                if (addrs.getIp() == null)
                    continue;
                String ipAddress = addrs.getIp().toString();

                Matcher m = p.matcher(ipAddress);
                if (m.find())
                    ipAddress = m.group();
                else
                    continue;
                ipAddress += "/32";
                if (networkMap.containsKey(ipAddress))
                    continue;
                IpPrefix ep = new IpPrefix(new Ipv4Prefix(ipAddress));
                epList.add(ep);
                networkMap.put(ipAddress, pidString);
            }
        }
        if (epList.size() == 0)
            return;

        final WriteTransaction tx = dataService.newWriteOnlyTransaction();

        if (tx == null)
            return;

        EndpointAddressGroup eag = new EndpointAddressGroupBuilder()
                .setAddressType(
                        new EndpointAddressType(
                                EndpointAddressType.Enumeration.Ipv4))
                .setEndpointPrefix(epList).build();

        try {
            tx.merge(LogicalDatastoreType.CONFIGURATION, ALTO_EAG, eag, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        submit(tx);
    }

    public void removeAddressesList(List<Addresses> addrsList,
            String resourceIdString, String pidString, String addressType) {
        if (addrsList == null) {
            return;
        } else {
            for (int i = 0; i < addrsList.size(); i++) {
                Addresses addrs = addrsList.get(i);
                if (addrs.getIp() == null)
                    continue;
                String ipAddress = addrs.getIp().toString();

                Matcher m = p.matcher(ipAddress);
                if (m.find())
                    ipAddress = m.group();
                else
                    continue;

                if (endpointPropertyMap.containsKey(ipAddress))
                    endpointPropertyMap.remove(ipAddress);
                else
                    continue;

                TypedIpv4Address ti4 = new TypedIpv4Address("ipv4:" + ipAddress);
                TypedEndpointAddress tea = new TypedEndpointAddress(ti4);

                InstanceIdentifier<EndpointProperties> ALTO_EP = InstanceIdentifier
                        .builder(Resources.class)
                        .child(EndpointPropertyMap.class)
                        .child(EndpointProperties.class,
                                new EndpointPropertiesKey(tea)).build();

                final WriteTransaction writeTx = this.dataService
                        .newWriteOnlyTransaction();
                writeTx.delete(LogicalDatastoreType.OPERATIONAL, ALTO_EP);
                submit(writeTx);

                ipAddress += "/32";
                if (networkMap.containsKey(ipAddress))
                    networkMap.remove(ipAddress);
                else
                    continue;

            }
        }

        ResourceId rid = new ResourceId(new ValidIdString(resourceIdString));
        ValidIdString vis = new ValidIdString(pidString);
        PidName pid = new PidName(vis);

        EndpointAddressType eat;
        if (addressType == "ipv4") {
            eat = new EndpointAddressType(EndpointAddressType.Enumeration.Ipv4);
        } else {
            eat = new EndpointAddressType(EndpointAddressType.Enumeration.Ipv6);
        }

        InstanceIdentifier<EndpointAddressGroup> ALTO_EAG = InstanceIdentifier
                .builder(Resources.class)
                .child(NetworkMaps.class)
                .child(NetworkMap.class, new NetworkMapKey(rid))
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map.class,
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.MapKey(
                                pid))
                .child(EndpointAddressGroup.class,
                        new EndpointAddressGroupKey(eat)).build();

        List<IpPrefix> epList = new ArrayList<IpPrefix>();

        Iterator iter = networkMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iter
                    .next();
            String ipAddress = entry.getKey();

            IpPrefix ep = new IpPrefix(new Ipv4Prefix(ipAddress));
            epList.add(ep);
        }

        if (epList.size() == 0) {
            InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map> ALTO_MP = InstanceIdentifier
                    .builder(Resources.class)
                    .child(NetworkMaps.class)
                    .child(NetworkMap.class, new NetworkMapKey(rid))
                    .child(org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.Map.class,
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.network.map.MapKey(
                                    pid)).build();

            final WriteTransaction tx = dataService.newWriteOnlyTransaction();
            if (tx == null)
                return;
            try {
                tx.delete(LogicalDatastoreType.CONFIGURATION, ALTO_MP);
            } catch (Exception e) {
                e.printStackTrace();
            }
            submit(tx);
        } else {
            EndpointAddressGroup eag = new EndpointAddressGroupBuilder()
                    .setAddressType(
                            new EndpointAddressType(
                                    EndpointAddressType.Enumeration.Ipv4))
                    .setEndpointPrefix(epList).build();

            final WriteTransaction tx = dataService.newWriteOnlyTransaction();
            if (tx == null)
                return;
            try {
                tx.put(LogicalDatastoreType.CONFIGURATION, ALTO_EAG, eag, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            submit(tx);
        }
    }

    public void registerAsDataChangeListener() {
        ResourceId ridForDelete = new ResourceId(NMRESOURCEID);
        InstanceIdentifier<NetworkMap> networkMapForDelete = InstanceIdentifier
                .builder(Resources.class).child(NetworkMaps.class)
                .child(NetworkMap.class, new NetworkMapKey(ridForDelete))
                .build();

        InstanceIdentifier<HostNode> hostNodes = InstanceIdentifier
                .builder(NetworkTopology.class)//
                .child(Topology.class,
                        new TopologyKey(new TopologyId(topologyId)))//
                .child(Node.class).augmentation(HostNode.class).build();

        InstanceIdentifier<Addresses> addrCapableNodeConnectors = //
        InstanceIdentifier
                .builder(Nodes.class)
                //
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class) //
                .child(NodeConnector.class) //
                .augmentation(AddressCapableNodeConnector.class)//
                .child(Addresses.class).build();

        // ReadOnlyTransaction newReadOnlyTransaction =
        // dataService.newReadOnlyTransaction();
        InstanceIdentifier<Nodes> iins = addrCapableNodeConnectors
                .firstIdentifierOf(Nodes.class);
        // InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node>
        // iin//
        // =
        // addrCapableNodeConnectors.firstIdentifierOf(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class);
        // ListenableFuture<Optional<NodeConnector>> dataFuture =
        // newReadOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, iinc);
        ListenableFuture<Optional<Nodes>> futureNodes;
        /*
         * try { NodeConnector get = dataFuture.get().get();
         * log.info("test "+get); } catch (InterruptedException |
         * ExecutionException ex) {
         * //java.util.logging.Logger.getLogger(HostTracker2Impl
         * .class.getName()).log(Level.SEVERE, null, ex);
         * log.info("exception on get"); }
         */
        try (ReadOnlyTransaction readTx = dataService.newReadOnlyTransaction()) {
            futureNodes = readTx.read(LogicalDatastoreType.OPERATIONAL, iins);
            // futureNode = readTx.read(LogicalDatastoreType.OPERATIONAL, iin);
            readTx.close();
        }
        Optional<Nodes> opNodes = null;
        try {
            opNodes = futureNodes.get();
        } catch (ExecutionException | InterruptedException ex) {
            log.warn(ex.getLocalizedMessage());
        }

        List<Addresses> addrsList = new ArrayList<Addresses>();
        if (opNodes != null && opNodes.isPresent()) {
            // log.info("node connector:"+opNodes.get());
            Nodes ns = opNodes.get();
            for (org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node n : ns
                    .getNode()) {
                List<NodeConnector> connectors = n.getNodeConnector();
                if (connectors != null) {
                    for (NodeConnector nc : connectors) {
                        AddressCapableNodeConnector acnc = (AddressCapableNodeConnector) nc
                                .getAugmentation(AddressCapableNodeConnector.class);
                        if (acnc != null) {
                            for (Addresses addrs : acnc.getAddresses()) {
                                log.info("existing address: " + addrs);
                                addrsList.add(addrs);
                                mergeEndpointPropertyMapForAddresses(addrs);
                            }
                        }
                    }
                }
            }
        }
        mergeNetworkMapForAddressesList(addrsList, NMRESOURCEID, "pid1", "ipv4");
        /*
         * Futures.addCallback(dataFuture, new
         * FutureCallback<Optional<NodeConnector>>() {
         * @Override public void onSuccess(final Optional<NodeConnector> result)
         * { if (result.isPresent()) { log.info("Processing NEW NODE? " +
         * result.get().getId().getValue()); // processHost(result, dataObject,
         * node); } }
         * @Override public void onFailure(Throwable arg0) { } });
         */

        this.hostNodeListerRegistration = dataService
                .registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                        hostNodes, this, DataChangeScope.SUBTREE);

        this.networkMapListerRegistration = dataService
                .registerDataChangeListener(LogicalDatastoreType.CONFIGURATION,
                        networkMapForDelete, this, DataChangeScope.BASE);

        // log.info("register data change");
    }

    @Override
    public void onDataChanged(
            final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        exec.submit(new Runnable() {
            @Override
            public void run() {
                if (change == null) {
                    log.info("In onDataChanged: No processing done as change even is null.");
                    return;
                }
                Map<InstanceIdentifier<?>, DataObject> updatedData = change
                        .getUpdatedData();
                Map<InstanceIdentifier<?>, DataObject> createdData = change
                        .getCreatedData();
                Map<InstanceIdentifier<?>, DataObject> originalData = change
                        .getOriginalData();
                Set<InstanceIdentifier<?>> deletedData = change
                        .getRemovedPaths();

                for (InstanceIdentifier<?> iid : deletedData) {
                    log.info("deletedData");
                    if (iid.getTargetType().equals(Node.class)) {
                        Node node = ((Node) originalData.get(iid));
                        HostNode hostNode = node
                                .getAugmentation(HostNode.class);
                        if (hostNode != null) {
                            List<Addresses> addrsList = hostNode.getAddresses();
                            removeAddressesList(addrsList, NMRESOURCEID,
                                    "pid1", "ipv4");
                        }
                    } else if (iid.getTargetType().equals(NetworkMap.class)) {
                        networkMap.clear();
                        endpointPropertyMap.clear();
                        close();
                        log.info("delete all!");
                    }
                }

                for (Map.Entry<InstanceIdentifier<?>, DataObject> entrySet : updatedData
                        .entrySet()) {
                    InstanceIdentifier<?> iiD = entrySet.getKey();
                    final DataObject dataObject = entrySet.getValue();
                    if (dataObject instanceof Addresses) {
                        Addresses addrs = (Addresses) dataObject;
                        log.info("updatedData addresses:" + addrs);
                        List<Addresses> addrsList = new ArrayList();
                        addrsList.add(addrs);
                        mergeNetworkMapForAddressesList(addrsList,
                                NMRESOURCEID, "pid1", "ipv4");
                        mergeEndpointPropertyMapForAddresses(addrs);

                    } else if (dataObject instanceof Node) {
                        log.info("updatedData node");
                    }
                }

                for (Map.Entry<InstanceIdentifier<?>, DataObject> entrySet : createdData
                        .entrySet()) {
                    InstanceIdentifier<?> iiD = entrySet.getKey();
                    final DataObject dataObject = entrySet.getValue();
                    if (dataObject instanceof Addresses) {
                        Addresses addrs = (Addresses) dataObject;
                        log.info("createdData addresses:" + addrs);
                        List<Addresses> addrsList = new ArrayList();
                        addrsList.add(addrs);
                        mergeNetworkMapForAddressesList(addrsList,
                                NMRESOURCEID, "pid1", "ipv4");
                        mergeEndpointPropertyMapForAddresses(addrs);
                    } else if (dataObject instanceof Node) {
                        log.info("createdData node");
                    }
                }
            }
        });
    }

    public void close() {
        this.hostNodeListerRegistration.close();
    }
}
