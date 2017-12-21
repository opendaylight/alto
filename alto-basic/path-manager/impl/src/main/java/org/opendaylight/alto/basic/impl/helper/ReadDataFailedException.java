/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadDataFailedException extends Exception {

    private static final Logger LOG = LoggerFactory.getLogger(ReadDataFailedException.class);
    private static final long serialVersionUID = 1L;

    public ReadDataFailedException(String message) {
        super(message);
    }
}
