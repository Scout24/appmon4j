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
  public void explicitInitializationShouldTakeOverInitializedCountersAndTimers() {
    InApplicationMonitor.getInstance().initializeCounter(INITIALIZED_COUNTER_1);

    DefaultKeyEscaper keyEscaper = new DefaultKeyEscaper();
    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "lala";
        }
      }, keyEscaper);
    InApplicationMonitor explicitInitializedInApplicationMonitor = InApplicationMonitor.initInstance(
      corePlugin, keyEscaper);

    CheckForCounterExistenceReportVisitor reportVisitor = new CheckForCounterExistenceReportVisitor(
      INITIALIZED_COUNTER_1);
    assertThat(explicitInitializedInApplicationMonitor.getCorePlugin()).isSameAs(corePlugin);
    explicitInitializedInApplicationMonitor.getCorePlugin().reportInto(reportVisitor);
    assertThat(reportVisitor.found).isTrue();
    assertThat(JMXTestHelper.checkInApplicationMonitorJMXBeanRegistered("lala")).isEqualTo(true);

    assertThat(JMXTestHelper.getCounterValue("lala", INITIALIZED_COUNTER_1)).isEqualTo(0);
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
