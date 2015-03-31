package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput
 *
 */
public class ClearToastsMadeInputBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput> {

    private InstanceIdentifier<?> _contextInstance;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>> augmentation = new HashMap<>();

    public ClearToastsMadeInputBuilder() {
    }
    public ClearToastsMadeInputBuilder(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.rpc.context.rev130617.RpcContextRef arg) {
        this._contextInstance = arg.getContextInstance();
    }

    public ClearToastsMadeInputBuilder(ClearToastsMadeInput base) {
        this._contextInstance = base.getContextInstance();
        if (base instanceof ClearToastsMadeInputImpl) {
            ClearToastsMadeInputImpl _impl = (ClearToastsMadeInputImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }

    /**
     *Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.rpc.context.rev130617.RpcContextRef</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
    */
    public void fieldsFrom(DataObject arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.rpc.context.rev130617.RpcContextRef) {
            this._contextInstance = ((org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.rpc.context.rev130617.RpcContextRef)arg).getContextInstance();
            isValidArg = true;
        }
        if (!isValidArg) {
            throw new IllegalArgumentException(
              "expected one of: [org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.rpc.context.rev130617.RpcContextRef] \n" +
              "but was: " + arg
            );
        }
    }

    public InstanceIdentifier<?> getContextInstance() {
        return _contextInstance;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public ClearToastsMadeInputBuilder setContextInstance(InstanceIdentifier<?> value) {
        this._contextInstance = value;
        return this;
    }
    
    public ClearToastsMadeInputBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public ClearToastsMadeInputBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>> augmentationType) {
        this.augmentation.remove(augmentationType);
        return this;
    }

    public ClearToastsMadeInput build() {
        return new ClearToastsMadeInputImpl(this);
    }

    private static final class ClearToastsMadeInputImpl implements ClearToastsMadeInput {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput.class;
        }

        private final InstanceIdentifier<?> _contextInstance;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>> augmentation = new HashMap<>();

        private ClearToastsMadeInputImpl(ClearToastsMadeInputBuilder base) {
            this._contextInstance = base.getContextInstance();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>>singletonMap(e.getKey(), e.getValue());
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public InstanceIdentifier<?> getContextInstance() {
            return _contextInstance;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_contextInstance == null) ? 0 : _contextInstance.hashCode());
            result = prime * result + ((augmentation == null) ? 0 : augmentation.hashCode());
            return result;
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DataObject)) {
                return false;
            }
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput other = (org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput)obj;
            if (_contextInstance == null) {
                if (other.getContextInstance() != null) {
                    return false;
                }
            } else if(!_contextInstance.equals(other.getContextInstance())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                ClearToastsMadeInputImpl otherImpl = (ClearToastsMadeInputImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.ClearToastsMadeInput>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("ClearToastsMadeInput [");
            boolean first = true;
        
            if (_contextInstance != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_contextInstance=");
                builder.append(_contextInstance);
             }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

}
