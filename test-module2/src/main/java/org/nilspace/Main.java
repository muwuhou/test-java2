package org.nilspace;

import com.google.common.collect.ImmutableList;
import org.dragonsky.Utils;


public class Main {

  public static void main(String[] args) {
    int n = Utils.add(3, 4);
    int m = Utils.sum(ImmutableList.of(1, 2, 3, 4));
    System.out.printf("%d, %d\n", n, m);
  }
}
