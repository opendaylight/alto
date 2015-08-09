/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.converter;

import java.util.List;
import java.util.LinkedList;

import org.opendaylight.alto.commons.helper.Converter;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285Endpoint;
import org.opendaylight.alto.commons.types.model150404.ModelEndpointAddressGroup;

public class RFC2ModelEndpointAddressGroupConverter
        extends Converter<RFC7285Endpoint.AddressGroup, List<ModelEndpointAddressGroup>> {

    public RFC2ModelEndpointAddressGroupConverter() {
    }

    public RFC2ModelEndpointAddressGroupConverter(RFC7285Endpoint.AddressGroup _in) {
        super(_in);
    }

    @Override
    protected Object _convert() {
        List<ModelEndpointAddressGroup> out = new LinkedList<ModelEndpointAddressGroup>();
        if ((in().ipv4 != null) && (!in().ipv4.isEmpty())) {
            ModelEndpointAddressGroup v4 = new ModelEndpointAddressGroup();
            v4.type = ModelEndpointAddressGroup.IPV4;
            v4.prefixes = in().ipv4;
            out.add(v4);
        }
        if ((in().ipv6 != null) && (!in().ipv6.isEmpty())) {
            ModelEndpointAddressGroup v6 = new ModelEndpointAddressGroup();
            v6.type = ModelEndpointAddressGroup.IPV6;
            v6.prefixes = in().ipv6;
            out.add(v6);
        }
        return out;
    }
}
