package org.opendaylight.alto.commons.types.converter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opendaylight.alto.commons.helper.Converter;
import org.opendaylight.alto.commons.types.model150404.ModelDstCosts;

public class RFC2ModelCostMapDataConverter
    extends Converter<Map<String, Object>, List<ModelDstCosts>> {

  public RFC2ModelCostMapDataConverter() {
  }

  public RFC2ModelCostMapDataConverter(Map<String, Object> _in) {
      super(_in);
  }

  @Override
  protected Object _convert() {
    List<ModelDstCosts> dstCostsList = new LinkedList<ModelDstCosts>();
    for (String dst : in().keySet()) {
      ModelDstCosts dstCosts = new ModelDstCosts();
      dstCosts.dst = dst;
      dstCosts.cost = in().get(dst);
      dstCostsList.add(dstCosts);
    }
    return dstCostsList;
  }

}
