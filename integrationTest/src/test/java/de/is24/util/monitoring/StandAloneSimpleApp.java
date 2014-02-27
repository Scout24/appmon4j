package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.JMXExporter;
import de.is24.util.monitoring.jmx.JmxAppMon4JNamingStrategy;
import de.is24.util.monitoring.keyhandler.DefaultKeyEscaper;
import de.is24.util.monitoring.state2graphite.StateValuesToGraphite;
import de.is24.util.monitoring.tools.GraphiteMockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.management.MalformedObjectNameException;


public class StandAloneSimpleApp {
  private static final Logger LOGGER = LoggerFactory.getLogger(StandAloneSimpleApp.class);

  public static void main(String[] args) {
    GraphiteMockServer graphiteMockServer = new GraphiteMockServer();
    try {
      graphiteMockServer.before();
    } catch (Throwable throwable) {
      LOGGER.error("oops", throwable);
    }

    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "is24";
        }
      }, new DefaultKeyEscaper());
    InApplicationMonitor instance = InApplicationMonitor.initInstance(corePlugin, new DefaultKeyEscaper());
    new StateValuesToGraphite("localhost", graphiteMockServer.getPort(), "lala");

    JMXExporter jmxExporter = new JMXExporter();
    corePlugin.registerMultiValueProvider(jmxExporter);

    jmxExporter.getValues();
    instance.incrementCounter("bla.bli.blu.lala");
    instance.addTimerMeasurement("lala", 10);

    JMXTestHelper.getTimerMBeanInfo("lala"); //throws exception if not found
    if (JMXTestHelper.getTimerValue("lala", "count").longValue() != 1) {
      LOGGER.error("wrong timer count in JMX");
      System.exit(1);
    }


    // we wil shutdown JMX connector after 60 seconds, check that beans are removed
    int i = 0;
    while (i < 30) {
      try {
        instance.incrementCounter("bla.bli.blu.lala");
        instance.addTimerMeasurement("lala", 10);
        Thread.sleep(2000);
        i++;
      } catch (InterruptedException e) {
        LOGGER.warn("oops , e");
      }
    }
    try {
      jmxExporter.addPattern("java.lang:*");
    } catch (MalformedObjectNameException e) {
      LOGGER.error("oops", e);
    }


    if (!JMXTestHelper.checkInApplicationMonitorJMXBeanRegistered()) {
      LOGGER.error("JMX Bean not registered");
      System.exit(1);
    }

    if (JMXTestHelper.getTimerMBeanInfo("lala") == null) {
      LOGGER.error("JMX Timer Info not found");
      System.exit(1);
    }

    if (JMXTestHelper.getTimerValue("lala", "count").longValue() != 31) {
      LOGGER.error("wrong timer count in JMX");
      System.exit(1);
    }


    /*    corePlugin.destroy();

        if (JMXTestHelper.checkInApplicationMonitorJMXBeanRegistered()) {
          LOGGER.error("JMX Bean still registered");
          System.exit(1);
        }
      */

    while (true) {
      try {
        instance.incrementCounter("bla.bli.blu.lala");
        instance.addTimerMeasurement("lala", 10);
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        LOGGER.warn("oops , e");
      }
    }
  }

}
