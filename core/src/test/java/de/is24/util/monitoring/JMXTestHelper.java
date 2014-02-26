package de.is24.util.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.Set;


public class JMXTestHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(JMXTestHelper.class);
  private static final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();


  public static boolean checkInApplicationMonitorJMXBeanRegistered() {
    return checkInApplicationMonitorJMXBeanRegistered("is24");
  }


  public static boolean checkInApplicationMonitorJMXBeanRegistered(String domain) {
    try {
      ObjectName inAppJMXName = new ObjectName(domain + ":name=InApplicationMonitor");

      Set<ObjectName> objectNames = platformMBeanServer.queryNames(inAppJMXName, null);
      LOGGER.info("checkInApplicationMonitorJMXBeanRegistered found " + objectNames.size() + " items");
      return objectNames.size() == 1;
    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }

  }

  public static MBeanInfo getInApplicationMonitorMBeanInfo() {
    try {
      ObjectName inAppJMXName = new ObjectName("is24:name=InApplicationMonitor");

      MBeanInfo info = platformMBeanServer.getMBeanInfo(inAppJMXName);
      return info;

    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }

  }

  public static Long getTimerValue(String timerName, String attribute) {
    try {
      ObjectName objectName = new ObjectName("is24:type=InApplicationMonitor,name=" + timerName);

      Long result = (Long) platformMBeanServer.getAttribute(objectName, attribute);
      return result;

    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }

  }

  public static Long getCounterValue(String domain, String counterName) {
    try {
      ObjectName objectName = new ObjectName(domain + ":name=InApplicationMonitor");

      Long result = (Long) platformMBeanServer.getAttribute(objectName, counterName);
      return result;

    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }

  }


  public static MBeanInfo getTimerMBeanInfo(String timerKey) {
    try {
      ObjectName inAppJMXName = new ObjectName("is24:type=InApplicationMonitor,name=" + timerKey);

      MBeanInfo info = platformMBeanServer.getMBeanInfo(inAppJMXName);
      return info;
    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }

  }


  public static Object invoke(ObjectName objectName, Object[] params, String[] signature, String operationName)
                       throws InstanceNotFoundException, MBeanException, ReflectionException {
    Object result = platformMBeanServer.invoke(objectName, operationName, params, signature);
    return result;
  }
}
