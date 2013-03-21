package de.is24.util.monitoring.jmx;

import de.is24.util.monitoring.CorePlugin;
import de.is24.util.monitoring.Counter;
import de.is24.util.monitoring.HistorizableList;
import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.Reportable;
import de.is24.util.monitoring.ReportableObserver;
import de.is24.util.monitoring.StateValueProvider;
import de.is24.util.monitoring.Timer;
import de.is24.util.monitoring.Version;
import de.is24.util.monitoring.state2graphite.StateValuesToGraphite;
import de.is24.util.monitoring.statsd.StatsdPlugin;
import de.is24.util.monitoring.visitors.HistogramLikeValueAnalysisVisitor;
import de.is24.util.monitoring.visitors.StringWriterReportVisitor;
import org.apache.log4j.Logger;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class publishes values registered at the Core Plugin as JMX MBeans and exposes
 * some management operations of InApplicationMonitor.
 * Simple values (Counter, Version, StateValue) are published directly via this class
 * as dynamic MBean, complex types are published using an own MBean for each Reportable.
 *
 */
public final class InApplicationMonitorJMXConnector implements DynamicMBean, ReportableObserver {
  private static final String DUMP_STRING_WRITER = "dumpStringWriter";

  private static final String DUMP_HISTOGRAM_LIKE_VALUE_ANALYSIS = "dumpHistogramLikeValueAnalysis";

  private static final String ACTIVATE = "activate";

  private static final String DEACTIVATE = "deactivate";

  private static final String IS_MONITOR_ACTIVE = "isMonitorActive";

  private static final String GET_REGISTERED_PLUGIN_KEYS = "getRegisteredPluginKeys";
  private static final String REMOVE_ALL_PLUGINS = "removeAllPlugins";
  private static final String ADD_STATSD_PLUGIN = "addStatsdPlugin";
  private static final String ADD_STATE_VALUES_TO_GRAPHITE = "addStateValuesToGraphite";

  private static final Logger LOG = Logger.getLogger(InApplicationMonitorJMXConnector.class);

  private static volatile InApplicationMonitorJMXConnector instance;
  private static final String semaphore = "InApplicationMonitorJMXConnectorSemaphore";

  private final Map<String, Reportable> reportables = new ConcurrentHashMap<String, Reportable>();

  private MBeanServer beanServer;
  private String jmxPrefix = "";

  private final CorePlugin corePlugin;


  public InApplicationMonitorJMXConnector(CorePlugin corePlugin, JmxAppMon4JNamingStrategy jmxAppMon4JNamingStrategy) {
    synchronized (semaphore) {
      LOG.info("initializing InApplicationMonitorJMXConnector");
      if (instance != null) {
        LOG.error("JMXConnector allready initialized, this is not allowed");
        throw new IllegalStateException("JMXConnector already initialized");
      }
      this.corePlugin = corePlugin;
      this.jmxPrefix = jmxAppMon4JNamingStrategy.getJmxPrefix() + ":";
      registerJMXStuff();

      // register yourself as ReportableObserver so that we're notified about every new reportable
      corePlugin.addReportableObserver(this);
      instance = this;
    }
  }

  public void shutdown() {
    synchronized (semaphore) {
      LOG.info("shutting down InApplicationMonitorJMXConnector ");
      corePlugin.removeReportableObserver(this);
      removeAllReportables();
      try {
        beanServer.unregisterMBean(new ObjectName(jmxPrefix + "name=InApplicationMonitor"));
      } catch (Exception e) {
        LOG.warn("problem when unregistering InApplicationMonitorJMXConnector during shutdown", e);
      }
      instance = null;
    }
  }

  /**
  * This method is called for each reportable that is registered on the InApplicationMonitor.
  * Basically, it checks if the JMX implementation is interested in the reportable and adds
  * it to the "reportables" map which is the base for both getAttribute(s) and getMBeanInfo().
  */
  public void addNewReportable(Reportable reportable) {
    // use intern string representation - so we can synchronize on it
    final String reportableKey = reportable.getName().intern();


    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (reportableKey) {
      boolean beanAlreadyRegistred = reportables.containsKey(reportableKey);
      reportables.put(reportableKey, reportable);

      // MBean for each reportable
      if ((reportable instanceof Timer) || (reportable instanceof HistorizableList)) {
        InApplicationMonitorDynamicMBean bean = new InApplicationMonitorDynamicMBean(reportable);
        try {
          if (beanAlreadyRegistred) {
            unregisterMBeanOnJMX(bean, reportableKey, "InApplicationMonitor");
          }
          registerMBeanOnJMX(bean, reportableKey, "InApplicationMonitor");
        } catch (Exception e) {
          LOG.error("could not register MBean for " + reportableKey, e);
        }
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("registered new reportable " + reportableKey + " for JMX");
      }
    }
  }

