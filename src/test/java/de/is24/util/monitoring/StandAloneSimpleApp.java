package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.InApplicationMonitorJMXConnector;
import org.apache.log4j.Logger;


public class StandAloneSimpleApp {
  private static final Logger LOGGER = Logger.getLogger(StandAloneSimpleApp.class);

  public static void main(String[] args) {
    InApplicationMonitor instance = InApplicationMonitor.getInstance();
    InApplicationMonitorJMXConnector jmxConnector = new InApplicationMonitorJMXConnector(InApplicationMonitor
      .getInstance().getCorePlugin(), "is24");

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

    jmxConnector.shutdown();

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
