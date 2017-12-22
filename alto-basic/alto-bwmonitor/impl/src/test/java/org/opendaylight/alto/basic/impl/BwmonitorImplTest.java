/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorQueryInput;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorQueryInputBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorSubscribeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.BwmonitorUnsubscribeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.Speeds;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.SpeedsBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.bwmonitor.rev150105.speeds.PortBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * BwmonitorImpl Tester.
 *
 * @author Jensen Zhang
 * @version 1.0
 * @since <pre>Oct 23, 2017</pre>
 */
public class BwmonitorImplTest {

  private final static InstanceIdentifier<Speeds> SPEEDS_IID = InstanceIdentifier
      .create(Speeds.class);

  private final DataBroker dataBroker1 = mock(DataBroker.class);
  private final DataBroker dataBroker2 = mock(DataBroker.class);
  private BwmonitorImpl service1;
  private BwmonitorImpl service2;

  @Before
  public void before() throws Exception {
    WriteTransaction wx1 = mock(WriteTransaction.class);
    CheckedFuture future1 = mock(CheckedFuture.class);
    when(wx1.submit()).thenReturn(future1);
    when(dataBroker1.newWriteOnlyTransaction()).thenReturn(wx1);

    ReadOnlyTransaction rx1 = mock(ReadOnlyTransaction.class);
    Optional optional1 = mock(Optional.class);
    when(optional1.isPresent()).thenReturn(false);
    when(future1.get()).thenReturn(optional1);
    when(rx1.read(any(), any())).thenReturn(future1);
    when(dataBroker1.newReadOnlyTransaction()).thenReturn(rx1);
    service1 = new BwmonitorImpl(dataBroker1);

    ReadOnlyTransaction rx2 = mock(ReadOnlyTransaction.class);
    CheckedFuture future2 = mock(CheckedFuture.class);
    Optional optional2 = mock(Optional.class);
    Speeds speeds = new SpeedsBuilder()
        .setPort(Arrays.asList(
            new PortBuilder().setPortId("testPort1").build(),
            new PortBuilder().setPortId("testPort2").build()
        ))
        .build();
    when(optional2.get()).thenReturn(speeds);
    when(optional2.isPresent()).thenReturn(true);
    when(future2.get()).thenReturn(optional2);
    when(rx2.read(any(), eq(SPEEDS_IID))).thenReturn(future2);
    when(dataBroker2.newReadOnlyTransaction()).thenReturn(rx2);
    service2 = new BwmonitorImpl(dataBroker2);
  }

  /**
   * Method: bwmonitorSubscribe(BwmonitorSubscribeInput input)
   */
  @Test
  public void testBwmonitorSubscribe() throws Exception {
    service1.bwmonitorSubscribe(new BwmonitorSubscribeInputBuilder()
        .setPortId(Arrays.asList("testPort1"))
        .build());
  }

  /**
   * Method: bwmonitorQuery(BwmonitorQueryInput input)
   */
  @Test
  public void testBwmonitorQuery() throws Exception {
    BwmonitorQueryInput input = new BwmonitorQueryInputBuilder()
        .setPortId(Arrays.asList("testPort1", "testPort3"))
        .build();
    service1.bwmonitorQuery(input);
    service2.bwmonitorQuery(input);
  }

  /**
   * Method: bwmonitorUnsubscribe(BwmonitorUnsubscribeInput input)
   */
  @Test
  public void testBwmonitorUnsubscribe() throws Exception {
    service1.bwmonitorUnsubscribe(new BwmonitorUnsubscribeInputBuilder()
        .setPortId(Arrays.asList("tsetPort1"))
        .build());
  }

  /**
   * Method: close()
   */
  @Test
  public void testClose() throws Exception {
    service1.close();
  }

}
