package de.is24.util.monitoring;

import java.util.Arrays;
import junit.framework.TestCase;


public class MathTest extends TestCase {
  private static final int[] measurements1 = { 1 };
  private static final int[] measurements2 = { 1, 2, 3 };
  private static final int[] measurements3 = { 5, 5, 5, 5, 5, 5 };
  private static final int[] measurements4 = { 1, 3, 5 };
  private static final int[] measurements5 = {};

  public void testAll() {
    runTest(measurements1, 1, 0);
    runTest(measurements2, 2, 1);
    runTest(measurements3, 5, 0);
    runTest(measurements4, 3, 2);
    runTest(measurements5, 0, 0); // test zero-safeness
  }

  private void runTest(int[] measurements, double expectedAverage, double expectedStdDeviance) {
    long sum = 0, sumOfSquares = 0, n;
    n = measurements.length;
    for (int i : measurements) {
      sum += i;
      sumOfSquares += i * i;
    }
    System.out.println("Testing measurements        : " + Arrays.toString(measurements));
    System.out.println("Measurements number         : " + n);
    System.out.println("Sum                         : " + sum);
    System.out.println("Sum of squares              : " + sumOfSquares);

    final double avg = Math.average(n, sum);
    System.out.println("Calculated average          : " + avg);

    final double dev = Math.stdDeviation(n, sum, sumOfSquares);
    System.out.println("Calculated standard deviance: " + dev);

    assertEquals("Average", expectedAverage, avg);
    assertEquals("Standard deviance", expectedStdDeviance, dev);
  }
}