  public void removeAllReportables() {
    for (Reportable reportable : reportables.values()) {
      // use intern string representation - so we can synchronize on it
      final String reportableKey = reportable.getName().intern();

      //noinspection SynchronizationOnLocalVariableOrMethodParameter
      synchronized (reportableKey) {
        // MBean for each reportable
        if ((reportable instanceof Timer) || (reportable instanceof HistorizableList)) {
          try {
            unregisterMBeanOnJMX(reportable, reportableKey, "InApplicationMonitor");
          } catch (Exception e) {
            LOG.error("could not unregister MBean for " + reportableKey, e);
          }
        }
      }

    }
    reportables.clear();
  }


  /**
  * registers the InApplicationMonitor as JMX MBean on the running JMX
  * server - if no JMX server is running, one is started automagically.
  */
  private void registerJMXStuff() {
    LOG.info("registering InApplicationMonitorDynamicMBean on JMX server");

    beanServer = ManagementFactory.getPlatformMBeanServer();
    try {
      beanServer.registerMBean(this,
        new ObjectName(jmxPrefix + "name=InApplicationMonitor"));
    } catch (Exception e) {
      LOG.error("could not register MBean server : ", e);
    }
  }


  protected void registerMBeanOnJMX(Object object, String name, String type) throws InstanceAlreadyExistsException,
                                                                                    MBeanRegistrationException,
                                                                                    NotCompliantMBeanException,
                                                                                    MalformedObjectNameException {
    beanServer.registerMBean(object, createBeanName(name, type));
  }

  protected void unregisterMBeanOnJMX(Object object, String name, String type) throws InstanceNotFoundException,
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

  /* MBean methods */

  public MBeanInfo getMBeanInfo() {
    List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();

    for (Entry<String, Reportable> entry : reportables.entrySet()) {
      /* we do not handle the not-so-primitive data types like "Timer" and "HistorizableList"
       * because those get separate MBeans
      */
      if ((entry.getValue() instanceof Counter) ||
          (entry.getValue() instanceof StateValueProvider)) {
        attributes.add(new MBeanAttributeInfo(entry.getKey(), "long", entry.getKey(), true, false, false));
      } else if (entry.getValue() instanceof Version) {
        attributes.add(new MBeanAttributeInfo(entry.getKey(), "String", entry.getKey(), true, false, false));
      }
    }

    // now build the attribute list
    MBeanAttributeInfo[] beanAttributeInfos = new MBeanAttributeInfo[attributes.size()];

    if (LOG.isDebugEnabled()) {
      LOG.debug("getMBeanInfo returning " + attributes.size() + " reportables.");
    }

    for (int loop = 0; loop < attributes.size(); loop++) {
      beanAttributeInfos[loop] = attributes.get(loop);
    }

    // add operations
    MBeanOperationInfo[] beanOperationInfos = new MBeanOperationInfo[] {
      new MBeanOperationInfo(DUMP_STRING_WRITER, "", null, "String",
        MBeanOperationInfo.ACTION_INFO),
      new MBeanOperationInfo(DUMP_HISTOGRAM_LIKE_VALUE_ANALYSIS, "",
        new MBeanParameterInfo[] { new MBeanParameterInfo("base", "java.lang.String", "") }, "String",
        MBeanOperationInfo.ACTION_INFO),
      new MBeanOperationInfo(ACTIVATE, "enable monitoring", null, "void", MBeanOperationInfo.ACTION),
      new MBeanOperationInfo(DEACTIVATE, "disable monitoring", null, "void", MBeanOperationInfo.ACTION),
      new MBeanOperationInfo(IS_MONITOR_ACTIVE, "check if monitor is active", null, "java.lang.Boolean",
        MBeanOperationInfo.INFO),
      new MBeanOperationInfo(GET_REGISTERED_PLUGIN_KEYS, "list registered plugin keys", null, "java.util.List",
        MBeanOperationInfo.INFO),
      new MBeanOperationInfo(REMOVE_ALL_PLUGINS, "remove all plugins", null, "void",
        MBeanOperationInfo.ACTION),
      new MBeanOperationInfo(ADD_STATSD_PLUGIN, "",
        new MBeanParameterInfo[] {
          new MBeanParameterInfo("statsd hostname", "java.lang.String", ""),
          new MBeanParameterInfo("statsd port", "java.lang.Integer", ""),
          new MBeanParameterInfo("app name", "java.lang.String", ""),
          new MBeanParameterInfo("sample rate", "java.lang.Double", "")
        }, "void",
        MBeanOperationInfo.ACTION),
      new MBeanOperationInfo(ADD_STATE_VALUES_TO_GRAPHITE, "",
        new MBeanParameterInfo[] {
          new MBeanParameterInfo("graphite hostname", "java.lang.String", ""),
          new MBeanParameterInfo("graphite port", "java.lang.Integer", ""),
          new MBeanParameterInfo("app name", "java.lang.String", "")
        }, "void",
        MBeanOperationInfo.ACTION),
    };

    // assemble the MBean description

    return new MBeanInfo("de.is24.util.monitoring.InApplicationMonitorDynamicMBeanThing",
      "InApplication Monitor dynamic MBean",
      beanAttributeInfos,
      null,
      beanOperationInfos,
      null);
  }

