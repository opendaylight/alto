/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoggingFuturesCallBack Tester.
 *
 * @author Jensen Zhang
 * @version 1.0
 * @since <pre>Oct 24, 2017</pre>
 */
public class LoggingFuturesCallBackTest {

  private final static Logger LOG = LoggerFactory.getLogger(LoggingFuturesCallBackTest.class);
  private LoggingFuturesCallBack callBack;

  @Before
  public void before() throws Exception {
    callBack = new LoggingFuturesCallBack("test", LOG);
  }

  /**
   * Method: onSuccess(T t)
   */
  @Test
  public void testOnSuccess() throws Exception {
    callBack.onSuccess("test");
  }

  /**
   * Method: onFailure(Throwable e)
   */
  @Test
  public void testOnFailure() throws Exception {
    callBack.onFailure(new Exception());
  }

}
