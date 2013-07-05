package de.is24.util.monitoring.statsd;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.InApplicationMonitorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class InApplicationMonitorWithStatsdTest {
  @Rule
  public final InApplicationMonitorRule inApplicationMonitorRule = new InApplicationMonitorRule();
  private InApplicationMonitor monitor;
  private StatsdClient statsdClient;

  @Before
  public void setup() {
    monitor = inApplicationMonitorRule.getInApplicationMonitor();
    statsdClient = Mockito.mock(StatsdClient.class);
    monitor.registerPlugin(new StatsdPlugin(statsdClient, "StatsdPluginMock", 1.0));
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
    monitor.registerPlugin(new StatsdPlugin(statsdClient, "StatsdPluginMock", 1.0));

    when(statsdClient.increment(anyString(), anyInt())).thenReturn(true);
    monitor.incrementCounter("testIncrement");
    verify(statsdClient, times(1)).increment("testIncrement", 1, 1.0);
  }

  @Test
  public void shouldKeepFirstPluginOnDuplicateAdds() {
    StatsdClient secondStatsdClient = Mockito.mock(StatsdClient.class);

    // register another client with same unique name
    monitor.registerPlugin(new StatsdPlugin(secondStatsdClient, "StatsdPluginMock", 1.0));

    when(statsdClient.increment(anyString(), anyInt())).thenReturn(true);
    monitor.incrementCounter("testIncrement");

    verify(secondStatsdClient, times(0)).increment("testIncrement", 1, 1.0);
  }

}
