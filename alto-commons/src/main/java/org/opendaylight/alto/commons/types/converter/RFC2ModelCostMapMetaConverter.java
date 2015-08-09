/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.converter;

import org.opendaylight.alto.commons.helper.Converter;
import org.opendaylight.alto.commons.types.model150404.ModelCostMapMeta;
import org.opendaylight.alto.commons.types.model150404.ModelDependentVtag;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;

public class RFC2ModelCostMapMetaConverter
    extends Converter<RFC7285CostMap.Meta, ModelCostMapMeta>{

  public RFC2ModelCostMapMetaConverter() {
  }

  public RFC2ModelCostMapMetaConverter(RFC7285CostMap.Meta _in) {
      super(_in);
  }

  @Override
  protected Object _convert() {
    ModelCostMapMeta modelMeta = new ModelCostMapMeta();

    ModelDependentVtag dvtag = new ModelDependentVtag();
    dvtag.rid = in().netmap_tags.get(0).rid;
    dvtag.vTag = in().netmap_tags.get(0).tag;
    modelMeta.dependentVtags.add(dvtag);

    modelMeta.costType.costMetric = in().costType.metric;
    modelMeta.costType.costMode = in().costType.mode;
    modelMeta.costType.description = in().costType.description;

    return modelMeta;
  }

}
