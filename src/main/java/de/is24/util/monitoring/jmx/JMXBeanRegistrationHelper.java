package de.is24.util.monitoring.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;


public class JMXBeanRegistrationHelper {
  private MBeanServer beanServer;
  private final String jmxPrefix;

  private static final Logger LOG = LoggerFactory.getLogger(JMXBeanRegistrationHelper.class);

  public JMXBeanRegistrationHelper(JmxAppMon4JNamingStrategy jmxAppMon4JNamingStrategy) {
    this.jmxPrefix = jmxAppMon4JNamingStrategy.getJmxPrefix() + ":";
    beanServer = ManagementFactory.getPlatformMBeanServer();
  }


  protected void registerMBeanOnJMX(Object object, String name, String type) throws InstanceAlreadyExistsException,
                                                                                    MBeanRegistrationException,
                                                                                    NotCompliantMBeanException,
                                                                                    MalformedObjectNameException {
    beanServer.registerMBean(object, createBeanName(name, type));
  }

  protected void unregisterMBeanOnJMX(String name, String type) throws InstanceNotFoundException,
                                                                       MBeanRegistrationException,
                                                                       MalformedObjectNameException {
    ObjectName beanName = createBeanName(name, type);
    if (beanServer.isRegistered(beanName)) {
      beanServer.unregisterMBean(beanName);
    }
  }

  private ObjectName createBeanName(String name, String type) throws MalformedObjectNameException {
    StringBuilder buf = new StringBuilder(jmxPrefix);
    if (type != null) {
      buf.append("type=");
      buf.append(type);
      buf.append(",");
    }
    buf.append("name=");
    buf.append(name);
    return new ObjectName(buf.toString());
  }

}
