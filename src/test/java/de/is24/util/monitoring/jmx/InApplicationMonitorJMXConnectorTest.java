package de.is24.util.monitoring.jmx;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.JMXTestHelper;
import de.is24.util.monitoring.TestingInApplicationMonitor;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.util.List;
import static de.is24.util.monitoring.TestHelper.initializeWithJMXNaming;
import static org.fest.assertions.Assertions.assertThat;


public class InApplicationMonitorJMXConnectorTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(InApplicationMonitorJMXConnectorTest.class);

  @Before
  public void setupClass() {
    TestingInApplicationMonitor.resetInstanceForTesting();
  }

  @Test
  public void callingAddStateValuesToGraphiteJMXMethodAddsThePlugin() {
    // given a default configured InApplication Monitor
    InApplicationMonitor.getInstance();
    initializeWithJMXNaming();

    // when calling JMX Operation addStateValuesToGraphite
    callAddStateValuesToGraphite("lala", "host", 2003, "name");

    // then
    List<String> registeredReportableObservers = InApplicationMonitor.getInstance()
      .getCorePlugin()
      .getRegisteredReportableObservers();
    String observerName = registeredReportableObservers.get(registeredReportableObservers.size() - 1);
    assertThat(observerName).isEqualTo("StateValuesToGraphite:GraphiteConection:host,2003");
  }

  @Test
  public void addJMXExporterPattern() throws InstanceNotFoundException, ReflectionException, MBeanException,
                                             MalformedObjectNameException {
    Object[] params = {};

    String[] signature = {};

    // given a default configured InApplication Monitor
    InApplicationMonitor.getInstance();
    initializeWithJMXNaming();

    Object result = callMethodOnJMXConnector("lala", "listJmxExporterPattern", params, signature);
    assertThat(result).isNotNull();
    assertThat(((List) result).size()).isEqualTo(0);

    // when calling JMX Operation addJmxExporter
    callAddJMXExporter("lala", "java.lang:*");

    // then
    result = callMethodOnJMXConnector("lala", "listJmxExporterPattern", params, signature);
    assertThat(result).isNotNull();
    assertThat(((List) result).size()).isEqualTo(1);
  }

  public static void callAddStateValuesToGraphite(String domain, String host, int port, String name) {
    try {
      Object[] params = {
        host,
        Integer.valueOf(port),
        name
      };

      String[] signature = {
        String.class.getName(),
        Integer.class.getName(),
        String.class.getName()
      };


      Object result = callMethodOnJMXConnector(domain, "addStateValuesToGraphite", params, signature);
      LOGGER.info(result + "");

    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }
  }

  public static Object callMethodOnJMXConnector(String domain, String method, Object[] params,
                                                String[] signature) throws InstanceNotFoundException,
                                                                           ReflectionException, MBeanException,
                                                                           MalformedObjectNameException {
    ObjectName objectName = new ObjectName(domain + ":name=InApplicationMonitor");

    return JMXTestHelper.invoke(objectName, params, signature, method);

  }

  public static void callAddJMXExporter(String domain, String exportDomain) {
    try {
      ObjectName objectName = new ObjectName(domain + ":name=InApplicationMonitor");
      Object[] params = { exportDomain };

      String[] signature = { String.class.getName() };


      Object result = JMXTestHelper.invoke(objectName, params, signature, "addJmxExporterPattern");
      LOGGER.info(result + "");

    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }
  }


}
