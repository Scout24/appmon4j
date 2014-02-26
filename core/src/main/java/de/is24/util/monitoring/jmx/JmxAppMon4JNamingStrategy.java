package de.is24.util.monitoring.jmx;


/**
 *
 * JmxAppMon4JNamingStrategy provides a prefix for the InApplicationMonitorJMXConnector.
 * It should be used in cases where more the one {@link InApplicationMonitorJMXConnector} / {@link de.is24.util.monitoring.InApplicationMonitor}
 * is created on a single VM, i.e. on tomcat in different WebApps.
 *
 *
 * @author JGaedicke
 * @see WebContextJmxAppMon4JNamingStrategy in common-springUtil
 */
public interface JmxAppMon4JNamingStrategy {
  /**
   * @return the prefix used for the jmx object name.
   */
  String getJmxPrefix();
}
