package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.alto.provider.impl.RpcRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.alto.provider.impl.DataBroker;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl
 *
 */
public class AltoProviderImplBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl> {

    private DataBroker _dataBroker;
    private RpcRegistry _rpcRegistry;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>> augmentation = new HashMap<>();

    public AltoProviderImplBuilder() {
    }

    public AltoProviderImplBuilder(AltoProviderImpl base) {
        this._dataBroker = base.getDataBroker();
        this._rpcRegistry = base.getRpcRegistry();
        if (base instanceof AltoProviderImplImpl) {
            AltoProviderImplImpl _impl = (AltoProviderImplImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }


    public DataBroker getDataBroker() {
        return _dataBroker;
    }
    
    public RpcRegistry getRpcRegistry() {
        return _rpcRegistry;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public AltoProviderImplBuilder setDataBroker(DataBroker value) {
        this._dataBroker = value;
        return this;
    }
    
    public AltoProviderImplBuilder setRpcRegistry(RpcRegistry value) {
        this._rpcRegistry = value;
        return this;
    }
    
    public AltoProviderImplBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public AltoProviderImplBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>> augmentationType) {
        this.augmentation.remove(augmentationType);
        return this;
    }

    public AltoProviderImpl build() {
        return new AltoProviderImplImpl(this);
    }

    private static final class AltoProviderImplImpl implements AltoProviderImpl {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl.class;
        }

        private final DataBroker _dataBroker;
        private final RpcRegistry _rpcRegistry;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>> augmentation = new HashMap<>();

        private AltoProviderImplImpl(AltoProviderImplBuilder base) {
            this._dataBroker = base.getDataBroker();
            this._rpcRegistry = base.getRpcRegistry();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>>singletonMap(e.getKey(), e.getValue());
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public DataBroker getDataBroker() {
            return _dataBroker;
        }
        
        @Override
        public RpcRegistry getRpcRegistry() {
            return _rpcRegistry;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_dataBroker == null) ? 0 : _dataBroker.hashCode());
            result = prime * result + ((_rpcRegistry == null) ? 0 : _rpcRegistry.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl other = (org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl)obj;
            if (_dataBroker == null) {
                if (other.getDataBroker() != null) {
                    return false;
                }
            } else if(!_dataBroker.equals(other.getDataBroker())) {
                return false;
            }
            if (_rpcRegistry == null) {
                if (other.getRpcRegistry() != null) {
                    return false;
                }
            } else if(!_rpcRegistry.equals(other.getRpcRegistry())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                AltoProviderImplImpl otherImpl = (AltoProviderImplImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.configuration.AltoProviderImpl>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("AltoProviderImpl [");
            boolean first = true;
        
            if (_dataBroker != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_dataBroker=");
                builder.append(_dataBroker);
             }
            if (_rpcRegistry != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_rpcRegistry=");
                builder.append(_rpcRegistry);
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
