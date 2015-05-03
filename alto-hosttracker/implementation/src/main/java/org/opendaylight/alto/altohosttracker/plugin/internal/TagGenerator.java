package org.opendaylight.alto.altohosttracker.plugin.internal;

import java.util.Random;

public class TagGenerator {
  public static String getTag(int length){
    String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    Random random = new Random();
    StringBuffer sb = new StringBuffer();
    for(int i =0;i<length;i++){
      int number = random.nextInt(base.length());
      sb.append(base.charAt(number));
    }
    return sb.toString();
  }
}
