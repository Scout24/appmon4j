/**
 *
 */
package de.is24.util.monitoring.jmx;

import javax.management.MBeanAttributeInfo;
import de.is24.util.monitoring.Timer;


/**
 * A wrapper for monitoring {@link Timer} reportables.
 *
 * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
 */
class TimerJmxReportable implements JmxReportable {
  private final Timer reportable;

  /**
   * @param reportable the {@link Timer}. May not be null.
   */
  public TimerJmxReportable(Timer reportable) {
    super();
    assert reportable != null;
    this.reportable = reportable;
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.jmx.JmxReportable#getAttributes()
   */
  public MBeanAttributeInfo[] getAttributes() {
    return new MBeanAttributeInfo[] {
        new MBeanAttributeInfo("count", "long", "count", true, false, false),
        new MBeanAttributeInfo("timerSum", "long",
          "sum of all measurements reported for this timer", true, false, false),
        new MBeanAttributeInfo("average", "double",
          "average duration for all measurements reported for this timer", true, false, false),
        new MBeanAttributeInfo("stdDeviation", "double",
          "standard deviation for all measurements reported for this timer", true, false, false)
      };
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.jmx.JmxReportable#getAttribute(java.lang.String)
   */
  public Object getAttribute(String attributeName) {
    if (attributeName.equals("count")) {
      return reportable.getCount();
    } else if (attributeName.equals("timerSum")) {
      return reportable.getTimerSum();
    } else if (attributeName.equals("average")) {
      return (reportable.getTimerAvg());
    } else if (attributeName.equals("stdDeviation")) {
      return (reportable.getTimerStdDev());
    }
    return null;
  }
}
