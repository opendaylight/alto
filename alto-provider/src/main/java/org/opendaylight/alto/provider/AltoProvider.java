/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.provider;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.opendaylight.alto.commons.helper.NetworkMapIpPrefixHelper;
import org.opendaylight.alto.commons.types.model150404.ModelCostMapMeta;
import org.opendaylight.controller.config.yang.config.alto_provider.impl.AltoProviderRuntimeMXBean;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.costdefault.rev150507.DstCosts1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.costdefault.rev150507.DstCosts1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.costdefault.rev150507.DstCosts2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.AltoServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.EndpointCostServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.EndpointCostServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.EndpointCostServiceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.FilteredCostMapServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.FilteredCostMapServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.FilteredNetworkMapServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.FilteredNetworkMapServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.Resources;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.input.CostType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.input.Endpoints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.EndpointCostService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.EndpointCostServiceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.EndpointCostMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.EndpointCostMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.EndpointCostMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.Meta;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.MetaBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.endpoint.cost.map.DstCosts;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.endpoint.cost.map.DstCostsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.CostMaps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.IRD;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.NetworkMaps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.cost.maps.CostMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.cost.maps.CostMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TypedEndpointAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.meta.DefaultAltoNetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.host.AttachmentPoints;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class AltoProvider implements AltoServiceService, DataChangeListener,
        AltoProviderRuntimeMXBean, AutoCloseable {

    private static final Logger log = LoggerFactory
            .getLogger(AltoProvider.class);

    private ListenerRegistration<DataChangeListener> hostNodeListerRegistration;

    private ListenerRegistration<DataChangeListener> linkListerRegistration;

    private ListenerRegistration<DataChangeListener> topologyListerRegistration;
    private ListenerRegistration<DataChangeListener> defaultNetworkMapperRegistration;
    private NetworkMapIpPrefixHelper ipHelper = new NetworkMapIpPrefixHelper();

    private Map<String, String> ipSwitchIdMap = null;
    Graph<NodeId, Link> networkGraph = null;
    Set<String> linkAdded = null;
    DijkstraShortestPath<NodeId, Link> shortestPath = null;
    private AtomicBoolean networkGraphFlag;
    public static final InstanceIdentifier<Resources> ALTO_IID = InstanceIdentifier
            .builder(Resources.class).build();

    private DataBroker dataProvider;
    private final ExecutorService executor;

    private InstanceIdentifier<DefaultAltoNetworkMap> DEFAULT_NETWORK_MAP_IID = InstanceIdentifier
            .builder(Resources.class)
            .child(IRD.class)
            .child(org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.Meta.class)
            .child(DefaultAltoNetworkMap.class).build();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AltoProvider() {
        this.networkGraph = new SparseMultigraph<>();
        this.shortestPath = new DijkstraShortestPath(this.networkGraph);
        this.ipSwitchIdMap = new HashMap<String, String>();
        this.linkAdded = new HashSet<>();
        this.networkGraphFlag = new AtomicBoolean(false);
        this.executor = Executors.newFixedThreadPool(1);
    }

    public void setDataProvider(final DataBroker salDataProvider) {
        this.dataProvider = salDataProvider;
        log.info(this.getClass().getName() + " data provider initiated");
    }

    private NetworkMap readNetworkMap(ResourceId rid)
            throws InterruptedException, ExecutionException {
        NetworkMapKey key = new NetworkMapKey(rid);
        InstanceIdentifier<NetworkMap> iid = InstanceIdentifier
                .builder(Resources.class).child(NetworkMaps.class)
                .child(NetworkMap.class, key).build();
        return readDataFromConfiguration(iid);
    }

    public void registerAsDataChangeListener() {
        this.defaultNetworkMapperRegistration = dataProvider
                .registerDataChangeListener(LogicalDatastoreType.CONFIGURATION,
                        this.DEFAULT_NETWORK_MAP_IID, this,
                        DataChangeScope.BASE);

        InstanceIdentifier<HostNode> hostNodes = InstanceIdentifier
                .builder(NetworkTopology.class)//
                .child(Topology.class,
                        new TopologyKey(new TopologyId("flow:1")))//
                .child(Node.class).augmentation(HostNode.class).build();
        this.hostNodeListerRegistration = dataProvider
                .registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                        hostNodes, this, DataChangeScope.BASE);

        InstanceIdentifier<Link> links = InstanceIdentifier
                .builder(NetworkTopology.class)//
                .child(Topology.class,
                        new TopologyKey(new TopologyId("flow:1")))//
                .child(Link.class).build();
        this.linkListerRegistration = dataProvider.registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL, links, this,
                DataChangeScope.BASE);

        InstanceIdentifier<Topology> topology = InstanceIdentifier
                .builder(NetworkTopology.class)
                //
                .child(Topology.class,
                        new TopologyKey(new TopologyId("flow:1"))).build();
        this.topologyListerRegistration = dataProvider
                .registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                        topology, this, DataChangeScope.BASE);

        ReadOnlyTransaction newReadOnlyTransaction = dataProvider
                .newReadOnlyTransaction();

        ListenableFuture<Optional<Topology>> dataFuture = newReadOnlyTransaction
                .read(LogicalDatastoreType.OPERATIONAL, topology);
        try {
            dataFuture.get().get();
        } catch (InterruptedException | ExecutionException ex) {
            java.util.logging.Logger.getLogger(AltoProvider.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        Futures.addCallback(dataFuture,
                new FutureCallback<Optional<Topology>>() {
                    @Override
                    public void onSuccess(final Optional<Topology> result) {
                        if (result.isPresent()) {
                            log.trace("Processing NEW NODE? " + result.get());
                            processTopology(result.get());
                        }
                    }

                    @Override
                    public void onFailure(Throwable arg0) {
                    }
                });
    }

    public synchronized void addLinks(List<Link> links) {
        if (links == null || links.isEmpty()) {
            log.info("In addLinks: No link added as links is null or empty.");
            return;
        }

        if (this.networkGraph == null) {
            this.networkGraph = new SparseMultigraph<>();
            networkGraphFlag.set(true);
        }

        for (Link link : links) {
            if (linkAlreadyAdded(link)) {
                continue;
            }
            NodeId sourceNodeId = link.getSource().getSourceNode();
            NodeId destinationNodeId = link.getDestination().getDestNode();
            this.networkGraph.addVertex(sourceNodeId);
            this.networkGraph.addVertex(destinationNodeId);
            this.networkGraph.addEdge(link, sourceNodeId, destinationNodeId,
                    EdgeType.UNDIRECTED);
            networkGraphFlag.set(true);
        }

    }

    private boolean linkAlreadyAdded(Link link) {
        String linkAddedKey = null;
        if (link.getDestination().getDestTp().hashCode() > link.getSource()
                .getSourceTp().hashCode()) {
            linkAddedKey = link.getSource().getSourceTp().getValue()
                    + link.getDestination().getDestTp().getValue();
        } else {
            linkAddedKey = link.getDestination().getDestTp().getValue()
                    + link.getSource().getSourceTp().getValue();
        }
        if (linkAdded.contains(linkAddedKey)) {
            return true;
        } else {
            linkAdded.add(linkAddedKey);
            return false;
        }
    }

    public void processTopology(Topology topology) {
        List<Node> nodeList = null;
        if ((nodeList = topology.getNode()) != null) {
            for (int i = 0; i < nodeList.size(); ++i) {
                Node node = nodeList.get(i);
                HostNode hostNode = node.getAugmentation(HostNode.class);
                log.info("process node " + i + hostNode);
                processNode(hostNode);
            }
            List<Link> linkList = topology.getLink();
            addLinks(linkList);
        }
    }

    private void deleteHostNode(HostNode hostNode) {
        String ipv4String = hostNode.getAddresses().get(0).getIp()
                .getIpv4Address().getValue();
        this.ipSwitchIdMap.remove(ipv4String);
    }

    private void processNode(HostNode hostNode) {
        if (this.networkGraph == null) {
            this.networkGraph = new SparseMultigraph<>();
        }
        if (hostNode == null)
            return;
        List<AttachmentPoints> attachmentPoints = hostNode
                .getAttachmentPoints();

        TpId tpId = attachmentPoints.get(0).getTpId();
        String tpIdString = tpId.getValue();

        String ipv4String = hostNode.getAddresses().get(0).getIp()
                .getIpv4Address().getValue();

        this.ipSwitchIdMap.put(ipv4String, tpIdString);
    }


    @Override
    public void onDataChanged(
            final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        log.info("in Data Changed");
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
        Set<InstanceIdentifier<?>> deletedData = change.getRemovedPaths();

        for (InstanceIdentifier<?> iid : deletedData) {
            log.info("delete Data");
            if (iid.getTargetType().equals(HostNode.class)) {
                log.info("delete hostnode");
                HostNode node = ((HostNode) originalData.get(iid));
                deleteHostNode(node);
            } else if (iid.getTargetType().equals(Link.class)) {
                log.info("delete edge");
                String linkAddedKey = null;
                Link link = (Link) originalData.get(iid);
                if (link.getDestination().getDestTp().hashCode() > link
                        .getSource().getSourceTp().hashCode()) {
                    linkAddedKey = link.getSource().getSourceTp().getValue()
                            + link.getDestination().getDestTp().getValue();
                } else {
                    linkAddedKey = link.getDestination().getDestTp().getValue()
                            + link.getSource().getSourceTp().getValue();
                }
                if (linkAdded.contains(linkAddedKey)) {
                    linkAdded.remove(linkAddedKey);
                }
                this.networkGraph.removeEdge((Link) originalData.get(iid));
                networkGraphFlag.set(true);

            } else if (iid.getTargetType().equals(DefaultAltoNetworkMap.class)) {
                ipHelper.clear();
            }
        }

        for (Map.Entry<InstanceIdentifier<?>, DataObject> entrySet : updatedData
                .entrySet()) {
            final DataObject dataObject = entrySet.getValue();
            if (dataObject instanceof HostNode) {
                log.info("update hostnode data");
                processNode((HostNode) dataObject);
            } else if (dataObject instanceof DefaultAltoNetworkMap) {
                try {
                    DefaultAltoNetworkMap defaultMap = (DefaultAltoNetworkMap) dataObject;
                    NetworkMap networkMap = readNetworkMap(defaultMap
                            .getResourceId());
                    ipHelper.update(networkMap);
                } catch (InterruptedException | ExecutionException
                        | UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Map.Entry<InstanceIdentifier<?>, DataObject> entrySet : createdData
                .entrySet()) {
            final DataObject dataObject = entrySet.getValue();
            if (dataObject instanceof HostNode) {
                log.info("update HostNode");
                processNode((HostNode) dataObject);
            } else if (dataObject instanceof Link) {
                log.info("update link");
                Link link = (Link) dataObject;
                if (!linkAlreadyAdded(link)) {
                    NodeId sourceNodeId = link.getSource().getSourceNode();
                    NodeId destinationNodeId = link.getDestination()
                            .getDestNode();
                    this.networkGraph.addVertex(sourceNodeId);
                    this.networkGraph.addVertex(destinationNodeId);
                    this.networkGraph.addEdge(link, sourceNodeId,
                            destinationNodeId, EdgeType.UNDIRECTED);
                    log.info("update link in networkGraph");
                    networkGraphFlag.set(true);
                }
            } else if (dataObject instanceof DefaultAltoNetworkMap) {
                try {
                    DefaultAltoNetworkMap defaultMap = (DefaultAltoNetworkMap) dataObject;
                    NetworkMap networkMap = readNetworkMap(defaultMap
                            .getResourceId());
                    ipHelper.update(networkMap);
                } catch (InterruptedException | ExecutionException
                        | UnknownHostException e) {
                    e.printStackTrace();
                    ipHelper.clear();
                }
            }
        }


    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<EndpointCostMap> hopcountNumerical(
            List<TypedEndpointAddress> srcs, List<TypedEndpointAddress> dsts) {
        if (networkGraphFlag.get()) {
            shortestPath = new DijkstraShortestPath(this.networkGraph);
            networkGraphFlag.set(false);
        }
        List<EndpointCostMap> result = new ArrayList<EndpointCostMap>();
        for (int i = 0; i < srcs.size(); ++i) {
            TypedEndpointAddress teaSrc = srcs.get(i);
            String ipv4SrcString = teaSrc.getTypedIpv4Address().getValue()
                    .substring(5);
            String tpIdSrc = this.ipSwitchIdMap.get(ipv4SrcString);
            String[] tempi = tpIdSrc.split(":");
            String swSrcId = tempi[0] + ":" + tempi[1];
            List<DstCosts> dstCostsList = new ArrayList<DstCosts>();

            for (int j = 0; j < dsts.size(); ++j) {
                TypedEndpointAddress teaDst = dsts.get(j);
                String ipv4DstString = teaDst.getTypedIpv4Address().getValue()
                        .substring(5);
                String tpIdDst = this.ipSwitchIdMap.get(ipv4DstString);
                String[] tempj = tpIdDst.split(":");
                String swDstId = tempj[0] + ":" + tempj[1];

                NodeId srcNodeId = new NodeId(swSrcId);
                NodeId dstNodeId = new NodeId(swDstId);
                Number number = shortestPath.getDistance(srcNodeId, dstNodeId);
                if (number == null) {
                    number = Integer.MAX_VALUE;
                }
                DstCosts1 dst1 = new DstCosts1Builder()
                        .setCostDefault(new Integer(number.intValue()).toString()).build();
                DstCosts dstCost = new DstCostsBuilder()
                        .addAugmentation(DstCosts1.class, dst1).setDst(teaDst)
                        .build();
                dstCostsList.add(dstCost);
            }
            EndpointCostMap ecp = new EndpointCostMapBuilder().setSrc(teaSrc)
                    .setDstCosts(dstCostsList)
                    .setKey(new EndpointCostMapKey(teaSrc)).build();
            result.add(ecp);
        }
        return result;
    }

    @Override
    public Future<RpcResult<EndpointCostServiceOutput>> endpointCostService(
            EndpointCostServiceInput input) {
        RpcResultBuilder<EndpointCostServiceOutput> endpointCostServiceBuilder = null;
        if (input.getCostType() == null) {
            endpointCostServiceBuilder = RpcResultBuilder
                    .<EndpointCostServiceOutput> failed().withError(
                            ErrorType.APPLICATION, "Invalid cost-type value ",
                            "Argument can not be null.");
        }

        if (input.getEndpoints() == null) {
            endpointCostServiceBuilder = RpcResultBuilder
                    .<EndpointCostServiceOutput> failed().withError(
                            ErrorType.APPLICATION, "Invalid endpoints value ",
                            "Argument can not be null.");
        }

        return hosttrackerNumericalHopCountImplementation(input, endpointCostServiceBuilder);
    }

    public ListenableFuture<RpcResult<EndpointCostServiceOutput>> interopECSImplementation(
            EndpointCostServiceInput input,
            RpcResultBuilder<EndpointCostServiceOutput> endpointCostServiceBuilder) {
        if (!ipHelper.initiated()) {
            return Futures.immediateFuture(RpcResultBuilder
                    .<EndpointCostServiceOutput> failed()
                    .withError(ErrorType.APPLICATION, "Default Map Error",
                            "Failed to parse default network map.").build());
        }

        Endpoints endpoints = input.getEndpoints();
        CostMap costMap = getDefaultCostMap(input.getCostType());
        if (costMap == null) {
            return Futures.immediateFuture(RpcResultBuilder
                    .<EndpointCostServiceOutput> failed()
                    .withError(ErrorType.APPLICATION, "Cost Map Error",
                            "Failed to read data from cost map.").build());
        }

        EndpointCostServiceOutput output = null;
        org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.meta.CostType eCostType = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.meta.CostTypeBuilder()
                .setCostMetric(input.getCostType().getCostMetric())
                .setCostMode(input.getCostType().getCostMode()).build();

        Meta meta = new MetaBuilder().setCostType(eCostType).build();
        List<TypedEndpointAddress> srcs = endpoints.getSrcs();
        List<TypedEndpointAddress> dsts = endpoints.getDsts();
        List<EndpointCostMap> ecmList = getEndpointCostListFromCostMap(
                ipHelper, costMap, srcs, dsts);
        EndpointCostService ecs = new EndpointCostServiceBuilder()
                .setMeta(meta).setEndpointCostMap(ecmList).build();

        if ((output = new EndpointCostServiceOutputBuilder()
                .setEndpointCostService(ecs).build()) != null) {
            endpointCostServiceBuilder = RpcResultBuilder.success(output);
        } else {
            endpointCostServiceBuilder = RpcResultBuilder
                    .<EndpointCostServiceOutput> failed().withError(
                            ErrorType.APPLICATION, "Invalid output value",
                            "Output is null.");
        }

        return Futures.immediateFuture(endpointCostServiceBuilder.build());
    }

    private CostMap getDefaultCostMap(CostType costType) {
        CostMap costMap = null;
        try {
            String costMapRid = ModelCostMapMeta.getCostMapResourceId(
                    readDefaultNetworkMapId().getValue(), costType
                            .getCostMetric().getString(), costType
                            .getCostMode().name().toLowerCase());
            InstanceIdentifier<CostMap> costMapNode = costMapIID(new ResourceId(
                    costMapRid));
            costMap = readDataFromConfiguration(costMapNode);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return costMap;
    }

    private ResourceId readDefaultNetworkMapId() throws InterruptedException,
            ExecutionException {
        return readDataFromConfiguration(this.DEFAULT_NETWORK_MAP_IID)
                .getResourceId();
    }

    private InstanceIdentifier<CostMap> costMapIID(ResourceId rid) {
        return InstanceIdentifier.builder(Resources.class)
                .child(CostMaps.class)
                .child(CostMap.class, new CostMapKey(rid)).build();
    }

    public List<EndpointCostMap> getEndpointCostListFromCostMap(
            NetworkMapIpPrefixHelper ipHelper, CostMap costMap,
            List<TypedEndpointAddress> srcs, List<TypedEndpointAddress> dsts) {
        Map<TypedEndpointAddress, PidName> srcPids = ipHelper
                .getPIDsByEndpointAddresses(srcs);
        Map<TypedEndpointAddress, PidName> dstPids = ipHelper
                .getPIDsByEndpointAddresses(dsts);
        Map<PidName, Map<PidName, DstCosts2>> costMaps = costMapListToMap(costMap
                .getMap());

        List<EndpointCostMap> ecmList = new ArrayList<EndpointCostMap>();
        for (TypedEndpointAddress src : srcs) {
            List<DstCosts> dstList = new ArrayList<DstCosts>();
            for (TypedEndpointAddress dst : dsts) {
                PidName srcPid = srcPids.get(src);
                PidName dstPid = dstPids.get(dst);
                String cost = getCostDefault(srcPid, dstPid, costMaps);
                if (cost != null) {
                    DstCosts1 dstCost1 = new DstCosts1Builder()
                            .setCostDefault(cost).build();
                    DstCosts dstCosts = new DstCostsBuilder()
                            .setDst(dst)
                            .setKey(new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.endpoint.cost.map.DstCostsKey(
                                    dst))
                            .addAugmentation(DstCosts1.class, dstCost1).build();
                    dstList.add(dstCosts);
                }
            }
            if (dstList.size() > 0) {
                ecmList.add(new EndpointCostMapBuilder().setDstCosts(dstList)
                    .setKey(new EndpointCostMapKey(src)).setSrc(src).build());
            }
        }
        return ecmList;
    }

    private String getCostDefault(PidName srcPid, PidName dstPid,
            Map<PidName, Map<PidName, DstCosts2>> costMaps) {
        if (srcPid != null && dstPid != null) {
            Map<PidName, DstCosts2> dstMap = costMaps.get(srcPid);
            if (dstMap != null) {
                DstCosts2 dstCost2 = dstMap.get(dstPid);
                if (dstCost2 != null) {
                    return dstCost2.getCostDefault();
                }
            }
        }
        return null;
    }

    private Map<PidName, Map<PidName, DstCosts2>> costMapListToMap(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Map> costMaps) {
        Map<PidName, Map<PidName, DstCosts2>> resultCostMaps = new HashMap<PidName, Map<PidName, DstCosts2>>();
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.Map costMap : costMaps) {
            Map<PidName, DstCosts2> resultCostMap = new HashMap<PidName, DstCosts2>();
            for (org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.map.DstCosts dstCosts : costMap
                    .getDstCosts()) {
                DstCosts2 cost = dstCosts.getAugmentation(DstCosts2.class);
                if (cost != null) {
                    resultCostMap.put(dstCosts.getDst(), cost);
                }
            }
            resultCostMaps.put(costMap.getSrc(), resultCostMap);
        }
        return resultCostMaps;
    }

    @SuppressWarnings("unused")
    private ListenableFuture<RpcResult<EndpointCostServiceOutput>> hosttrackerNumericalHopCountImplementation(
            EndpointCostServiceInput input,
            RpcResultBuilder<EndpointCostServiceOutput> endpointCostServiceBuilder) {
        Endpoints endpoints = input.getEndpoints();
        List<TypedEndpointAddress> srcs = endpoints.getSrcs();
        List<TypedEndpointAddress> dsts = endpoints.getDsts();
        CostType costTypeInput = input.getCostType();
        EndpointCostServiceOutput output = null;

        boolean srcDstFoundFlag = true;
        for (int i = 0; i < srcs.size(); ++i) {
            TypedEndpointAddress teaSrc = srcs.get(i);
            String ipv4SrcString = teaSrc.getTypedIpv4Address().getValue()
                    .substring(5);
            if (this.ipSwitchIdMap.get(ipv4SrcString) == null) {
                endpointCostServiceBuilder = RpcResultBuilder
                        .<EndpointCostServiceOutput> failed()
                        .withError(
                                ErrorType.APPLICATION,
                                "Invalid endpoints value ",
                                "src IP:"
                                        + ipv4SrcString
                                        + " can not be found. Or Topology has not been built.");
                srcDstFoundFlag = false;
                return Futures.immediateFuture(endpointCostServiceBuilder
                        .build());
            }
        }

        for (int j = 0; j < dsts.size(); ++j) {
            TypedEndpointAddress teaDst = dsts.get(j);
            String ipv4DstString = teaDst.getTypedIpv4Address().getValue()
                    .substring(5);
            if (this.ipSwitchIdMap.get(ipv4DstString) == null) {
                endpointCostServiceBuilder = RpcResultBuilder
                        .<EndpointCostServiceOutput> failed()
                        .withError(
                                ErrorType.APPLICATION,
                                "Invalid endpoints value ",
                                "dst IP:"
                                        + ipv4DstString
                                        + " can not be found. Or Topology has not been built.");
                srcDstFoundFlag = false;
                return Futures.immediateFuture(endpointCostServiceBuilder
                        .build());
            }
        }

        CostMetric costMetric = costTypeInput.getCostMetric();
        CostMode costMode = costTypeInput.getCostMode();
        if (srcDstFoundFlag
                && costMode.equals(CostMode.Numerical)
                && (costMetric.getEnumeration() == CostMetric.Enumeration.Hopcount || costMetric
                        .getString().equals("hopcount"))) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.meta.CostType costType = new org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.endpoint.cost.service.output.endpoint.cost.service.meta.CostTypeBuilder()
                    .setCostMetric(costMetric).setCostMode(costMode).build();
            Meta meta = new MetaBuilder().setCostType(costType).build();
            List<EndpointCostMap> ecmList = hopcountNumerical(srcs, dsts);
            EndpointCostService ecs = new EndpointCostServiceBuilder()
                    .setMeta(meta).setEndpointCostMap(ecmList).build();

            if ((output = new EndpointCostServiceOutputBuilder()
                    .setEndpointCostService(ecs).build()) != null) {
                endpointCostServiceBuilder = RpcResultBuilder.success(output);
            } else {
                endpointCostServiceBuilder = RpcResultBuilder
                        .<EndpointCostServiceOutput> failed().withError(
                                ErrorType.APPLICATION, "Invalid output value",
                                "Output is null.");
            }
            return Futures.immediateFuture(endpointCostServiceBuilder.build());
        }

        return Futures.immediateFuture(RpcResultBuilder
                .<EndpointCostServiceOutput> failed()
                .withError(ErrorType.APPLICATION, "Invalid output value",
                        "Output is null.").build());
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
        this.hostNodeListerRegistration.close();
        this.linkListerRegistration.close();
        this.ipSwitchIdMap.clear();
        this.defaultNetworkMapperRegistration.close();
        this.topologyListerRegistration.close();
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

    private <T extends DataObject> T readDataFromConfiguration(
            InstanceIdentifier<T> iid) throws InterruptedException,
            ExecutionException {
        ReadOnlyTransaction tx = this.dataProvider.newReadOnlyTransaction();
        Optional<T> optional = tx.read(LogicalDatastoreType.CONFIGURATION, iid)
                .get();
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
