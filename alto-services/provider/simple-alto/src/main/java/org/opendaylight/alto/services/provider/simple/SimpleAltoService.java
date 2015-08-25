/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.services.provider.simple;

import org.opendaylight.alto.commons.helper.NetworkMapIpPrefixHelper;
import org.opendaylight.alto.commons.types.converter.CostRequest2EndpointCostServiceInputConverter;
import org.opendaylight.alto.commons.types.converter.EndpointCostServiceOutput2CostResponseConverter;
import org.opendaylight.alto.commons.types.converter.YANGJSON2RFCCostMapConverter;
import org.opendaylight.alto.commons.types.converter.YANGJSON2RFCEndpointPropMapConverter;
import org.opendaylight.alto.commons.types.converter.YANGJSON2RFCIRDConverter;
import org.opendaylight.alto.commons.types.converter.YANGJSON2RFCNetworkMapConverter;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostType;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.AddressGroup;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.CostRequest;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.CostResponse;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.PropertyRequest;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint.PropertyResponse;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285EndpointPropertyMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285IRD;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;
import org.opendaylight.alto.services.api.rfc7285.AltoService;
import org.opendaylight.alto.services.api.rfc7285.NetworkMapService;
import org.opendaylight.alto.services.api.rfc7285.CostMapService;
import org.opendaylight.alto.services.api.rfc7285.EndpointCostService;
import org.opendaylight.alto.services.api.rfc7285.EndpointPropertyService;
import org.opendaylight.alto.services.api.rfc7285.IRDService;
import org.opendaylight.alto.commons.helper.ServiceHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.cost.map.map.DstCosts;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.AltoServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.EndpointCostServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.EndpointCostServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.Resources;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.CostMaps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.EndpointPropertyMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.IRD;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.NetworkMaps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.cost.maps.CostMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.cost.maps.CostMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.rev150404.resources.network.maps.NetworkMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TypedEndpointAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.TypedEndpointAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.Meta;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404.ird.meta.DefaultAltoNetworkMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.ServiceRegistration;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Iterator;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

@SuppressWarnings("rawtypes")
public class SimpleAltoService implements AltoService, AutoCloseable {

    private final Logger m_logger = LoggerFactory.getLogger(SimpleAltoService.class);
    private DataBroker m_db = null;
    private AltoServiceService m_service = null;

    private ObjectMapper m_mapper = new ObjectMapper();
    private RFC7285JSONMapper j_mapper = new RFC7285JSONMapper();
    private List<ServiceRegistration> m_reg = new LinkedList<ServiceRegistration>();
    private YANGJSON2RFCNetworkMapConverter m_nmconverter = null;
    private YANGJSON2RFCCostMapConverter m_cmconverter = null;
    private YANGJSON2RFCEndpointPropMapConverter m_epmconverter = null;
    private YANGJSON2RFCIRDConverter m_irdconverter = null;
    private EndpointCostServiceOutput2CostResponseConverter ecsOutputConverter = null;
    private CostRequest2EndpointCostServiceInputConverter ecsInputConverter = null;
    private NetworkMapIpPrefixHelper iHelper = new NetworkMapIpPrefixHelper();
    private final String PRIV_NETWORK_MAP = "private-network-map";
    private final String PRIV_ENDPOINT_PROPERTY_NAME = "priv:ietf-type";

