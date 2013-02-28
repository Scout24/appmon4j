package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.InApplicationMonitorJMXConnector;
import de.is24.util.monitoring.jmx.JmxAppMon4JNamingStrategy;
import de.is24.util.monitoring.state2graphite.StateValuesToGraphite;
import org.apache.log4j.Logger;


public class StandAloneSimpleApp {
  private static final Logger LOGGER = Logger.getLogger(StandAloneSimpleApp.class);

  public static void main(String[] args) {
    InApplicationMonitor instance = InApplicationMonitor.getInstance();
    new InApplicationMonitorJMXConnector(InApplicationMonitor.getInstance().getCorePlugin(), "is24");

    new StateValuesToGraphite("devgrp01.be.test.is24.loc", 2003, "appmon4jTest");
    instance.incrementCounter("bla.bli.blu.lala");
    while (true) {
      try {
        instance.incrementCounter("bla.bli.blu.lala");
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        LOGGER.warn("oops , e");
      }
    }
  }
}
