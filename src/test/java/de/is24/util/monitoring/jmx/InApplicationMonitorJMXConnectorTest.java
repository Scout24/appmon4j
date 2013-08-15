package de.is24.util.monitoring.jmx;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.JMXTestHelper;
import de.is24.util.monitoring.MultiValueProvider;
import de.is24.util.monitoring.TestingInApplicationMonitor;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.management.ObjectName;
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
  public void callingAddJmxExporterMethodAddsTheJMXExporter() {
    // given a default configured InApplication Monitor
    InApplicationMonitor.getInstance();
    initializeWithJMXNaming();

    // when calling JMX Operation addJmxExporter
    callAddJMXExporter("lala", "java.lang");

    // then
    MultiValueProvider multiValueProvider = InApplicationMonitor.getInstance()
      .getCorePlugin()
      .getMultiValueProvider("JMXExporter.java.lang");
    assertThat(multiValueProvider).isNotNull();
  }

  public static void callAddStateValuesToGraphite(String domain, String host, int port, String name) {
    try {
      ObjectName objectName = new ObjectName(domain + ":name=InApplicationMonitor");
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


      Object result = JMXTestHelper.invoke(objectName, params, signature, "addStateValuesToGraphite");
      LOGGER.info(result + "");

    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }
  }

  public static void callAddJMXExporter(String domain, String exportDomain) {
    try {
      ObjectName objectName = new ObjectName(domain + ":name=InApplicationMonitor");
      Object[] params = { exportDomain };

      String[] signature = { String.class.getName() };


      Object result = JMXTestHelper.invoke(objectName, params, signature, "addJmxExporter");
      LOGGER.info(result + "");

    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }
  }


}
