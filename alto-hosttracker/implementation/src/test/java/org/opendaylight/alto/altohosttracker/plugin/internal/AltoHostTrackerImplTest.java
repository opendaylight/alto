/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.altohosttracker.plugin.internal;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
//import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.address.tracker.rev140617.address.node.connector.Addresses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.address.tracker.rev140617.address.node.connector.AddressesBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

public class AltoHostTrackerImplTest {

  private AltoHostTrackerImpl altoHostTrackerImpl;
  private Addresses addrs;
  private DataBroker dataBroker;
  private IpAddress ipAddress;
  private MacAddress mac;

  @Before
  public void init(){
    dataBroker = mock(DataBroker.class);
    altoHostTrackerImpl = new AltoHostTrackerImpl(dataBroker,"flow:1");
  }

  @Test
  public void onMergeEndpointPropertyMapTest() throws Exception {
    ipAddress = new IpAddress(new Ipv4Address("10.0.0.1"));
    mac = new MacAddress("00:00:00:00:00:01");
    addrs = new AddressesBuilder().setIp(ipAddress).setMac(mac).build();
    altoHostTrackerImpl.mergeEndpointPropertyMapForAddresses(addrs);
    verify(dataBroker).newWriteOnlyTransaction();
  }

  @Test
  public void onMergeNetworkMapTest() throws Exception {
    ipAddress = new IpAddress(new Ipv4Address("10.0.0.1"));
    mac = new MacAddress("00:00:00:00:00:01");
    addrs = new AddressesBuilder().setIp(ipAddress).setMac(mac).build();
    List<Addresses> addrsList = new ArrayList<Addresses>();
    addrsList.add(addrs);
    altoHostTrackerImpl.mergeNetworkMapForAddressesList(addrsList, "default-network-map", "pid1", "ipv4");
    verify(dataBroker).newWriteOnlyTransaction();
  }

  @Test
  public void onRemoveAddressesTest() throws Exception {
    ipAddress = new IpAddress(new Ipv4Address("10.0.0.1"));
    mac = new MacAddress("00:00:00:00:00:01");
    addrs = new AddressesBuilder().setIp(ipAddress).setMac(mac).build();
    List<Addresses> addrsList = new ArrayList<Addresses>();
    addrsList.add(addrs);
    altoHostTrackerImpl.removeAddressesList(addrsList, "default-network-map", "pid1", "ipv4");
    verify(dataBroker).newWriteOnlyTransaction();
  }

}
