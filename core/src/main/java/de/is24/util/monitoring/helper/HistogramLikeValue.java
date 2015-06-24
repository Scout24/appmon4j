package de.is24.util.monitoring.helper;

import de.is24.util.monitoring.CorePlugin;
import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.StateValueProvider;


/**
 * <p>Convenience class that makes it easier to log values in a histogram-like fashion.
 * Values are automatically grouped into ranges/"bins" of similar values; the width of the
 * "bins" into which values are grouped can be specified by the constructor parameter "factor".
 * New values are divided by the given factor in order to determine the bin in which the
 * value should be counted, e.g. if you construct a HistogramLikeValue with a factor of "1000"
 * and add the following values</p><pre>
 *   50
 *   80
 *   100
 *   150
 *   1050
 *   1100</pre>
 * <p>then these values are grouped into two different bins, one between 0-999 and one between 1000-1999.
 * In this example, you would have 4 values in the bin named "biggerThan0" and 2 values in
 * the bin named "biggerThan1000".</p>
 * <p>See unit test for details.</p>
 *
 * <p>Furthermore, if you specify a "maxLimit" as optional constructor argument, all values bigger
 * than this maximum threshold will be grouped in one single bin (biggerThan&lt;maxLimit&gt;) rather
 * than grouped into separate bins as specified by "factor".</p>
 *
 * <p>The "bins" are not sized automatically because this would require storing all added
 * values so that the bin values could be re-calculated.</p>
 *
 * <p>The HistogramLikeValue automatically creates counter and timer values in the background;
 * the names of these Reportables are constructed using the "baseName" specified as
 * constructor argument. In the end, the following InApplicationMonitor reportables are created
 * by this class:</p><pre>
 *   &lt;basename&gt;.total                   Timer recording all values
 *   &lt;basename&gt;.biggerThan&lt;value&gt;       Counter for each bin that is created
 *   &lt;basename&gt;.currentMax              StateValue holding the current maximum value
 * </pre>
 * @author ptraeder
 *
 */
public class HistogramLikeValue {
  public static final String NAME_BIGGER_THAN = ".biggerThan";
  public static final String NAME_FACTOR = ".factor";
  public static final String NAME_CURRENT_MAX = ".currentMax";
  private String baseName;
  private String timerName;
  private String factorName;
  private long factor;
  private long maxLimit = Long.MAX_VALUE;
  private long currentMaxValue = 0L;
  private String maxValueName;
  private String maxLimitName;

  /**
   * @param baseName the base name to use for InApplicationMonitor value name
   * @param factor the factor to divide new values by in order to group them into bins
   */
  public HistogramLikeValue(String baseName, final long factor) {
    this.baseName = baseName;
    this.timerName = baseName + ".total";
    this.maxValueName = baseName + NAME_CURRENT_MAX;
    this.maxLimitName = baseName + ".maxLimit";
    this.factorName = baseName + NAME_FACTOR;

    this.factor = factor;
    register(InApplicationMonitor.getInstance().getCorePlugin());
  }

  private void register(CorePlugin corePlugin) {
    // publish the currentMaxValue and the maxLimit as state values
    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public String getName() {
          return maxValueName;
        }

        @Override
        public long getValue() {
          return currentMaxValue;
        }
      });

    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public String getName() {
          return maxLimitName;
        }

        @Override
        public long getValue() {
          return maxLimit;
        }
      });
    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public String getName() {
          return factorName;
        }

        @Override
        public long getValue() {
          return factor;
        }
      });
  }

  /**
  * @param baseName the base name to use for InApplicationMonitor value name
  * @param factor the factor to divide new values by in order to group them into bins
  * @param maxLimit the upper limit up to which bins are created - all values bigger than maxLimit are grouped into one single bin
  */
  public HistogramLikeValue(String baseName, long factor, long maxLimit) {
    this(baseName, factor);

    this.maxLimit = maxLimit;
  }

  public String getBaseName() {
    return baseName;
  }

  private String getBinName(long value) {
    StringBuilder binName = new StringBuilder();
    binName.append(baseName);
    binName.append(NAME_BIGGER_THAN);
    binName.append(value * factor);
    return binName.toString();
  }

  /**
   * adds a new value to the InApplicationMonitor, grouping it into the appropriate bin.
   *
   * @param newValue the value that should be added
   */
  public void addValue(long newValue) {
    // keep a "total" timer for all values
    InApplicationMonitor.getInstance().addTimerMeasurement(timerName, newValue);

    // keep track of the current maximum value
    if (newValue > currentMaxValue) {
      currentMaxValue = newValue;
    }

    // select the bin to put this value in
    long binIndex;
    if (newValue > maxLimit) {
      binIndex = maxLimit / factor;
    } else {
      binIndex = newValue / factor;
    }

    // add the new value to the appropriate bin
    String binName = getBinName(binIndex);
    InApplicationMonitor.getInstance().incrementCounter(binName);
  }

}
