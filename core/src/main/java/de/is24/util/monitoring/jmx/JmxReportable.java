package de.is24.util.monitoring.jmx;

import javax.management.MBeanAttributeInfo;


/**
 * Encapsulates attribute accessing for one type of monitoring reportables.
 *
 * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
 */
interface JmxReportable {
  /**
   * @return a list of {@link MBeanAttributeInfo}, that is the specification of available attributes for monitoring.
   */
  MBeanAttributeInfo[] getAttributes();

  /**
   * @param attributeName the name of the attribute to be monitored.
   * @return the current value of the requested attribute.
   */
  Object getAttribute(String attributeName);
}
