import java.math.BigInteger;
import org.testng.annotations.Test;


public class MyTest1 {

  @Test
  public void test1() {
    System.out.println("hello world!");
  }

  @Test
  public void test2() {
    String msg = String.format("hello %s, %s", "tome", null);
    System.out.println(msg);
  }

  @Test
  public void findHighestPrime() {
    for (int i = 64; i > 0; --i) {
      for (int j = i - 1; j > 0; --j) {
        for (int k = j - 1; k > 0; --k) {
          BigInteger bi = new BigInteger("1");
          bi = bi.setBit(i).setBit(j).setBit(k);
          if (bi.isProbablePrime(100)) {
            System.out.printf("%d, %d, %d\n", i, j, k);
            System.out.println("find:" +  bi);
            return;
          }
        }
      }
    }
  }
}
