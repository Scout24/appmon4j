package de.is24.util.monitoring;

import org.apache.log4j.Logger;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Set;


public class JMXTestHelper {
  private static final Logger LOGGER = Logger.getLogger(JMXTestHelper.class);

  public static boolean checkInApplicationMonitorJMXBeanRegistered() {
    return checkInApplicationMonitorJMXBeanRegistered("is24");
  }


  public static boolean checkInApplicationMonitorJMXBeanRegistered(String domain) {
    try {
      MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
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
      MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
      ObjectName inAppJMXName = new ObjectName("is24:name=InApplicationMonitor");

      Set<ObjectName> objectNames = platformMBeanServer.queryNames(inAppJMXName, null);
      Iterator<ObjectName> iterator = objectNames.iterator();

      if (iterator.hasNext()) {
        MBeanInfo info = platformMBeanServer.getMBeanInfo(iterator.next());
        return info;
      }
      throw new RuntimeException("wrong number of objects found " + objectNames.size());
    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }

  }

}
