/**
 *
 */
package de.is24.util.monitoring.jmx;

import javax.management.MBeanAttributeInfo;
import de.is24.util.monitoring.Counter;


/**
 * A wrapper for monitoring {@link Counter} reportables.
 *
 * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
 */
class CounterJmxReportable implements JmxReportable {
  private final Counter reportable;

  /**
   * @param reportable the {@link Counter}. May not be null.
   */
  public CounterJmxReportable(Counter reportable) {
    super();
    assert reportable != null;
    this.reportable = reportable;
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.jmx.JmxReportable#getAttributes()
   */
  public MBeanAttributeInfo[] getAttributes() {
    return new MBeanAttributeInfo[] { new MBeanAttributeInfo("value", "long", "value", true, false, false) };
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.jmx.JmxReportable#getAttribute(java.lang.String)
   */
  public Object getAttribute(String attributeName) {
    return reportable.getCount();
  }
}
