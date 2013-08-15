package de.is24.util.monitoring.jmx;

import de.is24.util.monitoring.MultiValueProvider;
import de.is24.util.monitoring.ReportVisitor;
import de.is24.util.monitoring.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(JMXExporter.class);
  private static final String JMXEXPORTER = "JMXExporter";
  private final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

  private final ObjectName objectPattern;
  private final String pattern;

  private String name;

  /**
  * Initialize Exporter for a given ObjectName pattern. The pattern must be a valid object name.
  *
  * @param pattern The JMX domain.
  * @throws MalformedObjectNameException in case of an invalid pattern
  */
  public JMXExporter(String pattern) throws MalformedObjectNameException {
    this.pattern = pattern;
    this.objectPattern = new ObjectName(pattern);

    name = JMXEXPORTER + "." + pattern;
  }

  @Override
  public Collection<State> getValues() {
    List<State> result = new ArrayList<State>();
    searchAndLogNumericAttributes(result);
    return result;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void accept(ReportVisitor visitor) {
    //visitor.reportMultiValue(this);
  }


  protected void searchAndLogNumericAttributes(List<State> result) {
    Map<ObjectName, MBeanInfo> mBeanInfoMap = getMBeanInfos();
    for (Map.Entry<ObjectName, MBeanInfo> entry : mBeanInfoMap.entrySet()) {
      ObjectName name = entry.getKey();
      MBeanInfo mBeanInfo = entry.getValue();
      MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
      for (MBeanAttributeInfo info : attributes) {
        try {
          Object valueObject = platformMBeanServer.getAttribute(name, info.getName());
          String attributeName = getAttributeName(name, info);
          handleObject(attributeName, null, valueObject, result);
        } catch (Exception e) {
          LOGGER.info("Error accessing numeric MBean Attribute {} {} {}", name, info, e.getMessage());
        }
      }
    }
  }

  private void handleObject(String attributeName, String path, Object valueObject, List<State> result) {
    LOGGER.debug("handling {}", attributeName);

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

    return new State(JMXEXPORTER, fullName, value);
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
      Set<ObjectName> objectNames = platformMBeanServer.queryNames(objectPattern, null);
      Map<ObjectName, MBeanInfo> result = new HashMap<ObjectName, MBeanInfo>(objectNames.size());
      for (ObjectName name : objectNames) {
        result.put(name, platformMBeanServer.getMBeanInfo(name));
      }

      LOGGER.info("searching for MBeans matching pattern  {} found {} Bean Infos", pattern, result.size());
      return result;
    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }

  }


}
