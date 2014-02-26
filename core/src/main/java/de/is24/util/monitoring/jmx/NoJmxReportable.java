/**
 *
 */
package de.is24.util.monitoring.jmx;

import javax.management.MBeanAttributeInfo;


/**
 * Null-object pattern implementation of {@link JmxReportable}.
 * Returns an empty attributes specification and therefore only null values.
 *
 * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
 */
final class NoJmxReportable implements JmxReportable {
  private static final MBeanAttributeInfo[] NONE = new MBeanAttributeInfo[0];

  private static final NoJmxReportable INSTANCE = new NoJmxReportable();

  public static NoJmxReportable getInstance() {
    return INSTANCE;
  }

  /**
   * Singleton, therefore no constructor.
   */
  private NoJmxReportable() {
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.jmx.JmxReportable#getAttributes()
   */
  public MBeanAttributeInfo[] getAttributes() {
    return NONE;
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.jmx.JmxReportable#getAttribute(java.lang.String)
   */
  public Object getAttribute(String attributeName) {
    return null;
  }
}
