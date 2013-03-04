package de.is24.util.monitoring.statsd;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;


public class InApplicationMonitorWithStatsdTest {
  private InApplicationMonitor monitor;
  private StatsdClient statsdClient;

  @BeforeClass
  public static void setupClass() {
    TestHelper.setInstanceForTesting();
  }

  @AfterClass
  public static void tearDownClass() {
    TestHelper.resetInstanceForTesting();
  }


  @Before
  public void setup() {
    monitor = InApplicationMonitor.getInstance();
    statsdClient = Mockito.mock(StatsdClient.class);
    monitor.registerPlugin(new StatsdPlugin(statsdClient, "StatsdPluginMock"));
  }

  @After
  public void tearDown() {
    InApplicationMonitor.getInstance().removeAllPlugins();
  }

  @Test
  public void shouldIncrementOnStatsd() {
    when(statsdClient.increment(anyString(), anyInt())).thenReturn(true);
    monitor.incrementCounter("testIncrement");
    verify(statsdClient, times(1)).increment("testIncrement", 1, 1.0);
  }

  @Test
  public void shouldNotAddPluginTwice() {
    // register client a second time with same unique name
    monitor.registerPlugin(new StatsdPlugin(statsdClient, "StatsdPluginMock"));

    when(statsdClient.increment(anyString(), anyInt())).thenReturn(true);
    monitor.incrementCounter("testIncrement");
    verify(statsdClient, times(1)).increment("testIncrement", 1, 1.0);
  }

  @Test
  public void shouldKeepFirstPluginOnDuplicateAdds() {
    StatsdClient secondStatsdClient = Mockito.mock(StatsdClient.class);

    // register another client with same unique name
    monitor.registerPlugin(new StatsdPlugin(secondStatsdClient, "StatsdPluginMock"));

    when(statsdClient.increment(anyString(), anyInt())).thenReturn(true);
    monitor.incrementCounter("testIncrement");

    verify(secondStatsdClient, times(0)).increment("testIncrement", 1, 1.0);
  }

}
