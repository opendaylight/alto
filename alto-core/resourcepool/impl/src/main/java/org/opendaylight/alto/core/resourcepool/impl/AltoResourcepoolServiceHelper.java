/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.resourcepool.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncFunction;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.resource.pool.Resource;

import org.opendaylight.yangtools.yang.common.RpcResult;

class AltoResourcepoolServiceHelper {

    public static interface VerificationCallback
            extends AsyncFunction<String, RpcResult<Void>> {
    }

    public static interface ResourceCallback
            extends AsyncFunction<Optional<Resource>, RpcResult<Void>> {
    }
}
