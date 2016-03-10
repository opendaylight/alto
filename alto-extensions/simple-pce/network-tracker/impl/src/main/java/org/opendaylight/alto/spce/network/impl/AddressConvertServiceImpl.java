/*
 * Copyright (c) 2015 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.spce.network.impl;

import org.opendaylight.alto.spce.network.api.AddressConvertService;
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

public class AddressConvertServiceImpl implements AddressConvertService, DataChangeListener, AutoCloseable {

    private static final Logger logger = LoggerFactory
            .getLogger(AddressConvertServiceImpl.class);
    private static final int CPUS = Runtime.getRuntime().availableProcessors();
    private ExecutorService exec = Executors.newFixedThreadPool(CPUS);
    private DataBroker dataBroker = null;

    private Map<String, String> macToIp;
    private Map<String, String> ipToMac;

    private ListenerRegistration<DataChangeListener> hostListener = null;

    public AddressConvertServiceImpl(DataBroker dataBroker) {
        this.logger.info("AddressConvertServiceImpl initial.");
        this.dataBroker = dataBroker;
        macToIp = new HashMap<>();
        ipToMac = new HashMap<>();
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

    @Override
    public String getIpByMac(String macAddress) {
        return this.macToIp.get(macAddress);
    }

    @Override
    public String getMacByIp(String ipAddress) {
        return this.ipToMac.get(ipAddress);
    }

    @Override
    public void close() throws Exception {
        this.hostListener.close();
    }

    @Override
    public void onDataChanged(
            final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        exec.submit(new Runnable() {
            @Override
            public void run() {
                if (change == null) {
                    logger.info("In onDataChanged: No processing done as change even is null.");
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
                    logger.info("deletedData");
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
                        logger.info("updatedData addresses:" + addrs + "~~" + origAddr);
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
                        logger.info("createdData addresses:" + addrs);
                        macToIp.put(addrs.getMac().getValue(), addrs.getIp().getIpv4Address().getValue());
                        ipToMac.put(addrs.getIp().getIpv4Address().getValue(), addrs.getMac().getValue());
                    }
                }
            }
        });
    }
}
