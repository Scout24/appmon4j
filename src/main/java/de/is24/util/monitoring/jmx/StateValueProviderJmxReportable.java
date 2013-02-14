/**
 *
 */
package de.is24.util.monitoring.jmx;

import javax.management.MBeanAttributeInfo;
import de.is24.util.monitoring.StateValueProvider;


/**
 * A wrapper for monitoring {@link StateValueProvider} reportables.
 *
 * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
 */
class StateValueProviderJmxReportable implements JmxReportable {
  private final StateValueProvider reportable;

  /**
   * @param reportable the {@link StateValueProvider}. May not be null.
   */
  public StateValueProviderJmxReportable(StateValueProvider reportable) {
    super();
    assert reportable != null;
    this.reportable = reportable;
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.jmx.JmxReportable#getAttributes()
   */
  public MBeanAttributeInfo[] getAttributes() {
    return new MBeanAttributeInfo[] { new MBeanAttributeInfo("long", "String", "value", true, false, false) };
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.jmx.JmxReportable#getAttribute(java.lang.String)
   */
  public Object getAttribute(String attributeName) {
    return reportable.getValue();
  }
}
