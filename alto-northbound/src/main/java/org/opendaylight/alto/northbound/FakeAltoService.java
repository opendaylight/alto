package org.opendaylight.alto.northbound;

import org.opendaylight.alto.services.api.IRDService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.IRD;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.IRDBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostTypeName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.MediaType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.cost.types.CostTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.cost.types.CostTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.cost.types.CostTypesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.MetaBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.data.Resources;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.data.ResourcesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.data.ResourcesKey;

import java.util.ArrayList;
import java.util.List;

class FakeAltoService implements IRDService {
    @Override
    public IRD getIRD() {
        IRDBuilder irdb = new IRDBuilder();
        MetaBuilder mb = new MetaBuilder();

        List<CostTypes> cost_types = new ArrayList<CostTypes>();
        CostTypesBuilder cb = new CostTypesBuilder();
        CostTypeName name = new CostTypeName("num-routing");
        CostMetric metric = new CostMetric(CostMetric.Enumeration.Routingcost);
        CostMode mode = CostMode.Numerical;
        CostTypesKey ckey = new CostTypesKey(name);

        cost_types.add(cb.setCostTypeName(name).setCostMetric(metric).setCostMode(mode).setKey(ckey).build());
        irdb.setMeta(mb.setCostTypes(cost_types).build());

        List<Resources> resources = new ArrayList<Resources>();
        ResourcesBuilder rb = new ResourcesBuilder();

        MediaType mt = new MediaType(MediaType.Enumeration.AltoNetworkmap);
        Uri uri = new Uri("http://localhost:8080/controller/nb/v2/networkmap/default");
        ResourcesKey rkey = new ResourcesKey(new ResourceId("my-default-networkmap"));

        resources.add(rb.setMediaType(mt).setUri(uri).setKey(rkey).build());
        irdb.setResources(resources);

        return irdb.build();
    }

    @Override
    public boolean register(ResourceId id, Resources resource) {
        return true;
    }

    @Override
    public void unregister(ResourceId id) {
    }
}
