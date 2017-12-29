/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.supportservice.impl;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.opendaylight.alto.basic.endpointcostservice.supportservice.service.HostNodeTrackerService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.address.tracker.rev140617.address.node.connector.Addresses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostNodeTrackerImpl implements HostNodeTrackerService, AutoCloseable {

    private static final Logger log = LoggerFactory
            .getLogger(HostNodeTrackerImpl.class);
    private static final int CPUS = Runtime.getRuntime().availableProcessors();
    private final ExecutorService exec = Executors.newFixedThreadPool(CPUS);
    private DataBroker dataBroker = null;
    private final Map<String, String> macToIp = new ConcurrentHashMap<>();
    private final Map<String, String> ipToMac = new ConcurrentHashMap<>();

    private ListenerRegistration<?> hostListener = null;
    private ListenerRegistration<?> addressesListener = null;

    public HostNodeTrackerImpl  (DataBroker dataBroker) {
        this.log.info("AddressConvertServiceImpl initial.");
        this.dataBroker = dataBroker;
        registerPortListener();
    }

    private void registerPortListener() {
        InstanceIdentifier<HostNode> hostNodes = InstanceIdentifier
                .builder(NetworkTopology.class)
                .child(Topology.class)
                .child(Node.class).augmentation(HostNode.class).build();
        this.hostListener = this.dataBroker
                .registerDataTreeChangeListener(new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL,
                        hostNodes), changes -> onHostChanged(changes));

        InstanceIdentifier<Addresses> addresses = InstanceIdentifier
                .builder(NetworkTopology.class)
                .child(Topology.class)
                .child(Node.class).augmentation(HostNode.class).child(Addresses.class).build();
        this.addressesListener = this.dataBroker
                .registerDataTreeChangeListener(new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL,
                        addresses), changes -> onAddressesChanged(changes));
    }

    @Override
    public void close() {
        this.hostListener.close();
        this.addressesListener.close();
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
    public Boolean isValidHost(String ipAddress) {
        if(ipAddress != null) {
            return this.ipToMac.containsKey(ipAddress);
        }
        else {
            return false;
        }
    }

    private void onAddressesChanged(Collection<DataTreeModification<Addresses>> changes) {
        exec.submit(() -> {
            for (DataTreeModification<Addresses> change: changes) {
                final DataObjectModification<Addresses> rootNode = change.getRootNode();
                switch (rootNode.getModificationType()) {
                    case WRITE:
                    case SUBTREE_MODIFIED:
                        Addresses origAddresses = rootNode.getDataBefore();
                        Addresses newAddresses = rootNode.getDataAfter();
                        if (origAddresses == null) {
                            log.info("createdData addresses:" + newAddresses);
                            macToIp.put(newAddresses.getMac().getValue(),
                                    newAddresses.getIp().getIpv4Address().getValue());
                            ipToMac.put(newAddresses.getIp().getIpv4Address().getValue(),
                                    newAddresses.getMac().getValue());
                        } else {
                            log.info("updatedData addresses:" + newAddresses + "~~" + origAddresses);
                            macToIp.remove(origAddresses.getMac().getValue());
                            macToIp.put(newAddresses.getMac().getValue(),
                                    newAddresses.getIp().getIpv4Address().getValue());
                            ipToMac.remove(origAddresses.getIp().getIpv4Address().getValue());
                            ipToMac.put(newAddresses.getIp().getIpv4Address().getValue(),
                                    newAddresses.getMac().getValue());
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void onHostChanged(Collection<DataTreeModification<HostNode>> changes) {
        exec.submit(() -> {
            for (DataTreeModification<HostNode> change: changes) {
                final DataObjectModification<HostNode> rootNode = change.getRootNode();
                switch (rootNode.getModificationType()) {
                    case DELETE:
                        log.info("deletedData");
                        HostNode deletedHostNode = rootNode.getDataBefore();
                        List<Addresses> addrList = deletedHostNode.getAddresses();
                        for (Addresses eachAddress : addrList) {
                            macToIp.remove(eachAddress.getMac().getValue());
                            ipToMac.remove(eachAddress.getIp().getIpv4Address().getValue());
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
