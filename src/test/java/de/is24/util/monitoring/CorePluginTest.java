package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.JmxAppMon4JNamingStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;


public class CorePluginTest {
  @Before
  public void setup() {
    InApplicationMonitor.resetInstanceForTesting();
  }

  @After
  public void tearDown() {
    InApplicationMonitor.resetInstanceForTesting();
  }


  @Test
  public void doNotInitializeJMXStuffIfNoNamingProvided() throws Exception {
    CorePlugin corePlugin = new CorePlugin(null, null);
    assertThat(JMXTestHelper.checkInApplicationMonitorJMXBeanRegistered()).isEqualTo(false);
  }

  @Test
  public void initializeJMXStuffIfNamingProvided() throws Exception {
    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "lala";
        }
      }, null);
    assertThat(JMXTestHelper.checkInApplicationMonitorJMXBeanRegistered("lala")).isEqualTo(true);
    corePlugin.destroy();
  }

  @Test
  public void countEvents() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String counterKey = "test.countEvents";
    corePlugin.incrementCounter(counterKey, 1);

    Counter counter = corePlugin.getCounter(counterKey);
    assertThat(counter.getCount()).isEqualTo(1L);
  }

  @Test
  public void doNotHandleHighRateCounterDifferently() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String counterKey = "test.highRateCounter";
    corePlugin.incrementHighRateCounter(counterKey, 1);

    Counter counter = corePlugin.getCounter(counterKey);
    assertThat(counter.getCount()).isEqualTo(1L);
  }


  @Test
  public void allowCounterInitialization() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String counterKey = "test.initializeCounter";
    corePlugin.initializeCounter(counterKey);

    Counter counter = corePlugin.getCounter(counterKey);
    assertThat(counter.getCount()).isEqualTo(0L);
  }

  @Test
  public void timeEvents() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String timerKey = "test.timeEvents";
    corePlugin.addTimerMeasurement(timerKey, 10);

    Timer timer = corePlugin.getTimer(timerKey);
    assertThat(timer.getCount()).isEqualTo(1L);
    assertThat(timer.getTimerSum()).isEqualTo(10L);
  }

  @Test
  public void doNotHandleHighRateTimerDifferently() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String timerKey = "test.highRateTimer";
    corePlugin.addHighRateTimerMeasurement(timerKey, 10);

    Timer timer = corePlugin.getTimer(timerKey);
    assertThat(timer.getCount()).isEqualTo(1L);
    assertThat(timer.getTimerSum()).isEqualTo(10L);
  }

  @Test
  public void doNotHandleSingleEventTimerDifferently() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String timerKey = "test.singleEventTimer";
    corePlugin.addSingleEventTimerMeasurement(timerKey, 10);

    Timer timer = corePlugin.getTimer(timerKey);
    assertThat(timer.getCount()).isEqualTo(1L);
    assertThat(timer.getTimerSum()).isEqualTo(10L);
  }

  @Test
  public void allowTimerInitialization() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String timerKey = "test.timeInitialization";
    corePlugin.initializeTimerMeasurement(timerKey);

    Timer timer = corePlugin.getTimer(timerKey);
    assertThat(timer.getCount()).isEqualTo(0L);
    assertThat(timer.getTimerSum()).isEqualTo(0L);
  }


  private CorePlugin givenCounterPluginWithoutJMX() {
    return new CorePlugin(null, null);
  }
}
