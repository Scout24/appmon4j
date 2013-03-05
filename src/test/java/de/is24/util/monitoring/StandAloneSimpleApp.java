package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.JmxAppMon4JNamingStrategy;
import de.is24.util.monitoring.keyhandler.DefaultKeyEscaper;
import org.apache.log4j.Logger;


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

    corePlugin.destroy();

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
