package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.JmxAppMon4JNamingStrategy;


public class TestHelper {
  public static InApplicationMonitor setInstanceForTesting() {
    DefaultKeyEscaper keyEscaper = new DefaultKeyEscaper();
    return InApplicationMonitor.initInstanceForTesting(new CorePlugin(new JmxAppMon4JNamingStrategy() {
          @Override
          public String getJmxPrefix() {
            return "lala";
          }
        }, keyEscaper), keyEscaper);
  }

  public static void resetInstanceForTesting() {
    InApplicationMonitor.resetInstance();
  }
}
