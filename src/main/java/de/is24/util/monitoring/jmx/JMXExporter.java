package de.is24.util.monitoring.jmx;

import de.is24.util.monitoring.MultiValueProvider;
import de.is24.util.monitoring.ReportVisitor;
import de.is24.util.monitoring.State;
import org.apache.log4j.Logger;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * JMXExporter exports all numeric attributes of a given JMX Domain.
 * You can use it to send metrics from other JMX beans to graphite.
 * You do not need it, and should not use it, to send appmon4j metrics, this should be done by
 * Statsd or Graphite plugins instead.
 */
public class JMXExporter implements MultiValueProvider {
  private static final Logger LOGGER = Logger.getLogger(JMXExporter.class);
  private final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

  private String domain;

  /**
   * Initialize Exporter for a given domain. The domain must not be empty.
   *
   * @param domain The JMX domain.
   * @throws IllegalArgumentException if domain is null or empty.
   */
  public JMXExporter(String domain) {
    if ((domain == null) || (domain.trim().length() == 0)) {
      throw new IllegalArgumentException("domain must not be empty");
    }
    this.domain = domain;
  }

  @Override
  public Collection<State> getValues() {
    List<State> result = new ArrayList<State>();
    searchAndLogNumericAttributes(result);
    return result;
  }

  @Override
  public String getName() {
    return "JMXExporter_" + domain;
  }

  @Override
  public void accept(ReportVisitor visitor) {
    //visitor.reportMultiValue(this);
  }


  protected void searchAndLogNumericAttributes(List<State> result) {
    Map<ObjectName, MBeanInfo> mBeanInfoMap = getMBeanInfos();
    for (ObjectName name : mBeanInfoMap.keySet()) {
      MBeanInfo mBeanInfo = mBeanInfoMap.get(name);
      MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
      for (MBeanAttributeInfo info : attributes) {
        try {
          Object valueObject = platformMBeanServer.getAttribute(name, info.getName());
          String attributeName = getAttributeName(name, info);
          handleObject(attributeName, null, valueObject, result);
        } catch (Exception e) {
          LOGGER.info("Error accessing numeric MBean Attribute " + name.toString() + " " + info.toString() +
            e.getMessage());
        }
      }
    }
  }

  private void handleObject(String attributeName, String path, Object valueObject, List<State> result) {
    LOGGER.debug("handling " + attributeName);

    Long value = null;
    if (valueObject instanceof Long) {
      value = ((Long) valueObject).longValue();
    } else if (valueObject instanceof Boolean) {
      value = ((Boolean) valueObject) ? 1L : 0L;
    } else if (valueObject instanceof Integer) {
      value = ((Integer) valueObject).longValue();
    } else if (valueObject instanceof Short) {
      value = ((Short) valueObject).longValue();
    } else if (valueObject instanceof Double) {
      value = ((Double) valueObject).longValue();
    } else if (valueObject instanceof Float) {
      value = ((Float) valueObject).longValue();
    } else if (valueObject instanceof CompositeData) {
      logComposite(attributeName, path, (CompositeData) valueObject, result);
    }
    if (value != null) {
      result.add(createState(attributeName, path, value));
    }
  }


  private void logComposite(String attributeName, String path, CompositeData obj, List<State> result) {
    for (String key : obj.getCompositeType().keySet()) {
      String additionalPath = (path == null) ? key : (path + "." + key);
      Object valueObject = obj.get(key);
      if (valueObject instanceof CompositeData) {
        logComposite(attributeName, additionalPath, (CompositeData) valueObject, result);
      } else if (valueObject != null) {
        handleObject(attributeName, additionalPath, valueObject, result);
      }
    }
  }

  private State createState(String attributeName, String path, Long value) {
    String fullName = attributeName + ((path != null) ? ("." + path) : "");

    //LOGGER.debug("adding state for " + fullName + " with value " + value);
    return new State(fullName, value);
  }

  private String getAttributeName(ObjectName name, MBeanAttributeInfo info) {
    StringBuilder builder = new StringBuilder();
    builder.append(name.getDomain())
    .append(".")
    .append(name.getCanonicalKeyPropertyListString())
    .append(".")
    .append(info.getName());
    return builder.toString().replaceAll("[:* =]", "_").replaceAll(",", ".");
  }


  protected Map<ObjectName, MBeanInfo> getMBeanInfos() {
    try {
      ObjectName inAppJMXName = new ObjectName(domain + ":*");

      Set<ObjectName> objectNames = platformMBeanServer.queryNames(inAppJMXName, null);
      Map<ObjectName, MBeanInfo> result = new HashMap<ObjectName, MBeanInfo>(objectNames.size());
      for (ObjectName name : objectNames) {
        result.put(name, platformMBeanServer.getMBeanInfo(name));
      }

      LOGGER.info("searching for MBeans in domain  " + domain + " found " + result.size() + " Bean Infos");
      return result;
    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }

  }


}
