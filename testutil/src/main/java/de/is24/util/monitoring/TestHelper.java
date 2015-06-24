package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.JmxAppMon4JNamingStrategy;
import de.is24.util.monitoring.keyhandler.DefaultKeyEscaper;


public class TestHelper {
  /**
   * initialize a TestingInApplicationMonitor with JMX Plugin
   * @return the TestingInApplicationMonitor instance
   */
  public static InApplicationMonitor setInstanceForTesting() {
    resetInstanceForTesting();

    DefaultKeyEscaper keyEscaper = new DefaultKeyEscaper();
    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "lala";
        }
      }, keyEscaper);
    return TestingInApplicationMonitor.initInstanceForTesting(corePlugin, keyEscaper);
  }

  /**
   * initialize a fresh TestingInApplicationMonitor without JMX Plugin as global InApplicationMonitor instance.
   * This method is not safe to use during multithreaded tests
   */
  public static void resetInstanceForTesting() {
    TestingInApplicationMonitor.resetInstanceForTesting();
  }

  public static CorePlugin initializeWithJMXNaming() {
    DefaultKeyEscaper keyEscaper = new DefaultKeyEscaper();
    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "lala";
        }
      }, keyEscaper);
    InApplicationMonitor.initInstance(corePlugin, keyEscaper);
    return corePlugin;
  }

}
