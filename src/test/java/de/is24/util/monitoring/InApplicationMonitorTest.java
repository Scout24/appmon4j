package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.JmxAppMon4JNamingStrategy;
import de.is24.util.monitoring.keyhandler.DefaultKeyEscaper;
import de.is24.util.monitoring.tools.DoNothingReportVisitor;
import org.junit.Before;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;


public class InApplicationMonitorTest {
  private static final String INITIALIZED_COUNTER_1 = "initialized.counter.1";

  @Before
  public void setupClass() {
    InApplicationMonitor.resetInstanceForTesting();
  }


  @Test
  public void explicitInitializationShouldKeepInApplicationMonitorInstance() {
    // given a default configured InApplication Monitor
    InApplicationMonitor defaultInstance = InApplicationMonitor.getInstance();

    // when explicitly initializing InApplicationMonitor
    initializeWithJMXNaming();

    // then the new CorePlugin Instance is used
    assertThat(InApplicationMonitor.getInstance()).isSameAs(defaultInstance);
  }

  @Test
  public void explicitInitializationShouldSetCorePlugin() {
    // given a default configured InApplication Monitor with a counter added
    InApplicationMonitor.getInstance().initializeCounter(INITIALIZED_COUNTER_1);

    // when explicitly initializing InApplicationMonitor
    DefaultKeyEscaper keyEscaper = new DefaultKeyEscaper();
    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "lala";
        }
      }, keyEscaper);
    InApplicationMonitor explicitInitializedInApplicationMonitor = InApplicationMonitor.initInstance(
      corePlugin, keyEscaper);

    // then the new CorePlugin Instance is used
    assertThat(explicitInitializedInApplicationMonitor.getCorePlugin()).isSameAs(corePlugin);
  }


  @Test
  public void explicitInitializationShouldTakeOverInitializedCountersAndTimersIntoCorePlugin() {
    // given a default configured InApplication Monitor with a counter added
    InApplicationMonitor.getInstance().initializeCounter(INITIALIZED_COUNTER_1);

    // when explicitly initializing InApplicationMonitor
    CorePlugin corePlugin = initializeWithJMXNaming();

    // then
    CheckForCounterExistenceReportVisitor reportVisitor = new CheckForCounterExistenceReportVisitor(
      INITIALIZED_COUNTER_1);
    corePlugin.reportInto(reportVisitor);
    assertThat(reportVisitor.found).isTrue();

  }

  @Test
  public void explicitInitializationShouldTakeOverInitializedCountersAndTimersIntoJMX() {
    // given a default configured InApplication Monitor with a counter added
    InApplicationMonitor.getInstance().initializeCounter(INITIALIZED_COUNTER_1);

    // when explicitly initializing InApplicationMonitor
    initializeWithJMXNaming();

    // then
    assertThat(JMXTestHelper.getCounterValue("lala", INITIALIZED_COUNTER_1)).isEqualTo(0);
  }


  @Test
  public void explicitInitializationWithJMXNamingStrategyShouldInitializeJMX() {
    // given a default configured InApplication Monitor
    InApplicationMonitor.getInstance();

    // when explicitly initializing InApplicationMonitor
    initializeWithJMXNaming();

    // then
    assertThat(JMXTestHelper.checkInApplicationMonitorJMXBeanRegistered("lala")).isEqualTo(true);
  }


  private CorePlugin initializeWithJMXNaming() {
    DefaultKeyEscaper keyEscaper = new DefaultKeyEscaper();
    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "lala";
        }
      }, keyEscaper);
    InApplicationMonitor explicitInitializedInApplicationMonitor = InApplicationMonitor.initInstance(
      corePlugin, keyEscaper);
    return corePlugin;
  }


  private static class CheckForCounterExistenceReportVisitor extends DoNothingReportVisitor {
    private final String counterName;
    private boolean found = false;

    CheckForCounterExistenceReportVisitor(String counterName) {
      this.counterName = counterName;
    }

    @Override
    public void reportCounter(Counter counter) {
      if (counter.getName().equals(counterName)) {
        found = true;
      }
    }

  }
}
