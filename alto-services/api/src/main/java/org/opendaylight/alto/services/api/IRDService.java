
package org.opendaylight.alto.services.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.IRD;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.data.Resources;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;

public interface IRDService {

    public IRD getIRD();

    public boolean register(ResourceId id, Resources resource);

    public void unregister(ResourceId id);

}
