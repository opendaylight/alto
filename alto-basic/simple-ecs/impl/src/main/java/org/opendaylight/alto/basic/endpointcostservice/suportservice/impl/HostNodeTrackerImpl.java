/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.suportservice.impl;


import org.opendaylight.alto.basic.endpointcostservice.suportservice.service.HostNodeTrackerService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.address.tracker.rev140617.address.node.connector.Addresses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HostNodeTrackerImpl implements HostNodeTrackerService,DataChangeListener {

    private static final Logger log = LoggerFactory
            .getLogger(HostNodeTrackerImpl.class);
    private static final int CPUS = Runtime.getRuntime().availableProcessors();
    private ExecutorService exec = Executors.newFixedThreadPool(CPUS);
    private DataBroker dataBroker = null;
    private Map<String, String> macToIp;
    private Map<String, String> ipToMac;

    private ListenerRegistration<DataChangeListener> hostListener = null;
    public HostNodeTrackerImpl  (DataBroker dataBroker) {
        this.log.info("AddressConvertServiceImpl initial.");
        this.dataBroker = dataBroker;
        macToIp = new HashMap<String,String>();
        ipToMac = new HashMap<String,String>();
        registerPortListener();
    }

    private void registerPortListener() {
        InstanceIdentifier<HostNode> hostNodes = InstanceIdentifier
                .builder(NetworkTopology.class)
                .child(Topology.class)
                .child(Node.class).augmentation(HostNode.class).build();
        this.hostListener = this.dataBroker
                .registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                        hostNodes, this, AsyncDataBroker.DataChangeScope.SUBTREE);
    }
    public String getIpByMac(String macAddress) {
        return this.macToIp.get(macAddress);
    }

    public String getMacByIp(String ipAddress) {
        return this.ipToMac.get(ipAddress);
    }

    public Boolean isValidHost(String ipAddress) {
        if(ipAddress != null) {
            return this.ipToMac.containsKey(ipAddress);
        }
        else {
            return false;
        }
    }

    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
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
                            List<Addresses> addrList = hostNode.getAddresses();
                            for (Addresses eachAddress : addrList) {
                                macToIp.remove(eachAddress.getMac().getValue());
                                ipToMac.remove(eachAddress.getIp().getIpv4Address().getValue());
                            }
                        }
                    }
                }

                for (Map.Entry<InstanceIdentifier<?>, DataObject> entrySet : updatedData
                        .entrySet()) {
                    InstanceIdentifier<?> iiD = entrySet.getKey();
                    final DataObject dataObject = entrySet.getValue();
                    if (dataObject instanceof Addresses) {
                        Addresses addrs = (Addresses) dataObject;
                        Addresses origAddr = (Addresses) originalData.get(iiD);
                        log.info("updatedData addresses:" + addrs + "~~" + origAddr);
                        macToIp.remove(origAddr.getMac().getValue());
                        macToIp.put(addrs.getMac().getValue(), addrs.getIp().getIpv4Address().getValue());
                        ipToMac.remove(origAddr.getIp().getIpv4Address().getValue());
                        ipToMac.put(addrs.getIp().getIpv4Address().getValue(), addrs.getMac().getValue());
                    }
                }

                for (Map.Entry<InstanceIdentifier<?>, DataObject> entrySet : createdData
                        .entrySet()) {
                    InstanceIdentifier<?> iiD = entrySet.getKey();
                    final DataObject dataObject = entrySet.getValue();
                    if (dataObject instanceof Addresses) {
                        Addresses addrs = (Addresses) dataObject;
                        log.info("createdData addresses:" + addrs);
                        macToIp.put(addrs.getMac().getValue(), addrs.getIp().getIpv4Address().getValue());
                        ipToMac.put(addrs.getIp().getIpv4Address().getValue(), addrs.getMac().getValue());
                    }
                }
            }
        });
    }
}
