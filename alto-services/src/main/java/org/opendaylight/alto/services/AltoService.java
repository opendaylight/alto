package org.opendaylight.alto.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.AbstractBrokerAwareActivator;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.IRDBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.NetworkMaps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.CostTypeName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.IRD;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.MediaType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.cost.types.CostTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.cost.types.CostTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.cost.types.CostTypesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.MetaBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.data.Resources;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.data.ResourcesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.data.ResourcesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;

import com.google.common.base.Optional;

public class AltoService extends AbstractBrokerAwareActivator implements BindingAwareConsumer {
  private DataBroker dataBroker;
  
  @Override
  public void onSessionInitialized(ConsumerContext session) {
    this.dataBroker = session.getSALService(DataBroker.class);
  }
  
  public NetworkMap getNetworkMap(ResourceId resourceId) {
    ReadTransaction tx = dataBroker.newReadOnlyTransaction();
    Optional<NetworkMap> networkMap = null;
    try {
      InstanceIdentifier<NetworkMap> npIID = InstanceIdentifier
          .builder(org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.Resources.class)
          .child(NetworkMaps.class)
          .child(NetworkMap.class, new NetworkMapKey(new ResourceId(resourceId)))
          .build();
      networkMap = tx.read(LogicalDatastoreType.OPERATIONAL, npIID).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    
    return networkMap.get();
  }

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
  protected void onBrokerAvailable(BindingAwareBroker broker, BundleContext context) {
    broker.registerConsumer(this);
  }
}
