package org.opendaylight.alto.commons.types.converter;
import java.util.LinkedList;

import org.opendaylight.alto.commons.helper.Converter;
import org.opendaylight.alto.commons.types.model150404.ModelCostMap;
import org.opendaylight.alto.commons.types.model150404.ModelCostMapData;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285CostMap;
import java.security.MessageDigest;

public class RFC2ModelCostMapConverter
    extends Converter<RFC7285CostMap, ModelCostMap>{

  protected RFC2ModelCostMapMetaConverter metaConv = new RFC2ModelCostMapMetaConverter();
  protected RFC2ModelCostMapDataConverter dataConv = new RFC2ModelCostMapDataConverter();

  public RFC2ModelCostMapConverter() {
  }

  public RFC2ModelCostMapConverter(RFC7285CostMap _in) {
      super(_in);
  }

  public String generateTag(){
      MessageDigest instance = null;
      try {
          instance = MessageDigest.getInstance("MD5");
      } catch (java.security.NoSuchAlgorithmException e) {
          instance = null;
      }
      byte[] messageDigest = instance.digest(String.valueOf(System.nanoTime()).getBytes());
      StringBuilder hexString = new StringBuilder();
      for (int i = 0; i < messageDigest.length; i++) {
          String hex = Integer.toHexString(0xFF & messageDigest[i]);
          if (hex.length() == 1) {
              hexString.append('0');
          }
          hexString.append(hex);
      }
      return hexString.toString();
  }

  @Override
  protected Object _convert() {
    ModelCostMap out = new ModelCostMap();
    out.rid = getCostMapResourceId(in());
    out.tag = generateTag();
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

  private String getCostMapResourceId(RFC7285CostMap costMap) {
    String networkMapRID = costMap.meta.netmap_tags.get(0).rid;
    String costMetric = costMap.meta.costType.metric;
    String costMode = costMap.meta.costType.mode;
    return networkMapRID + "-" + costMetric + "-" + costMode;
  }
}
