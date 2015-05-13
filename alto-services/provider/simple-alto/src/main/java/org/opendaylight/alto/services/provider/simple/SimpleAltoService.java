package org.opendaylight.alto.services.provider.simple;

import org.opendaylight.alto.services.api.rfc7285.NetworkMapService;
import org.opendaylight.alto.services.api.rfc7285.CostMapService;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostType;

import org.opendaylight.alto.commons.types.converter.YANGJSON2RFCNetworkMapConverter;
import org.opendaylight.alto.commons.types.converter.YANGJSON2RFCCostMapConverter;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.Resources;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.IRD;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.NetworkMaps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.CostMaps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.cost.maps.CostMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.cost.maps.CostMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.meta.DefaultAltoNetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.Meta;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.osgi.framework.ServiceRegistration;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SimpleAltoService implements NetworkMapService, CostMapService, AutoCloseable {

    private final Logger m_logger = LoggerFactory.getLogger(SimpleAltoService.class);
    private DataBroker m_db = null;
    private ObjectMapper m_mapper = new ObjectMapper();
    private List<ServiceRegistration> m_reg = new LinkedList<ServiceRegistration>();
    private YANGJSON2RFCNetworkMapConverter m_nmconverter = null;
    private YANGJSON2RFCCostMapConverter m_cmconverter = null;

    public SimpleAltoService(DataBroker db) {
        this.m_db = db;
        this.m_nmconverter = new YANGJSON2RFCNetworkMapConverter();
        this.m_cmconverter = new YANGJSON2RFCCostMapConverter();

        this.register(NetworkMapService.class);
        this.register(CostMapService.class);
    }

    protected <E> void register(Class<E> clazz) {
        ServiceRegistration reg = ServiceHelper.registerGlobalServiceWReg(clazz, this, null);
        if (reg != null)
            this.m_reg.add(reg);

        assert ServiceHelper.getGlobalInstance(clazz, this) != this;
    }

    @Override
    public void close() {
        for (ServiceRegistration reg: this.m_reg) {
            reg.unregister();
        }
        this.m_reg.clear();
    }

    @Override
    public RFC7285NetworkMap getDefaultNetworkMap() {
        //TODO
        return null;
    }

    @Override
    public RFC7285NetworkMap getNetworkMap(String id) {
        m_logger.info("Handling resource-id: {}", id);
        InstanceIdentifier<NetworkMap> niid = getNetworkMapIID(id);
        m_logger.info("IID: {}", niid);

        try {
            ReadOnlyTransaction tx = m_db.newReadOnlyTransaction();
            ListenableFuture<Optional<NetworkMap>> result
                        = tx.read(LogicalDatastoreType.CONFIGURATION, niid);
            if (result.get().isPresent()) {
                NetworkMap nm = result.get().get();
                ObjectNode node = m_mapper.valueToTree(nm);
                m_logger.info(m_mapper.writeValueAsString(nm));

                RFC7285NetworkMap ret = m_nmconverter.convert(node);
                return ret;
            } else {
                m_logger.info("Failed to read with niid: {}", niid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public RFC7285NetworkMap getNetworkMap(RFC7285VersionTag vtag) {
        RFC7285NetworkMap nm = getNetworkMap(vtag.rid);

        if ((nm != null) && (vtag.equals(nm.meta.vtag))) {
            return nm;
        }
        return null;
    }

    @Override
    public RFC7285NetworkMap getNetworkMap(String id, RFC7285NetworkMap.Filter filter) {
        RFC7285NetworkMap nm = getNetworkMap(id);

        if (nm == null)
            return null;

        LinkedHashMap<String, RFC7285Endpoint.AddressGroup> map
                    = new LinkedHashMap<String, RFC7285Endpoint.AddressGroup>();
        for (String pid: filter.pids) {
            if (nm.map.containsKey(pid))
                map.put(pid, nm.map.get(pid));
        }
        nm.map = map;
        return nm;
    }

    @Override
    public RFC7285NetworkMap getNetworkMap(RFC7285VersionTag vtag, RFC7285NetworkMap.Filter filter) {
        RFC7285NetworkMap nm = getNetworkMap(vtag.rid, filter);
        if ((nm != null) && (vtag.equals(nm.meta.vtag))) {
            return nm;
        }
        return null;
    }

    @Override
    public Boolean validateNetworkMapFilter(String id, RFC7285NetworkMap.Filter filter) {
        if ((filter != null) && (filter.pids != null))
            return true;
        return false;
    }

    @Override
    public Boolean validateNetworkMapFilter(RFC7285VersionTag vtag, RFC7285NetworkMap.Filter filter) {
        return validateNetworkMapFilter(vtag.rid, filter);
    }

    @Override
	public RFC7285CostMap getCostMap(String id) {
        m_logger.info("Handling cost-map resource: {}", id);
        InstanceIdentifier<CostMap> ciid = getCostMapIID(id);
        m_logger.info("CostMap IID: {}", ciid);

        try {
            ReadOnlyTransaction tx = m_db.newReadOnlyTransaction();
            ListenableFuture<Optional<CostMap>> result
                        = tx.read(LogicalDatastoreType.CONFIGURATION, ciid);
            if (result.get().isPresent()) {
                CostMap cm = result.get().get();
                m_logger.info(m_mapper.writeValueAsString(cm));
                ObjectNode node = m_mapper.valueToTree(cm);

                RFC7285CostMap ret = m_cmconverter.convert(node);
                return ret;
            } else {
                m_logger.info("Failed to read with ciid: {}", ciid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
	public RFC7285CostMap getCostMap(RFC7285VersionTag vtag) {
        //TODO
        return null;
    }

    @Override
	public RFC7285CostMap getCostMap(String id, RFC7285CostType type) {
        RFC7285CostMap cm = getCostMap(id);
        if (cm == null)
            return null;
        if (!type.equals(cm.meta.costType))
            return null;
        return cm;
    }

    @Override
	public RFC7285CostMap getCostMap(RFC7285VersionTag vtag, RFC7285CostType type) {
        //TODO
        return null;
    }

    @Override
	public RFC7285CostMap getCostMap(String id, RFC7285CostMap.Filter filter) {
        RFC7285CostMap cm = null;
        if (filter.costType != null) {
            cm = getCostMap(id, filter.costType);
        } else {
            cm = getCostMap(id);
        }

        if (cm == null)
            return null;

        if (filter.pids != null) {
            if (filter.pids.src == null)
                filter.pids.src = new LinkedList<String>(cm.map.keySet());
            if (filter.pids.dst == null)
                filter.pids.dst = new LinkedList<String>(cm.map.keySet());

            Map<String, Map<String, Object>> data = new LinkedHashMap<String, Map<String, Object>>();
            for (String src: filter.pids.src) {
                if (!cm.map.containsKey(src))
                    continue;
                if (data.containsKey(src))
                    continue;
                Map<String, Object> old_data = cm.map.get(src);
                if (old_data == null)
                    continue;

                Map<String, Object> new_data = new LinkedHashMap<String, Object>();
                for (String dst: filter.pids.dst) {
                    if (!old_data.containsKey(dst))
                        continue;
                    if (new_data.containsKey(dst))
                        continue;
                    new_data.put(dst, old_data.get(dst));
                }
                data.put(src, new_data);
            }
            cm.map = data;
        }
        return cm;
    }

    @Override
	public RFC7285CostMap getCostMap(RFC7285VersionTag vtag, RFC7285CostMap.Filter filter) {
        //TODO
        return null;
    }

    @Override
	public Boolean supportCostType(String id, RFC7285CostType type) {
        //TODO
        return true;
    }

    @Override
	public Boolean supportCostType(RFC7285VersionTag vtag, RFC7285CostType type) {
        //TODO
        return true;
    }

    @Override
	public Boolean validateCostMapFilter(String id, RFC7285CostMap.Filter filter) {
        //TODO
        return true;
    }

    @Override
	public Boolean validateCostMapFilter(RFC7285VersionTag vtag, RFC7285CostMap.Filter filter) {
        //TODO
        return true;
    }

    protected InstanceIdentifier<DefaultAltoNetworkMap> getDefaultNetworkMapIID() {
        InstanceIdentifier<DefaultAltoNetworkMap> iid = InstanceIdentifier.builder(Resources.class)
                                                .child(IRD.class)
                                                .child(Meta.class)
                                                .child(DefaultAltoNetworkMap.class).build();
        return iid;
    }

    protected InstanceIdentifier<NetworkMap> getNetworkMapIID(String resource_id) {
        NetworkMapKey key = new NetworkMapKey(ResourceId.getDefaultInstance(resource_id));
        InstanceIdentifier<NetworkMap> iid = InstanceIdentifier.builder(Resources.class)
                                                .child(NetworkMaps.class)
                                                .child(NetworkMap.class, key)
                                                .build();
        return iid;
    }

    protected InstanceIdentifier<CostMap> getCostMapIID(String resource_id) {
        CostMapKey key = new CostMapKey(ResourceId.getDefaultInstance(resource_id));
        InstanceIdentifier<CostMap> iid = InstanceIdentifier.builder(Resources.class)
                                                .child(CostMaps.class)
                                                .child(CostMap.class, key)
                                                .build();
        return iid;
    }
}
