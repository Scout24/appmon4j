package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.JmxAppMon4JNamingStrategy;
import de.is24.util.monitoring.keyhandler.DefaultKeyEscaper;
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;


public class CorePluginTest {
  @Before
  public void setup() {
    TestingInApplicationMonitor.resetInstanceForTesting();
  }

  @After
  public void tearDown() {
    TestingInApplicationMonitor.resetInstanceForTesting();
  }


  @Test
  public void doNotInitializeJMXStuffIfNoNamingProvided() throws Exception {
    Assertions.assertThat(JMXTestHelper.checkInApplicationMonitorJMXBeanRegistered()).isEqualTo(false);
  }

  @Test
  public void initializeJMXStuffIfNamingProvided() throws Exception {
    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "lala";
        }
      }, null);
    Assertions.assertThat(JMXTestHelper.checkInApplicationMonitorJMXBeanRegistered("lala")).isEqualTo(true);
    corePlugin.destroy();
  }

  @Test
  public void countEvents() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String counterKey = "test.countEvents";
    corePlugin.incrementCounter(counterKey, 1);

    Counter counter = corePlugin.getCounter(counterKey);
    Assertions.assertThat(counter.getCount()).isEqualTo(1L);
  }

  @Test
  public void doNotHandleHighRateCounterDifferently() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String counterKey = "test.highRateCounter";
    corePlugin.incrementHighRateCounter(counterKey, 1);

    Counter counter = corePlugin.getCounter(counterKey);
    Assertions.assertThat(counter.getCount()).isEqualTo(1L);
  }


  @Test
  public void allowCounterInitialization() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String counterKey = "test.initializeCounter";
    corePlugin.initializeCounter(counterKey);

    Counter counter = corePlugin.getCounter(counterKey);
    Assertions.assertThat(counter.getCount()).isEqualTo(0L);
  }

  @Test
  public void timeEvents() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String timerKey = "test.timeEvents";
    corePlugin.addTimerMeasurement(timerKey, 10);

    Timer timer = corePlugin.getTimer(timerKey);
    Assertions.assertThat(timer.getCount()).isEqualTo(1L);
    Assertions.assertThat(timer.getTimerSum()).isEqualTo(10L);
  }

  @Test
  public void doNotHandleHighRateTimerDifferently() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String timerKey = "test.highRateTimer";
    corePlugin.addHighRateTimerMeasurement(timerKey, 10);

    Timer timer = corePlugin.getTimer(timerKey);
    Assertions.assertThat(timer.getCount()).isEqualTo(1L);
    Assertions.assertThat(timer.getTimerSum()).isEqualTo(10L);
  }

  @Test
  public void doNotHandleSingleEventTimerDifferently() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String timerKey = "test.singleEventTimer";
    corePlugin.addSingleEventTimerMeasurement(timerKey, 10);

    Timer timer = corePlugin.getTimer(timerKey);
    Assertions.assertThat(timer.getCount()).isEqualTo(1L);
    Assertions.assertThat(timer.getTimerSum()).isEqualTo(10L);
  }

  @Test
  public void allowTimerInitialization() throws Exception {
    CorePlugin corePlugin = givenCounterPluginWithoutJMX();
    String timerKey = "test.timeInitialization";
    corePlugin.initializeTimerMeasurement(timerKey);

    Timer timer = corePlugin.getTimer(timerKey);
    Assertions.assertThat(timer.getCount()).isEqualTo(0L);
    Assertions.assertThat(timer.getTimerSum()).isEqualTo(0L);
  }


  @Test
  public void defaultInitializedCorePluginNotEqualToExplicitInitialized() {
    CorePlugin defaultCorePlugin = InApplicationMonitor.getInstance().getCorePlugin();


    DefaultKeyEscaper keyEscaper = new DefaultKeyEscaper();
    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "lala";
        }
      }, keyEscaper);

    Assertions.assertThat(corePlugin).isNotEqualTo(defaultCorePlugin);
    corePlugin.destroy();
  }

  @Test
  public void syncReportableObserverShouldBeDiscardedAfterGC() throws InterruptedException {
    InApplicationMonitor.getInstance().incrementCounter("lalala");


    DefaultKeyEscaper keyEscaper = new DefaultKeyEscaper();
    CorePlugin corePlugin = new CorePlugin(new JmxAppMon4JNamingStrategy() {
        @Override
        public String getJmxPrefix() {
          return "lala";
        }
      }, keyEscaper);
    InApplicationMonitor.initInstance(corePlugin, keyEscaper);
    Thread.sleep(100);
    System.gc();
    assertThat(corePlugin.syncObserverReference.get() != null);


    Thread.sleep(100);
    System.gc();
    Thread.sleep(100);
    System.gc();
    assertThat(corePlugin.syncObserverReference.get() == null);

  }


  private CorePlugin givenCounterPluginWithoutJMX() {
    return new CorePlugin(null, null);
  }
}
