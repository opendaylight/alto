package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import java.util.HashMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.alto.host.tracker.impl.DataBroker;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl
 *
 */
public class AltoHostTrackerImplBuilder implements Builder <org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl> {

    private DataBroker _dataBroker;
    private java.lang.String _topologyId;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>> augmentation = new HashMap<>();

    public AltoHostTrackerImplBuilder() {
    }

    public AltoHostTrackerImplBuilder(AltoHostTrackerImpl base) {
        this._dataBroker = base.getDataBroker();
        this._topologyId = base.getTopologyId();
        if (base instanceof AltoHostTrackerImplImpl) {
            AltoHostTrackerImplImpl impl = (AltoHostTrackerImplImpl) base;
            this.augmentation = new HashMap<>(impl.augmentation);
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>) base;
            this.augmentation = new HashMap<>(casted.augmentations());
        }
    }


    public DataBroker getDataBroker() {
        return _dataBroker;
    }
    
    public java.lang.String getTopologyId() {
        return _topologyId;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public AltoHostTrackerImplBuilder setDataBroker(DataBroker value) {
        this._dataBroker = value;
        return this;
    }
    
    public AltoHostTrackerImplBuilder setTopologyId(java.lang.String value) {
        this._topologyId = value;
        return this;
    }
    
    public AltoHostTrackerImplBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public AltoHostTrackerImplBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>> augmentationType) {
        this.augmentation.remove(augmentationType);
        return this;
    }

    public AltoHostTrackerImpl build() {
        return new AltoHostTrackerImplImpl(this);
    }

    private static final class AltoHostTrackerImplImpl implements AltoHostTrackerImpl {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl.class;
        }

        private final DataBroker _dataBroker;
        private final java.lang.String _topologyId;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>> augmentation = new HashMap<>();

        private AltoHostTrackerImplImpl(AltoHostTrackerImplBuilder base) {
            this._dataBroker = base.getDataBroker();
            this._topologyId = base.getTopologyId();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
                case 1:
                    final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>> e = base.augmentation.entrySet().iterator().next();
                    this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>>singletonMap(e.getKey(), e.getValue());
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
        public java.lang.String getTopologyId() {
            return _topologyId;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + ((_topologyId == null) ? 0 : _topologyId.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl other = (org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl)obj;
            if (_dataBroker == null) {
                if (other.getDataBroker() != null) {
                    return false;
                }
            } else if(!_dataBroker.equals(other.getDataBroker())) {
                return false;
            }
            if (_topologyId == null) {
                if (other.getTopologyId() != null) {
                    return false;
                }
            } else if(!_topologyId.equals(other.getTopologyId())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                AltoHostTrackerImplImpl otherImpl = (AltoHostTrackerImplImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("AltoHostTrackerImpl [");
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
            if (_topologyId != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_topologyId=");
                builder.append(_topologyId);
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
