/*
 * Copyright (c) 2015 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.spce.network.util;

public class ReadDataFailedException extends Exception {
    private static final long serialVersionUID = 1L;

    public void printMessage(String message) {
        System.out.println(message);
    }
}
