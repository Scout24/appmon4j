package de.is24.util.monitoring.statsd;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class StatsdPluginTest {
  private StatsdClient client;
  private StatsdPlugin target;

  @Before
  public void setUp() throws Exception {
    client = mock(StatsdClient.class);
    target = new StatsdPlugin(client, "StatdsClientMock");
  }

  @Test
  public void sanitizeColon() {
    target.incrementCounter("test:Increment", 23);
    verify(client, times(1)).increment("test_Increment", 23, 1.0);
  }


  @Test
  public void shouldDelegateIncrementMethod() {
    target.incrementCounter("testIncrement", 23);
    verify(client, times(1)).increment("testIncrement", 23, 1.0);
  }

  @Test
  public void useHighRateSamplerateOnHighRateCounters() throws Exception {
    target.incrementHighRateCounter("testIncrement", 23);
    verify(client, times(1)).increment("testIncrement", 23, 0.1);
  }

  @Test
  public void shouldDelegateTimingMethod() {
    target.addTimerMeasurement("testTiming", 42);
    verify(client, times(1)).timing("testTiming", 42, 1.0);
  }

  @Test
  public void shouldNotDelegateSingleEventTimingMethod() throws Exception {
    target.addSingleEventTimerMeasurement("testSingleEventTiming", 1000);
    verify(client, never()).timing(anyString(), anyInt());
    verify(client, never()).timing(anyString(), anyInt(), anyDouble());
  }

  @Test
  public void useHighRateSamplerateOnHighRateTimingEvent() throws Exception {
    target.addHighRateTimerMeasurement("testHighRateTiming", 42);
    verify(client, times(1)).timing("testHighRateTiming", 42, 0.1);
  }
}
