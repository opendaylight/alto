package org.opendaylight.alto.services.provider.simple;

import org.opendaylight.alto.services.api.rfc7285.NetworkMapService;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;
import org.opendaylight.alto.commons.types.converter.YANGJSON2RFCNetworkMapConverter;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.Resources;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.IRD;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.NetworkMaps;
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

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SimpleAltoService implements NetworkMapService, AutoCloseable {

    private final Logger m_logger = LoggerFactory.getLogger(SimpleAltoService.class);
    private DataBroker m_db = null;
    private ObjectMapper m_mapper = new ObjectMapper();
    private ServiceRegistration m_reg = null;
    private YANGJSON2RFCNetworkMapConverter m_converter = null;

    public SimpleAltoService(DataBroker db) {
        this.m_db = db;
        this.m_reg = ServiceHelper.registerGlobalServiceWReg(NetworkMapService.class, this, null);
        this.m_converter = new YANGJSON2RFCNetworkMapConverter();
        assert ServiceHelper.getGlobalInstance(NetworkMapService.class, this) != this;
    }

    @Override
    public void close() {
        this.m_reg.unregister();
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

                RFC7285NetworkMap ret = m_converter.convert(node);
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
}
