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
