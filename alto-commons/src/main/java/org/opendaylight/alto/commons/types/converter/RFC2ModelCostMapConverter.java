package org.opendaylight.alto.commons.types.converter;
import java.util.LinkedList;

import org.opendaylight.alto.commons.helper.Converter;
import org.opendaylight.alto.commons.types.model150404.ModelCostMap;
import org.opendaylight.alto.commons.types.model150404.ModelCostMapData;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;

public class RFC2ModelCostMapConverter 
    extends Converter<RFC7285CostMap, ModelCostMap>{

  protected RFC2ModelCostMapMetaConverter metaConv = new RFC2ModelCostMapMetaConverter();
  protected RFC2ModelCostMapDataConverter dataConv = new RFC2ModelCostMapDataConverter();
  
  public RFC2ModelCostMapConverter() {
  }

  public RFC2ModelCostMapConverter(RFC7285CostMap _in) {
      super(_in);
  }
  
  @Override
  protected Object _convert() {
    ModelCostMap out = new ModelCostMap();
    out.rid = getCostMapResourceId(in());
    out.tag = "";
    
    out.meta = metaConv.convert(in().meta);
    out.map = new LinkedList<ModelCostMapData>();
    for (String src : in().map.keySet()) {
      ModelCostMapData data = new ModelCostMapData();
      data.src = src;
      data.dstCosts = dataConv.convert(in().map.get(src));
      out.map.add(data);
    }
    return out;
  }
  
  public static void main(String[] args) {
    String rfcCostMap = "{\"meta\":{\"dependent-vtags\":[{\"resource-id\":\"my-default-network-map\",\"tag\":\"3ee2cb7e8d63d9fab71b9b34cbf764436315542e\"}],\"cost-type\":{\"cost-mode\":\"numerical\",\"cost-metric\":\"routingcost\"}},\"cost-map\":{\"PID1\":{\"PID1\":1,\"PID2\":5,\"PID3\":10},\"PID2\":{\"PID1\":5,\"PID2\":1,\"PID3\":15},\"PID3\":{\"PID1\":20,\"PID2\":15}}}";
    try {
      RFC7285JSONMapper mapper = new RFC7285JSONMapper();
      RFC7285CostMap costMap = mapper.asCostMap(rfcCostMap);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private String getCostMapResourceId(RFC7285CostMap costMap) {
    String networkMapRID = costMap.meta.netmap_tags.get(0).rid;
    String costMetric = costMap.meta.costType.metric;
    String costMode = costMap.meta.costType.mode;
    return networkMapRID + "-" + costMetric + "-" + costMode;
  }
}
