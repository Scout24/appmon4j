package de.is24.util.monitoring.jmx;

import de.is24.util.monitoring.MultiValueProvider;
import de.is24.util.monitoring.ReportVisitor;
import de.is24.util.monitoring.State;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

  private final Set<ObjectName> objectPatterns;


  /**
   * Initialize Exporter
   *
   */
  public JMXExporter() {
    objectPatterns = Collections.synchronizedSet(new HashSet<ObjectName>());
  }

  /**
  * Initialize Exporter for a given ObjectName pattern. The pattern must be a valid object name.
  *
  * @param pattern The JMX domain.
  * @throws MalformedObjectNameException in case of an invalid pattern
  */
  public JMXExporter(String pattern) throws MalformedObjectNameException {
    this();
    addPattern(pattern);
  }

  public void addPattern(String pattern) throws MalformedObjectNameException {
    objectPatterns.add(new ObjectName(pattern));
  }

  public List<ObjectName> listPatterns() {
    return new ArrayList<ObjectName>(objectPatterns);
  }

  public boolean removePattern(String pattern) throws MalformedObjectNameException {
    return objectPatterns.remove(new ObjectName(pattern));
  }

  @Override
  public Collection<State> getValues() {
    List<State> result = new ArrayList<State>();
    searchAndLogNumericAttributes(result);
    return result;
  }

  @Override
  public String getName() {
    return JMXEXPORTER;
  }

  @Override
  public void accept(ReportVisitor visitor) {
    visitor.reportMultiValue(this);
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
          String attributeName = getBaseName(name);
          handleObject(attributeName, info.getName(), valueObject, result);
        } catch (Exception e) {
          if ((e.getCause() != null) && e.getCause().getClass().equals(UnsupportedOperationException.class)) {
            LOGGER.debug("ignoring unsupported numeric MBean Attribute {} {} {}", name, info);
          } else {
            LOGGER.info("Error accessing numeric MBean Attribute {} {} {}", name, info, e.getMessage());
          }
        }
      }
    }
  }

  private void handleObject(String baseName, String path, Object valueObject, List<State> result) {
    LOGGER.debug("handling {}", baseName);

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
      logComposite(baseName, path, (CompositeData) valueObject, result);
    }
    if (value != null) {
      result.add(createState(baseName, path, value));
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

  private State createState(String baseName, String path, Long value) {
    return new State(baseName, path, value);
  }

  private String getBaseName(ObjectName name) {
    StringBuilder builder = new StringBuilder();
    builder.append(name.getDomain()).append(".").append(name.getCanonicalKeyPropertyListString());
    return builder.toString();
  }


  protected Map<ObjectName, MBeanInfo> getMBeanInfos() {
    try {
      Map<ObjectName, MBeanInfo> result = new HashMap<ObjectName, MBeanInfo>();
      for (ObjectName objectPattern : objectPatterns) {
        Set<ObjectName> objectNames = platformMBeanServer.queryNames(objectPattern, null);
        for (ObjectName name : objectNames) {
          result.put(name, platformMBeanServer.getMBeanInfo(name));
        }
      }

      LOGGER.debug("searching for MBeans using {} patterns found {} matching Bean Infos", objectPatterns.size(),
        result.size());
      return result;
    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }

  }

  public void readFromDirectory(String path) {
    LOGGER.info("reading JMXExporter Patterns from directory {}", path);

    File dir = new File(path);
    if (dir.exists() && dir.isDirectory()) {
      Collection<File> files = FileUtils.listFiles(dir, null, false);
      if (files.size() == 0) {
        LOGGER.warn("no files found in readFromDirectory {}", path);
      }
      for (File file : files) {
        readFromFile(file);
      }
    } else {
      LOGGER.warn("JMXExporter config dir {} does not exist.", dir.getAbsolutePath());
    }
  }

  public void readFromFile(String filename) {
    readFromFile(new File(filename));
  }

  public void readFromFile(File file) {
    LOGGER.info("reading JMXExporter Patterns from file {}", file.getAbsolutePath());

    LineIterator it;
    try {
      it = FileUtils.lineIterator(file, "UTF-8");
    } catch (IOException e) {
      LOGGER.warn("Error while reading patterns from file " + file.getAbsolutePath(), e);
      throw new RuntimeException(e);
    }

    try {
      while (it.hasNext()) {
        String pattern = it.nextLine();

        // an empty pattern would be translated to *:*, we do not want this default behaviour of ObjectName here.
        if (pattern.length() > 0) {
          try {
            addPattern(pattern);
          } catch (MalformedObjectNameException e) {
            LOGGER.warn("Ignoring malformed ObjectName pattern while applying pattern {} from file {} {} ", pattern,
              file.getAbsolutePath(), e.getMessage());
          }
        }
      }
    } finally {
      LineIterator.closeQuietly(it);
    }

  }
}
