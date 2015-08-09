/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.helper;

public class TypeWrapper<T extends TypeWrapper, E> {

    private E data = null;

    public static <T extends TypeWrapper, E> T wrap(T wrapper, E data) {
        wrapper.set(data);
        return wrapper;
    }

    public TypeWrapper() {
        data = null;
    }

    public TypeWrapper(E data) {
        this.data = data;
    }

    public TypeWrapper(TypeWrapper<T, E> wrapper) {
        this.set(wrapper.get());
    }

    public E get() {
        return this.data;
    }

    public void set(E newData) {
        this.data = newData;
    }
}
