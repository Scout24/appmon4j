package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.JmxAppMon4JNamingStrategy;
import de.is24.util.monitoring.keyhandler.DefaultKeyEscaper;
import de.is24.util.monitoring.tools.DoNothingReportVisitor;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import static de.is24.util.monitoring.TestHelper.initializeWithJMXNaming;
import static org.fest.assertions.Assertions.assertThat;


public class InApplicationMonitorTest {
  private static final String INITIALIZED_COUNTER_1 = "initialized.counter.1";

  @Before
  public void setupClass() {
    TestingInApplicationMonitor.resetInstanceForTesting();
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

  @Test
  public void allowThreadLocalStateValuesForTesting() throws Exception {
    final String threadTestGlobalValue = "threadTestGlobalValue";
    ExecutorService executorService = new ScheduledThreadPoolExecutor(1);

    try {
      // This future represents another thread registering a State Value after the parent thread
      // set itself to use the thread local state, and check that it all the time sees the right
      // state value while the parent thread does his testing.
      Future<Boolean> task = executorService.submit(new Callable<Boolean>() {
          @Override
          public Boolean call() throws Exception {
            boolean result;
            try {
              //wait until parent thread has set up thread local State
              Thread.sleep(10);

              // than register stateValue with same name
              InApplicationMonitor.getInstance()
              .registerStateValue(new SimpleStateValueProvider(threadTestGlobalValue, 11L));

              result = InApplicationMonitor.getInstance()
                .getCorePlugin()
                .getStateValue(threadTestGlobalValue)
                .getValue() == 11L;

              Thread.sleep(20);
            } catch (InterruptedException e) {
              result = false;
            }

            // and check again after reset
            result = result &&
              (InApplicationMonitor.getInstance().getCorePlugin().getStateValue(threadTestGlobalValue).getValue() ==
                11L);
            return result;
          }
        });

      // switch this thread to local State, and register the same state value as the future Task thread will do
      InApplicationMonitor.getInstance().setThreadLocalState();
      InApplicationMonitor.getInstance().registerStateValue(new SimpleStateValueProvider(threadTestGlobalValue, 110L));

      // wait until future thread (hopefully) has registered his state value
      Thread.sleep(15);

      // check that we see our local state value
      assertThat(InApplicationMonitor.getInstance().getCorePlugin().getStateValue(threadTestGlobalValue).getValue())
      .isEqualTo(110L);

      InApplicationMonitor.getInstance().resetThreadLocalState();

      assertThat(task.get()).isTrue();
    } finally {
      InApplicationMonitor.getInstance().resetThreadLocalState();
    }

  }

  @Test
  public void resetInstanceInOtherThreadShouldNotAffectThreadLocalStateValues() throws Exception {
    final String threadTestGlobalValue = "threadTestGlobalValue";
    String threadTestLocalValue = "threadTestLocalValue";

    try {
      InApplicationMonitor.getInstance().setThreadLocalState();
      InApplicationMonitor.getInstance().registerStateValue(new SimpleStateValueProvider(threadTestGlobalValue, 110L));
      InApplicationMonitor.getInstance().registerStateValue(new SimpleStateValueProvider(threadTestLocalValue, 1100L));

      Thread thread = new Thread() {
        @Override
        public void run() {
          TestingInApplicationMonitor.resetInstanceForTesting();
        }
      };
      thread.start();


      Thread.sleep(10);

      assertThat(InApplicationMonitor.getInstance().getCorePlugin().getStateValue(threadTestGlobalValue).getValue())
      .isEqualTo(110L);
    } finally {
      InApplicationMonitor.getInstance().resetThreadLocalState();
    }

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
