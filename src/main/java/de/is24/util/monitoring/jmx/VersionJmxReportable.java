/**
 *
 */
package de.is24.util.monitoring.jmx;

import javax.management.MBeanAttributeInfo;
import de.is24.util.monitoring.Version;


/**
 * A wrapper for {@link Version} reportables.
 *
 * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
 */
class VersionJmxReportable implements JmxReportable {
  private final Version reportable;

  /**
   * @param reportable the {@link Version}. May not be null.
   */
  public VersionJmxReportable(Version reportable) {
    super();
    assert reportable != null;
    this.reportable = reportable;
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.jmx.JmxReportable#getAttributes()
   */
  public MBeanAttributeInfo[] getAttributes() {
    return new MBeanAttributeInfo[] { new MBeanAttributeInfo("value", "String", "value", true, false, false) };
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.jmx.JmxReportable#getAttribute(java.lang.String)
   */
  public Object getAttribute(String attributeName) {
    return reportable.getValue();
  }
}
