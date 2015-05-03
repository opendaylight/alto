package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150416;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.concepts.Builder;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150416.DstCosts1} instances.
 *
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150416.DstCosts1
 *
 */
public class DstCosts1Builder implements Builder <org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150416.DstCosts1> {

    private java.lang.Integer _costInHosttracker;


    public DstCosts1Builder() {
    }

    public DstCosts1Builder(DstCosts1 base) {
        this._costInHosttracker = base.getCostInHosttracker();
    }


    public java.lang.Integer getCostInHosttracker() {
        return _costInHosttracker;
    }

    public DstCosts1Builder setCostInHosttracker(java.lang.Integer value) {
        this._costInHosttracker = value;
        return this;
    }

    public DstCosts1 build() {
        return new DstCosts1Impl(this);
    }

    private static final class DstCosts1Impl implements DstCosts1 {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150416.DstCosts1> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150416.DstCosts1.class;
        }

        private final java.lang.Integer _costInHosttracker;


        private DstCosts1Impl(DstCosts1Builder base) {
            this._costInHosttracker = base.getCostInHosttracker();
        }

        @Override
        public java.lang.Integer getCostInHosttracker() {
            return _costInHosttracker;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_costInHosttracker == null) ? 0 : _costInHosttracker.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150416.DstCosts1.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150416.DstCosts1 other = (org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150416.DstCosts1)obj;
            if (_costInHosttracker == null) {
                if (other.getCostInHosttracker() != null) {
                    return false;
                }
            } else if(!_costInHosttracker.equals(other.getCostInHosttracker())) {
                return false;
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("DstCosts1 [");
            boolean first = true;
        
            if (_costInHosttracker != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_costInHosttracker=");
                builder.append(_costInHosttracker);
             }
            return builder.append(']').toString();
        }
    }

}
