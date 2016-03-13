/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.impl;

import org.opendaylight.alto.basic.endpointcostservice.suportservice.impl.HostNodeTrackerImpl;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.impl.NetworkElementImpl;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.impl.RoutingServiceImpl;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.HostNodeTrackerService;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.NetworkElementService;
import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.RoutingService;
import org.opendaylight.alto.basic.endpointcostservice.impl.base.BaseECSImplementation;
import org.opendaylight.alto.basic.endpointcostservice.flow.MatchFields;
import org.opendaylight.alto.basic.endpointcostservice.util.LinkNode;
import org.opendaylight.alto.basic.endpointcostservice.helper.UtilHelper;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.CostMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.CostTypeData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.TypedAddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.endpointcostmap.response.data.endpoint.cost.map.endpoint.cost.Cost;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.cost.Numerical;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.output.response.endpointcost.response.endpointcost.data.endpoint.costmap.data.endpoint.cost.map.endpoint.cost.cost.NumericalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BasicECSImplementation extends BaseECSImplementation{
    private static final Logger log = LoggerFactory.getLogger(BasicECSImplementation.class);
    private RoutingService routingService;
    private NetworkElementService hostNetworkService;
    private HostNodeTrackerService hostNodeTrackerService;

    public BasicECSImplementation(DataBroker dataBroker){
        this.dataBroker = dataBroker;
        this.hostNodeTrackerService= new HostNodeTrackerImpl(dataBroker);
        this.hostNetworkService = new NetworkElementImpl(dataBroker);
        this.routingService = new RoutingServiceImpl(hostNetworkService);
    }

    @Override
    protected Cost computeCost(TypedAddressData src, TypedAddressData dst, CostTypeData costType) {
        NumericalBuilder numericalBuilder = new NumericalBuilder();
        if (validSourceAndDest(src, dst)) {
                if (isAddressEqual(src,dst)) {
                return numericalBuilder.setCost(new BigDecimal(0)).build();
            }
            MatchFields matchFields = createMatchField(src,dst);
            LinkNode head = routingService.buildRoutePath(matchFields);
            if (head != null) {
                return computeCostBasedOnCostType(head, costType);
            }
        }
        return null;
    }

    private Cost computeCostBasedOnCostType(LinkNode head, CostTypeData costType) {
        CostMetric costMetric = costType.getCostMetric();
        if (hasLoop(head, new HashMap<LinkNode, Integer>())) {
            return null;
        }
        if(costMetric.getValue().toString().equals(new String("hopcount"))){
            log.info("hopcpunt");
            Numerical cost = computeHopCountECS(head);
            return (cost == null) ? null : cost;
        }else if(costMetric.getValue().toString().equals(new String("routingcost"))){
            log.info("routingcost");
//            Numerical cost = computeRoutingcostECS(head);
//            return (cost == null) ? null : cost;
            return null;
        }else if(costMetric.getValue().toString().equals(new String("bandwidth"))){
             log.info("bandwidth");
            Numerical cost = computeBandwidthECS(head);
            return (cost == null) ? null : cost;
        }
        return null;
    }

    private Numerical computeBandwidthECS(LinkNode head) {
        //// TODO: 16-3-3
        Long result = null;
        LinkedList<LinkNode> queue = new LinkedList<>();
        Map<LinkNode, Long> maxBw = new HashMap<LinkNode, Long>();
        queue.addLast(head);
        maxBw.put(head, head.availableBandwidth());
        while (!queue.isEmpty()) {
            LinkNode now = queue.pop();
            Long topBw = maxBw.get(now)==null?0L:maxBw.get(now);
            if (now.isDestHost()) {
                result = maxBw.get(now);
            }
            for (LinkNode child : now.children()) {
                Long bw = (child.availableBandwidth() > topBw) ? child.availableBandwidth() : topBw;
                if (maxBw.containsKey(child)) {
                    Long currentBw = maxBw.get(child);
                    if (bw > currentBw) {
                        maxBw.put(child, bw);
                        queue.addLast(child);
                    }
                } else {
                    maxBw.put(child, bw);
                    queue.addLast(child);
                }
            }
        }
        NumericalBuilder re = new NumericalBuilder();
        return (result == null) ? null : re.setCost(new BigDecimal(-result/500)).build();
    }

    private boolean hasLoop(LinkNode head, Map<LinkNode, Integer> status) {
        if (status.containsKey(head)) {
            return status.get(head) == -1;
        } else {
            status.put(head, -1);
            for (LinkNode eachNode : head.children()) {
                if (hasLoop(eachNode, status)) {
                    return true;
                }
            }
            status.put(head, 1);
            return false;
        }
    }

    private Numerical computeHopCountECS(LinkNode head) {
        List<LinkNode> path = computeShortestLinkNodePath(head);
        if (path == null)
            return null;
        else {
            NumericalBuilder result = new NumericalBuilder();
            BigDecimal cost = new BigDecimal(path.size());
            return result.setCost(cost).build();
        }
    }
    private List<LinkNode> computeShortestLinkNodePath(LinkNode head) {
        LinkedList<LinkNode> queue = new LinkedList<LinkNode>();
        HashMap<LinkNode, Integer> hopCounts = new HashMap<LinkNode, Integer>();
        HashMap<LinkNode, LinkNode> prior = new HashMap<LinkNode, LinkNode>();
        hopCounts.put(head, 0);
        queue.addLast(head);
        int maxIter = 500;
        while (!queue.isEmpty()) {
            if (maxIter <= 0) {
                break;
            }
            --maxIter;
            LinkNode now = queue.pop();
            if (now.isDestHost()) {
                LinkedList<LinkNode> res = new LinkedList<LinkNode>();
                while (!now.equals(head)) {
                    res.addFirst(now);
                    now = prior.get(now);
                }
                res.addFirst(head);
                return res;
            } else {
                int hopCountNow = hopCounts.get(now) + 1;
                for (LinkNode eachNode : now.children()) {
                    if (addHopCountToMap(hopCounts, eachNode, hopCountNow)) {
                        prior.put(eachNode, now);
                        queue.addLast(eachNode);
                    }
                }
            }
        }
        return null;
    }

    private Boolean addHopCountToMap(HashMap<LinkNode, Integer> map, LinkNode key, int value) {
        if (map.containsKey(key)) {
            int tmp = map.get(key);
            map.put(key, tmp < value ? tmp : value);
            if (tmp < value) {
                return false;
            }
        } else {
            map.put(key, value);
        }
        return true;
    }

    private MatchFields createMatchField(TypedAddressData src, TypedAddressData dst) {
        MacAddress srcMac = new MacAddress(hostNodeTrackerService.getMacByIp(UtilHelper.getIpFromTypedAddressData(src)));
        MacAddress dstMac = new MacAddress(hostNodeTrackerService.getMacByIp(UtilHelper.getIpFromTypedAddressData(dst)));
        return new MatchFields(srcMac,dstMac,src,dst);
    }

    private boolean isAddressEqual(TypedAddressData src, TypedAddressData dst) {
        return UtilHelper.getIpFromTypedAddressData(src).equals(UtilHelper.getIpFromTypedAddressData(dst));
    }

    private boolean validSourceAndDest(TypedAddressData src, TypedAddressData dst) {
        return hostNodeTrackerService.isValidHost(UtilHelper.getIpFromTypedAddressData(src)) && hostNodeTrackerService.isValidHost(UtilHelper.getIpFromTypedAddressData(dst));
    }

}
