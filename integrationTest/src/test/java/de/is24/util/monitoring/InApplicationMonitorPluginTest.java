package de.is24.util.monitoring;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class InApplicationMonitorPluginTest {
  @Rule
  public final InApplicationMonitorRule inApplicationMonitorRule = new InApplicationMonitorRule();
  private MonitorPlugin plugin;
  private InApplicationMonitor monitor;

  @Before
  public void setUp() throws Exception {
    plugin = mock(AbstractMonitorPlugin.class);
    monitor = inApplicationMonitorRule.getInApplicationMonitor();
  }

  @After
  public void tearDown() {
    InApplicationMonitor.getInstance().removeAllPlugins();
  }

  @Test
  public void shouldIncrementCounterInPlugin() {
    plugin.incrementCounter(anyString(), anyInt());

    monitor.registerPlugin(plugin);
    monitor.incrementCounter("test", 42);

    verify(plugin, times(1)).incrementCounter("test", 42);
  }

  @Test
  public void shouldIncrementCounterBy1OnSimpleIncrementInPlugin() {
    plugin.incrementCounter(anyString(), anyInt());

    monitor.registerPlugin(plugin);
    monitor.incrementCounter("simpleIncrementTest");

    verify(plugin, times(1)).incrementCounter("simpleIncrementTest", 1);
  }

  @Test
  public void shouldAddTimerMeasurementInPlugin() {
    plugin.addTimerMeasurement(anyString(), anyInt());

    monitor.registerPlugin(plugin);
    monitor.addTimerMeasurement("timerTest", 42);

    verify(plugin, times(1)).addTimerMeasurement("timerTest", 42);
  }
}
