package de.is24.util.monitoring;

/**
 * Util method for Maths that are not provided by standard libraries.
 * <p>
 * Current functions available are:
 * <ul>
 * <li>Calculate the average from an amount of values n and a sum.</li>
 * <li>Calculate the standard deviation from an amount of values n, a sum and a sum of squares.</li>
 * </ul>
 *
 * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
 */
public class Math {
  protected Math() {} // utility class constructor

  /**
   * Calculate the average from an amount of values n and a sum.
   *
   * @param n the number of values measured.
   * @param sum the total sum of values measured.
   * @return the average of a number of values.
   */
  public static double average(final long n, final double sum) {
    double avg = 0;
    if (n != 0) { // avoid 0 divides
      avg = sum / n;
    }
    return avg;
  }


  /**
   * Calculate the standard deviation from an amount of values n, a sum and a sum of squares.
   *
   * @param n the number of values measured.
   * @param sum the total sum of values measured.
   * @param sumOfSquares the total sum of squares of the values measured.
   * @return the standard deviation of a number of values.
   */
  public static double stdDeviation(final long n, final double sum, final double sumOfSquares) {
    double stdDev = 0;
    if (n > 1) { // std deviation for 1 entry is 0 by definition

      final double numerator = sumOfSquares - ((sum * sum) / n);
      stdDev = java.lang.Math.sqrt(numerator / (n - 1));
    }
    return stdDev;
  }
}
