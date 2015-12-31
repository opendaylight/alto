/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.alto.basic.manual.maps.ManualMapsUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.rev151021.ConfigResponseData1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.rev151021.Meta1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.rev151021.config.context.ResourceCostMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.rev151021.config.context.ResourceNetworkMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.rev151021.query.input.request.config.request.config.request.message.config.resource.data.ConfigCostmapData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.rev151021.query.input.request.config.request.config.request.message.config.resource.data.ConfigNetworkmapData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.rev151021.query.output.response.config.response.config.response.message.config.response.data.ConfigCostmapResponseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.basic.manual.maps.rev151021.query.output.response.config.response.config.response.message.config.response.data.ConfigNetworkmapResponseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.AltoModelConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.ResourceTypeConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.alto.request.config.request.ConfigRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.alto.response.config.response.ConfigResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.config.request.data.ConfigRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.config.response.data.ConfigResponseMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.config.response.data.config.response.message.ConfigResponseData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.config.rev151021.config.response.data.config.response.message.Meta;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.rev151021.alto.request.base.Request;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

public class AltoModelConfigImpl implements AltoModelConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(AltoModelConfigImpl.class);
    private DataBroker dataBroker = null;

    private static final String RESPONSE_ERROR_CODE_OK = "OK";
    private static final String RESPONSE_ERROR_CODE_FAILED = "FAILED";
    private static final List<String> CONFIG_RESOURCE_TYPES = Arrays.asList("networkmap", "costmap");

    public AltoModelConfigImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    protected boolean validateConfigResourceType(String configResourceType) {
        return CONFIG_RESOURCE_TYPES.contains(configResourceType);
    }

    protected QueryOutput readResourceNetworkMap(String rid, final ReadTransaction rx) {
        InstanceIdentifier<ResourceNetworkMap> iid = ManualMapsUtils.getResourceNetworkMapIID(rid);
        CheckedFuture<Optional<ResourceNetworkMap>, ReadFailedException> future =
                rx.read(LogicalDatastoreType.OPERATIONAL, iid);
        Optional<ResourceNetworkMap> optional = Optional.absent();

        QueryOutputBuilder outputBuilder = new QueryOutputBuilder();
        outputBuilder.setType(ResourceTypeConfig.class);
        ConfigResponseBuilder crBuilder = new ConfigResponseBuilder();
        ConfigResponseMessageBuilder crmBuilder = new ConfigResponseMessageBuilder();
        ConfigResponseData1Builder crdBuilder = new ConfigResponseData1Builder();

        ResourceNetworkMap result = null;
        String errorCode = RESPONSE_ERROR_CODE_FAILED;

        try {
            optional = future.checkedGet();
            if (optional.isPresent()) {
                result = optional.get();
                crdBuilder.setConfigNetworkmapResponseData(new ConfigNetworkmapResponseDataBuilder()
                        .setResourceId(result.getResourceId())
                        .setTag(result.getTag())
                        .setMap(result.getMap())
                        .build());
                errorCode = RESPONSE_ERROR_CODE_OK;
            }
        } catch (Exception e) {
            LOG.warn("Reading resource failed! ResourceId: " + rid);
        }
        crmBuilder
                .setMeta((Meta) new Meta1Builder().setConfigResponseErrorCode(errorCode).build())
                .setConfigResponseData((ConfigResponseData) crdBuilder.build());
        crBuilder.setConfigResponseMessage(crmBuilder.build());
        outputBuilder.setResponse(crBuilder.build());
        return outputBuilder.build();
    }

    protected QueryOutput readResourceCostMap(String rid, final ReadTransaction rx) {
        InstanceIdentifier<ResourceCostMap> iid = ManualMapsUtils.getResourceCostMapIID(rid);
        CheckedFuture<Optional<ResourceCostMap>, ReadFailedException> future =
                rx.read(LogicalDatastoreType.OPERATIONAL, iid);
        Optional<ResourceCostMap> optional = Optional.absent();

        QueryOutputBuilder outputBuilder = new QueryOutputBuilder();
        outputBuilder.setType(ResourceTypeConfig.class);
        ConfigResponseBuilder crBuilder = new ConfigResponseBuilder();
        ConfigResponseMessageBuilder crmBuilder = new ConfigResponseMessageBuilder();
        ConfigResponseData1Builder crdBuilder = new ConfigResponseData1Builder();

        ResourceCostMap result = null;
        String errorCode = RESPONSE_ERROR_CODE_FAILED;

        try {
            optional = future.checkedGet();
            if (optional.isPresent()) {
                result = optional.get();
                crdBuilder.setConfigCostmapResponseData(new ConfigCostmapResponseDataBuilder()
                        .setResourceId(result.getResourceId())
                        .setTag(result.getTag())
                        .setMeta(result.getMeta())
                        .setMap(result.getMap())
                        .build());
                errorCode = RESPONSE_ERROR_CODE_OK;
            }
        } catch (Exception e) {
            LOG.warn("Reading resource failed! ResourceId: " + rid);
        }
        crmBuilder
                .setMeta((Meta) new Meta1Builder().setConfigResponseErrorCode(errorCode).build())
                .setConfigResponseData((ConfigResponseData) crdBuilder.build());
        crBuilder.setConfigResponseMessage(crmBuilder.build());
        outputBuilder.setResponse(crBuilder.build());
        return outputBuilder.build();
    }

    @Override
    public Future<RpcResult<QueryOutput>> query(QueryInput input) {
        if (!(input.getType().equals(ResourceTypeConfig.class))) {
            LOG.warn("Unsupported Request!");
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }
        Request _request = input.getRequest();
        if (!(_request instanceof ConfigRequest)) {
            LOG.warn("Request is inconsistent with input");
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }
        ConfigRequest request = (ConfigRequest) _request;
        ConfigRequestMessage requestMessage = request.getConfigRequestMessage();
        String resourceId = requestMessage.getConfigResourceId();
        if (!validateConfigResourceType(requestMessage.getConfigResourceType())) {
            LOG.warn("Unsupported ResourceType in Request!");
            return RpcResultBuilder.<QueryOutput>failed().buildFuture();
        }

        final ReadWriteTransaction rwx = dataBroker.newReadWriteTransaction();
        QueryOutput output = null;

        if (requestMessage.getConfigType() == ConfigRequestMessage.ConfigType.Get) {
            if (requestMessage.getConfigResourceType() == "networkmap") {
                output = readResourceNetworkMap(resourceId, rwx);
            } else if (requestMessage.getConfigResourceType() == "costmap") {
                rwx.read(LogicalDatastoreType.CONFIGURATION, ManualMapsUtils.getResourceCostMapIID(resourceId));
            }
        } else if (requestMessage.getConfigType() == ConfigRequestMessage.ConfigType.Delete) {
            if (requestMessage.getConfigResourceType() == "networkmap") {
                ManualMapsUtils.deleteResourceNetworkMap(resourceId, rwx);
            } else if (requestMessage.getConfigResourceType() == "costmap") {
                ManualMapsUtils.deleteResourceCostMap(resourceId, rwx);
            }
        } else if (requestMessage.getConfigType() == ConfigRequestMessage.ConfigType.Create) {
            if (requestMessage.getConfigResourceType() == "networkmap") {
                ConfigNetworkmapData networkmapData = (ConfigNetworkmapData) requestMessage.getConfigResourceData();
                ManualMapsUtils.createResourceNetworkMap(resourceId, networkmapData.getMap(), rwx);
            } else if (requestMessage.getConfigResourceType() == "costmap") {
                ConfigCostmapData costmapData = (ConfigCostmapData) requestMessage.getConfigResourceData();
                ManualMapsUtils.createResourceCostMap(resourceId, costmapData.getMeta(), costmapData.getMap(), rwx);
            }
        }
        rwx.submit();
        return null;
    }
}
