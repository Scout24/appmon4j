package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.JmxAppMon4JNamingStrategy;
import de.is24.util.monitoring.keyhandler.DefaultKeyEscaper;


public class TestHelper {
  public static InApplicationMonitor setInstanceForTesting() {
    resetInstanceForTesting();

    DefaultKeyEscaper keyEscaper = new DefaultKeyEscaper();
    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "lala";
        }
      }, keyEscaper);
    return InApplicationMonitor.initInstanceForTesting(corePlugin, keyEscaper);
  }

  public static void resetInstanceForTesting() {
    InApplicationMonitor.resetInstanceForTesting();
  }
}
