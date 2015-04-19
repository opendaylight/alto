package org.opendaylight.alto.commons.helper;

public abstract class Converter<I, O> {
    private I _in = null;
    private O _out = null;

    public Converter() {
    }

    public Converter(I in) {
        this._in = in;
        this._out = convert();
    }

    public O convert() {
        return (O)_convert();
    }

    protected abstract Object _convert();

    public O convert(I in) {
        return reset(in).out();
    }

    public Converter<I, O> reset(I in) {
        this._in = in;
        this._out = convert();
        return this;
    }

    public I in() {
        return _in;
    }

    public O out() {
        return _out;
    }
}