  private Object getValueForReportable(String attribute) {
    LOG.debug("getting value for attribute " + attribute);

    Reportable reportable = reportables.get(attribute);

    if (reportable != null) {
      if (reportable instanceof Counter) {
        return ((Counter) reportable).getCount();
      } else if (reportable instanceof StateValueProvider) {
        return ((StateValueProvider) reportable).getValue();
      } else if (reportable instanceof Version) {
        return ((Version) reportable).getValue();
      }
    } else {
      LOG.warn("attribute " + attribute + " not found");
    }

    return null;
  }

  public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
    return getValueForReportable(attribute);
  }

  public AttributeList getAttributes(String[] attributes) {
    AttributeList attributeList = new AttributeList();

    for (Attribute attribute : attributeList.asList()) {
      attributeList.add(new Attribute(attribute.getName(), getValueForReportable(attribute.getName())));
    }

    return attributeList;
  }

  /* got no setters at the moment */

  public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
                                                       MBeanException, ReflectionException {
  }


  public AttributeList setAttributes(AttributeList attributes) {
    return null;
  }

  public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
                                                                                      ReflectionException {
    if (actionName.equals(DUMP_STRING_WRITER)) {
      return dumpStringWriter();
    } else if (actionName.equals(DUMP_HISTOGRAM_LIKE_VALUE_ANALYSIS)) {
      return dumpHistogramLikeValueAnalysis((String) params[0]);
    } else if (actionName.equals(ACTIVATE)) {
      activate();
    } else if (actionName.equals(DEACTIVATE)) {
      deactivate();
    } else if (actionName.equals(IS_MONITOR_ACTIVE)) {
      return isMonitorActive();
    } else if (actionName.equals(GET_REGISTERED_PLUGIN_KEYS)) {
      return getRegisteredPluginKeys();
    } else if (actionName.equals(REMOVE_ALL_PLUGINS)) {
      InApplicationMonitor.getInstance().removeAllPlugins();
    } else if (actionName.equals(ADD_STATSD_PLUGIN)) {
      String host = (String) params[0];
      Integer port = (Integer) params[1];
      String appName = (String) params[2];
      Double sampleRate = (Double) params[3];
      addStatsdPlugin(host, port, appName, sampleRate);
    } else if (actionName.equals(ADD_STATSD_PLUGIN)) {
      String host = (String) params[0];
      Integer port = (Integer) params[1];
      String appName = (String) params[2];
      addStateValuesToGraphite(host, port, appName);
    }
    return null;
  }

  /* operations that can be invoked via JMX */

  private void addStatsdPlugin(String host, Integer port, String appName, Double sampleRate) {
    StatsdPlugin statsdPlugin;
    try {
      statsdPlugin = new StatsdPlugin(host, port, appName, sampleRate);
      InApplicationMonitor.getInstance().registerPlugin(statsdPlugin);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void addStateValuesToGraphite(String host, Integer port, String appName) {
    try {
      new StateValuesToGraphite(host, port, appName);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public String dumpStringWriter() {
    StringWriterReportVisitor visitor = new StringWriterReportVisitor();
    corePlugin.reportInto(visitor);
    LOG.info(visitor.toString());
    return visitor.toString();
  }

  public String dumpHistogramLikeValueAnalysis(String base) {
    HistogramLikeValueAnalysisVisitor visitor = new HistogramLikeValueAnalysisVisitor(base);
    corePlugin.reportInto(visitor);
    LOG.info(visitor.toString());
    return visitor.toString();
  }

  public void activate() {
    InApplicationMonitor.getInstance().activate();
  }

  public void deactivate() {
    InApplicationMonitor.getInstance().deactivate();
  }

  public Boolean isMonitorActive() {
    return Boolean.valueOf(InApplicationMonitor.getInstance().isMonitorActive());
  }

  public List<String> getRegisteredPluginKeys() {
    return InApplicationMonitor.getInstance().getRegisteredPluginKeys();
  }
}