    protected class DstCostSerializer extends JsonSerializer<DstCosts> {
        @Override
        public void serialize(DstCosts value, JsonGenerator jgen, SerializerProvider provider) {
            try {
                jgen.writeStartObject();

                jgen.writeObjectFieldStart("dst");
                jgen.writeStringField("value", value.getDst().getValue());
                jgen.writeEndObject();

                Map<Class<? extends Augmentation<?>>, Augmentation<?>> augmentations
                        = BindingReflections.getAugmentations(value);
                String cost = null;
                for (Augmentation<?> aug : augmentations.values()) {
                    try {
                        ObjectNode node = m_mapper.valueToTree(aug);
                        for (Iterator<String> itr = node.fieldNames(); itr.hasNext();) {
                            String field = itr.next();
                            if (field.toLowerCase().indexOf("cost") >= 0) {
                                cost = node.get(field).asText();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        m_logger.warn("Failed to write data from {}", cost);
                    }
                }
                if (cost != null) {
                    jgen.writeStringField("cost", cost);
                }

                jgen.writeEndObject();
            } catch (Exception e) {
                m_logger.info("Failed to parse DstCosts");
            }
        }
    }

    public SimpleAltoService(DataBroker db, AltoServiceService service) {
        this.m_db = db;
        this.m_service = service;
        this.m_nmconverter = new YANGJSON2RFCNetworkMapConverter();
        this.m_cmconverter = new YANGJSON2RFCCostMapConverter();
        this.m_epmconverter = new YANGJSON2RFCEndpointPropMapConverter();
        this.m_irdconverter = new YANGJSON2RFCIRDConverter();
        this.ecsOutputConverter = new EndpointCostServiceOutput2CostResponseConverter();
        this.ecsInputConverter = new CostRequest2EndpointCostServiceInputConverter();

        this.register(IRDService.class);
        this.register(NetworkMapService.class);
        this.register(CostMapService.class);
        this.register(EndpointPropertyService.class);
        this.register(EndpointCostService.class);

        try {
            SimpleModule module = new SimpleModule();
            module.addSerializer(DstCosts.class, new DstCostSerializer());
            m_mapper.registerModule(module);
        } catch (Exception e) {
            m_logger.info("failed to load customized serializer");
        }
    }

    protected <E> void register(Class<E> clazz) {
        ServiceRegistration reg = ServiceHelper.registerGlobalServiceWReg(clazz, this, null);
        if (reg != null)
            this.m_reg.add(reg);

        assert ServiceHelper.getGlobalInstance(clazz, this) != this;
    }

    @Override
    public void close() {
        for (ServiceRegistration reg : this.m_reg) {
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

        LinkedHashMap<String, RFC7285Endpoint.AddressGroup> map = new LinkedHashMap<String, RFC7285Endpoint.AddressGroup>();
        for (String pid : filter.pids) {
            if (nm.map.get(pid) != null)
                map.put(pid, nm.map.get(pid));
        }
        if (filter.pids.isEmpty()) {
            map = new LinkedHashMap<String, RFC7285Endpoint.AddressGroup>(nm.map);
        }
        LinkedHashMap<String, RFC7285Endpoint.AddressGroup> ret = new LinkedHashMap<String, RFC7285Endpoint.AddressGroup>();
        for (Map.Entry<String, RFC7285Endpoint.AddressGroup> entry : map.entrySet()) {
            String pid = entry.getKey();
            if (filter.addressTypes != null && (!filter.addressTypes.isEmpty())) {
                AddressGroup ag = new AddressGroup();
                if (filter.addressTypes.contains("ipv4")) {
                    ag.ipv4 = nm.map.get(pid).ipv4;
                }
                if (filter.addressTypes.contains("ipv6")) {
                    ag.ipv6 = nm.map.get(pid).ipv6;
                }
                if (!ag.ipv4.isEmpty() || !ag.ipv6.isEmpty())
                    ret.put(pid, ag);
            } else {
                ret.put(pid, entry.getValue());
            }
        }
        nm.map = ret;
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
        return (filter != null) && (filter.pids != null);
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
                m_logger.info(cm.toString());
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
        RFC7285CostMap cm = getCostMap(id + "-" + filter.costType.metric + "-" + filter.costType.mode);

        if (cm == null)
            return null;

        if (filter.pids != null) {
            if (filter.pids.src.isEmpty())
                filter.pids.src = new LinkedList<String>(cm.map.keySet());
            if (filter.pids.dst.isEmpty())
                filter.pids.dst = new LinkedList<String>(cm.map.keySet());

            Map<String, Map<String, Object>> data = new LinkedHashMap<String, Map<String, Object>>();
            for (String src : filter.pids.src) {
                if (!cm.map.containsKey(src))
                    continue;
                Map<String, Object> old_data = cm.map.get(src);
                if (old_data == null)
                    continue;

                Map<String, Object> new_data = new LinkedHashMap<String, Object>();
                for (String dst : filter.pids.dst) {
                    if (!old_data.containsKey(dst))
                        continue;
                    if (filter.constraints == null || filter.constraints.isEmpty()
                            || meetConstraints(filter.constraints, old_data.get(dst)))
                        new_data.put(dst, old_data.get(dst));
                }
                data.put(src, new_data);
            }
            cm.map = data;
        }
        return cm;
    }

    private boolean meetConstraints(List<String> constraints, Object object) {
        // We'd better simplify the constraints before using it.
        for (String constraint : constraints)
            if (!meetConstraint(constraint, object))
                return false;
        return true;
    }

    private boolean meetConstraint(String constraint, Object object) {
        String operator = constraint.substring(0, 2);
        double target = Double.parseDouble(object.toString());
        double value = Double.parseDouble(constraint.substring(3));
        switch (operator) {
        case "gt":
            return target > value;
        case "lt":
            return target < value;
        case "ge":
            return target >= value;
        case "le":
            return target <= value;
        case "eq":
            return target == value;
        }
        return false;
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

    protected InstanceIdentifier<EndpointPropertyMap> getEndpointPropertyMapIID() {
        InstanceIdentifier<EndpointPropertyMap> iid = InstanceIdentifier.builder(Resources.class)
                .child(EndpointPropertyMap.class).build();
        return iid;
    }

    @Override
    public PropertyResponse getEndpointProperty(PropertyRequest request) {
        InstanceIdentifier<EndpointPropertyMap> eiid = getEndpointPropertyMapIID();
        m_logger.info("EndpointPropertyMap IID: {}", eiid);
        updatePrivateNetworkMap();

        try {
            ReadOnlyTransaction tx = m_db.newReadOnlyTransaction();
            ListenableFuture<Optional<EndpointPropertyMap>> result = tx.read(LogicalDatastoreType.CONFIGURATION, eiid);
            if (result.get().isPresent()) {
                EndpointPropertyMap epm = result.get().get();
                ObjectNode node = m_mapper.valueToTree(epm);
                m_logger.info(m_mapper.writeValueAsString(epm));

                RFC7285EndpointPropertyMap endpointPropMap = m_epmconverter.convert(node);
                RFC7285EndpointPropertyMap ret = new RFC7285EndpointPropertyMap();
                ret.meta = endpointPropMap.meta;
                ret.meta.netmap_tags = getDependentTags(endpointPropMap.meta, request.properties);
                for (String addr : request.endpoints) {
                    Map<String, String> newProps = new LinkedHashMap<String, String>();
                    if (endpointPropMap.map.containsKey(addr.toLowerCase())) {
                        Map<String, String> props = endpointPropMap.map.get(addr);
                        for (String type : request.properties) {
                            if (props.containsKey(type)) {
                                newProps.put(type, props.get(type));
                            }
                        }
                    } else if (request.properties.contains(PRIV_ENDPOINT_PROPERTY_NAME)) {
                        newProps = getPrivateEndpointProperty(addr);
                    }
                    if (!newProps.isEmpty())
                        ret.map.put(addr, newProps);
                }
                return j_mapper.asPropertyResponse(j_mapper.asJSON(ret));
            } else {
                m_logger.info("Failed to read with eiid: {}", eiid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updatePrivateNetworkMap() {
        InstanceIdentifier<NetworkMap> niid = getNetworkMapIID(PRIV_NETWORK_MAP);
        try {
            ReadOnlyTransaction tx = m_db.newReadOnlyTransaction();
            ListenableFuture<Optional<NetworkMap>> result
                        = tx.read(LogicalDatastoreType.CONFIGURATION, niid);
            if (result.get().isPresent()) {
                NetworkMap privateNetworkMap = result.get().get();
                iHelper.update(privateNetworkMap);
            } else {
                m_logger.info("Failed to read with niid: {}", niid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<RFC7285VersionTag> getDependentTags(RFC7285EndpointPropertyMap.Meta meta, List<String> properties) {
        List<RFC7285VersionTag> dependentTags = new LinkedList<RFC7285VersionTag>();
        for (RFC7285VersionTag vtag : meta.netmap_tags) {
            if (properties.contains(vtag.rid + ".pid"))
                dependentTags.add(vtag);
        }
        return dependentTags;
    }

    private Map<String, String> getPrivateEndpointProperty(String addr) {
        Map<String, String> property = new LinkedHashMap<String, String>();
        try {
            TypedEndpointAddress address = TypedEndpointAddressBuilder.getDefaultInstance(addr);
            PidName pid = iHelper.getPIDByEndpointAddress(address);
            if (pid != null)
                property.put(PRIV_ENDPOINT_PROPERTY_NAME, pid.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return property;
    }

    @Override
    public RFC7285IRD getDefaultIRD() {
        InstanceIdentifier<IRD> iid = getIRDIID();

        try {
            ReadOnlyTransaction tx = m_db.newReadOnlyTransaction();
            ListenableFuture<Optional<IRD>> result = tx.read(LogicalDatastoreType.CONFIGURATION, iid);
            if (result.get().isPresent()) {
                IRD iIRD = result.get().get();
                m_logger.info(iIRD.toString());
                m_logger.info(m_mapper.writeValueAsString(iIRD));
                ObjectNode node = m_mapper.valueToTree(iIRD);

                RFC7285IRD ret = m_irdconverter.convert(node);
                m_logger.info("IRD convert compelete.");
                return ret;
            } else {
                m_logger.info("Failed to read with ciid: {}", iid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PropertyResponse getEndpointProperty(RFC7285VersionTag vtag, PropertyRequest request) {
        return null;
    }

    @Override
    public CostResponse getEndpointCost(CostRequest request) {
        CostResponse response = null;
        EndpointCostServiceInput input = this.ecsInputConverter.convert(request);
        Future<RpcResult<EndpointCostServiceOutput>> result = this.m_service.endpointCostService(input);
        try {
            EndpointCostServiceOutput output = result.get().getResult();
            response = this.ecsOutputConverter.convert(output);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public CostResponse getEndpointCost(RFC7285VersionTag vtag, CostRequest request) {
        return null;
    }

    public RFC7285IRD getIRD(String id) {
        return this.getDefaultIRD();
    }

    protected InstanceIdentifier<IRD> getIRDIID() {
        InstanceIdentifier<IRD> iid = InstanceIdentifier.builder(Resources.class).child(IRD.class).build();
        return iid;
    }
}
