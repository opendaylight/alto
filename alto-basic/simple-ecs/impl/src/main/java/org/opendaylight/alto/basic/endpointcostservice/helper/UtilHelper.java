/*
 * Copyright © 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.endpointcostservice.helper;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.TypedAddressData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.endpoint.filter.data.endpoint.filter.source.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.endpoint.filter.data.endpoint.filter.source.address.Ipv6;

public class UtilHelper {
    public static String getIpFromTypedAddressData(TypedAddressData addressData){
        if(addressData.getAddress() instanceof Ipv4){
            return ((Ipv4) addressData.getAddress()).getIpv4().getValue();
        }
        else if(addressData.getAddress() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.endpoint.filter.data.endpoint.filter.destination.address.Ipv4){
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.endpoint.filter.data.endpoint.filter.destination.address.Ipv4) addressData.getAddress()).getIpv4().getValue();
        }
        else if(addressData.getAddress() instanceof Ipv6){
            return  ((Ipv6) addressData.getAddress()).getIpv6().getValue();
        }
        else if(addressData.getAddress() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.endpoint.filter.data.endpoint.filter.destination.address.Ipv6){
            return ((org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.model.endpointcost.rfc7285.rev151021.query.input.request.endpointcost.request.endpointcost.params.filter.endpoint.filter.data.endpoint.filter.destination.address.Ipv6) addressData.getAddress()).getIpv6().getValue();
        }
        return null;
    }
}
