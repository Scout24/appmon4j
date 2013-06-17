package de.is24.util.monitoring.jmx;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.JMXTestHelper;
import de.is24.util.monitoring.TestingInApplicationMonitor;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import javax.management.ObjectName;
import java.util.List;
import static de.is24.util.monitoring.TestHelper.initializeWithJMXNaming;
import static org.fest.assertions.Assertions.assertThat;


public class InApplicationMonitorJMXConnectorTest {
  private static final Logger LOGGER = Logger.getLogger(InApplicationMonitorJMXConnectorTest.class);

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

  public static void callAddStateValuesToGraphite(String domain, String host, int i, String name) {
    try {
      ObjectName objectName = new ObjectName(domain + ":name=InApplicationMonitor");
      Object[] params = {
        host,
        Integer.valueOf(i),
        name
      };

      String[] signature = {
        String.class.getName(),
        Integer.class.getName(),
        String.class.getName()
      };


      Object result = JMXTestHelper.invoke(objectName, params, signature);
      LOGGER.info(result);

    } catch (Exception e) {
      LOGGER.warn("oops", e);
      throw new RuntimeException(e);
    }
  }


}
