package org.dragonsky;

import java.util.List;


public class Utils {

  private Utils() {
  }

  public static int add(int x, int y) {
    return x + y;
  }

  public static int sum(List<Integer> is) {
    int r = 0;
    for (int x: is) {
      r += x;
    }
    return r;
  }
}
