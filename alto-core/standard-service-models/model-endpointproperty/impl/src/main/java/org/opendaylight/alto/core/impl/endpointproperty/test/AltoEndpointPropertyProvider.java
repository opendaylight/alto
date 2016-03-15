/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.core.impl.endpointproperty.test;


import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.alto.core.northbound.api.exception.AltoBadFormatException;
import org.opendaylight.alto.core.northbound.api.exception.AltoErrorTestException;
import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.resourcepool.rev150921.context.Resource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.PidName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.types.rev150921.SpecificEndpointProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.AltoModelEndpointpropertyService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.ResourceTypeEndpointproperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.alto.request.endpointproperty.request.EndpointpropertyRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.alto.response.endpointproperty.response.EndpointpropertyResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rev151021.endpointproperty.request.data.EndpointpropertyParams;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.TypedAddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointproperty.filter.data.endpointproperty.filter.PropertyFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.EndpointPropertyMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.EndpointProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.EndpointPropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.Properties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.PropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.endpointpropertymap.response.data.endpoint.property.map.endpoint.property.properties.PropertyContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.input.request.endpointproperty.request.endpointproperty.params.filter.EndpointpropertyFilterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.input.request.endpointproperty.request.endpointproperty.params.filter.endpointproperty.filter.data.endpointproperty.filter.property.filter.property.InputGlobalProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.EndpointPropertymapDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.endpoint.propertymap.data.endpoint.property.map.endpoint.property.properties.property.container.property.OutputResourceSpecificPropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointproperty.rfc7285.rev151021.query.output.response.endpointproperty.response.endpointproperty.data.endpoint.propertymap.data.endpoint.property.map.endpoint.property.properties.property.value.PidNameBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoEndpointPropertyProvider implements BindingAwareProvider, AutoCloseable, AltoModelEndpointpropertyService {

    private static final Logger LOG = LoggerFactory.getLogger(AltoEndpointPropertyProvider.class);

    private DataBroker m_dataBroker = null;
    private RoutedRpcRegistration<AltoModelEndpointpropertyService> m_serviceReg = null;
    private ListenerRegistration<DataChangeListener> m_listener=null;

    private static  final String TEST_ENDPOINTPROPERTY_NAME="test-model-endpointproperty";
    private static final ResourceId TEST_ENDPOINTPROPERTY_RID = new ResourceId(TEST_ENDPOINTPROPERTY_NAME);
    private InstanceIdentifier<Resource> m_testIID = null;

    protected void createContextTag()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        ResourcepoolUtils.createResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                TEST_ENDPOINTPROPERTY_NAME,
                ResourceTypeEndpointproperty.class,
                wx
        );

        ResourcepoolUtils.lazyUpdateResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                TEST_ENDPOINTPROPERTY_NAME, wx);

        wx.submit().get();
    }


    protected void removeContextTag()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException  {
        WriteTransaction wx = m_dataBroker.newWriteOnlyTransaction();

        ResourcepoolUtils.deleteResource(ResourcepoolUtils.DEFAULT_CONTEXT,
                TEST_ENDPOINTPROPERTY_NAME, wx);

        wx.submit().get();
    }

    protected void setupListener() {
        ResourcepoolUtils.ContextTagListener listener = new ResourcepoolUtils.ContextTagListener(m_testIID, m_serviceReg);
        m_listener = m_dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                m_testIID,listener, AsyncDataBroker.DataChangeScope.SUBTREE);

        assert m_listener != null;
    }
    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("AltoModelEndpoinPropertyProvider Session Initiated");

        m_dataBroker = session.getSALService(DataBroker.class);
        m_testIID = ResourcepoolUtils.getResourceIID(ResourcepoolUtils.DEFAULT_CONTEXT,
                TEST_ENDPOINTPROPERTY_NAME);
        m_serviceReg = session.addRoutedRpcImplementation(AltoModelEndpointpropertyService.class, this);

        try {
            setupListener();
            createContextTag();
        } catch (Exception e) {
        }
    }

    @Override
    public void close() throws Exception {
        LOG.info("AltoModelBaseProvider Closed");
        if (m_serviceReg != null) {
            m_serviceReg.close();
        }

        try {
            removeContextTag();
        } catch (Exception e) {
        }
    }

    @Override
    public Future<RpcResult<QueryOutput>> query(QueryInput input) {
        LOG.info("test-model-endpointproperty has been called");
        if (!input.getType().equals(ResourceTypeEndpointproperty.class)) {
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }

        EndpointpropertyRequest request = (EndpointpropertyRequest)input.getRequest();
        EndpointpropertyParams params = request.getEndpointpropertyParams();
        EndpointpropertyFilterData filter= (EndpointpropertyFilterData)params.getFilter();

        List<? extends TypedAddressData> endpoints=filter.getEndpointpropertyFilter().getEndpointFilter();
        List<PropertyFilter> properties = filter.getEndpointpropertyFilter().getPropertyFilter();

        for (PropertyFilter property: properties) {
            if (property.getProperty() instanceof InputGlobalProperty) {
                String propertyString = ((InputGlobalProperty)property.getProperty()).getGlobalProperty().getValue();
                if (propertyString.equals("no-such-property")) {
                    throw new AltoBadFormatException(AltoErrorTestException.ERROR_CODES.E_INVALID_FIELD_VALUE.name(), "properties", "no-such-property");
                }
            }
        }

        int order = 0;
        LinkedList<EndpointProperty> eppist = new LinkedList<EndpointProperty>();
        for (TypedAddressData endpoint: endpoints) {
            SourceBuilder srcBuilder = new SourceBuilder();
            srcBuilder.setAddress(endpoint.getAddress());

            PidName pidName = new PidName("PID1");
            PidNameBuilder pidNameBuilder = new PidNameBuilder();
            pidNameBuilder.setValue(pidName);

            EndpointPropertyBuilder epBuilder = new EndpointPropertyBuilder();
            epBuilder.setSource(srcBuilder.build());

            LinkedList<Properties> propertiesInResponse = new LinkedList<Properties>();
            PropertiesBuilder propertiesBuilder = new PropertiesBuilder();
            PropertyContainerBuilder pcBuilder = new PropertyContainerBuilder();
            pcBuilder.setProperty(
                    new OutputResourceSpecificPropertyBuilder().
                            setResourceSpecificProperty(new SpecificEndpointProperty("my-default-networkmap.pid")).build());
            propertiesBuilder.setPropertyContainer(pcBuilder.build()).setPropertyValue(pidNameBuilder.build());
            propertiesInResponse.add(propertiesBuilder.build());
            epBuilder.setProperties(propertiesInResponse);
            eppist.add(epBuilder.build());
        }

        EndpointPropertyMapBuilder endpointPropertyMapBuilder = new EndpointPropertyMapBuilder();
        endpointPropertyMapBuilder.setEndpointProperty(eppist);

        EndpointPropertymapDataBuilder endpointPropertymapDataBuilder = new EndpointPropertymapDataBuilder();
        endpointPropertymapDataBuilder.setEndpointPropertyMap(endpointPropertyMapBuilder.build());

        EndpointpropertyResponseBuilder endpointpropertyResponseBuilder = new EndpointpropertyResponseBuilder();
        endpointpropertyResponseBuilder.setEndpointpropertyData(endpointPropertymapDataBuilder.build());

        QueryOutputBuilder queryOutputBuilder = new QueryOutputBuilder();
        queryOutputBuilder.setType(ResourceTypeEndpointproperty.class).setResponse(endpointpropertyResponseBuilder.build());
        return RpcResultBuilder.success(queryOutputBuilder.build()).buildFuture();
    }

}
