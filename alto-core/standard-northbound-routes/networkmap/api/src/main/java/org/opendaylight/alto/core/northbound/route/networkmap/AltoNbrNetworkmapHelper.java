/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.core.northbound.route.networkmap;


import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.networkmap.rev151021.Resources;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.networkmap.rev151021.resources.Resource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.core.northbound.route.networkmap.rev151021.resources.ResourceKey;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AltoNbrNetworkmapHelper {

    public static InstanceIdentifier<Resource> getNbrNetworkmapResourceIID(String path) {
        ResourceKey key = new ResourceKey(new Uri(path));
        return InstanceIdentifier.builder(Resources.class).child(Resource.class, key).build();
 }
}

