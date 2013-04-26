package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.JmxAppMon4JNamingStrategy;
import de.is24.util.monitoring.keyhandler.DefaultKeyEscaper;
import org.apache.log4j.Logger;
import javax.management.MBeanInfo;


public class StandAloneSimpleApp {
  private static final Logger LOGGER = Logger.getLogger(StandAloneSimpleApp.class);

  public static void main(String[] args) {
    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "is24";
        }
      }, new DefaultKeyEscaper());
    InApplicationMonitor instance = InApplicationMonitor.initInstance(corePlugin, new DefaultKeyEscaper());

    instance.incrementCounter("bla.bli.blu.lala");
    instance.addTimerMeasurement("lala", 10);

    MBeanInfo timerMBeanInfo = JMXTestHelper.getTimerMBeanInfo("lala"); //throws exception if not found
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
        Thread.sleep(20);
        i++;
      } catch (InterruptedException e) {
        LOGGER.warn("oops , e");
      }
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
