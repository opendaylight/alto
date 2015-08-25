/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.converter;

import java.util.LinkedList;

import org.opendaylight.alto.commons.helper.Converter;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostType;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285IRD;

import com.fasterxml.jackson.databind.JsonNode;

public class YANGJSON2RFCIRDConverter extends Converter<JsonNode, RFC7285IRD> {

    public YANGJSON2RFCIRDConverter() {
    }

    public YANGJSON2RFCIRDConverter(JsonNode _in) {
        super(_in);
    }

    @Override
    protected Object _convert() {
        JsonNode node = this.in();
        RFC7285IRD ird = new RFC7285IRD();

        JsonNode meta = node.get("meta");
        for (JsonNode costType : meta.get("costTypes")) {
            String costTypeName = costType.get("costTypeName").get("value").asText();
            String costMode = costType.get("costMode").asText().toLowerCase();
            String costMatric = costType.get("costMetric").get("value").asText();
            if (!costType.get("description").isNull()) {
                ird.meta.costTypes.put(costTypeName,
                        new RFC7285CostType(costMode, costMatric, costType.get("description").asText()));
            } else {
                ird.meta.costTypes.put(costTypeName, new RFC7285CostType(costMode, costMatric));
            }

        }
        ird.meta.defaultAltoNetworkMap = meta.get("defaultAltoNetworkMap").get("resourceId").get("value").asText();

        JsonNode resources = node.get("resources");
        for (JsonNode res : resources) {
            String rid = res.get("resourceId").get("value").asText();

            RFC7285IRD.Entry ent = ird.new Entry();
            ent.uri = res.get("uri").get("value").asText();
            ent.mediaType = res.get("mediaType").get("value").asText();
            if (!res.get("uses").isNull()) {
                ent.uses = new LinkedList<String>();
                for (JsonNode use : res.get("uses")) {
                    ent.uses.add(use.get("value").asText());
                }
            }

            if (!res.get("capabilities").isNull()) {
                ent.capabilities = ird.new Capability();
                if (!res.get("capabilities").get("costConstraints").isNull()) {
                    ent.capabilities.costConstraints = res.get("capabilities").get("costConstraints").asBoolean();

                }
                if (!res.get("capabilities").get("costTypeNames").isNull()) {
                    ent.capabilities.costTypeNames = new LinkedList<String>();
                    for (JsonNode name : res.get("capabilities").get("costTypeNames")) {
                        ent.capabilities.costTypeNames.add(name.get("value").asText());
                    }
                }
                if (!res.get("capabilities").get("propTypes").isNull()) {
                    ent.capabilities.propTypes = new LinkedList<String>();
                    for (JsonNode prop : res.get("capabilities").get("propTypes")) {
                        ent.capabilities.propTypes.add(prop.get("value").asText());
                    }
                }
            }

            if (!res.get("accepts").isNull()) {
                for (JsonNode act : res.get("accepts")) {
                    ent.accepts = act.get("value").asText();
                }
            }

            ird.resources.put(rid, ent);
        }
        return ird;
    }

    private RFC7285CostType RFC7285CostType() {
        // TODO Auto-generated method stub
        return null;
    }

}