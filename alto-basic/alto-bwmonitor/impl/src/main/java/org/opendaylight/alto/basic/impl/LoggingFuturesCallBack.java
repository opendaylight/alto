/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import com.google.common.util.concurrent.FutureCallback;
import org.slf4j.Logger;

public class LoggingFuturesCallBack<T> implements FutureCallback<T>{
    private Logger LOG;
    private String message;

    public LoggingFuturesCallBack(String message, Logger LOG){
        this.message = message;
        this.LOG = LOG;
    }

    @Override
    public void onSuccess(T t) {
        LOG.info("Success! {} ", t);
    }

    @Override
    public void onFailure(Throwable e) {
        LOG.warn(message, e);
    }
}
