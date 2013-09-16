package de.is24.util.monitoring.statsd;

import org.junit.Test;
import static org.mockito.Mockito.mock;


public class SaveStatsdPluginWrapperTest {
  @Test
  public void unknownHostDoesNotFail() {
    new SaveStatsdPluginWrapper("host.not.known", 8125, "lala");
  }

  @Test
  public void unknownHostWithSampleRateDoesNotFail() {
    new SaveStatsdPluginWrapper("host.not.known", 8125, "lala", 0.3);
  }

  @Test
  public void unknownHostWithFormatterDoesNotFail() {
    StatsdMessageFormatter messageFormatter = mock(StatsdMessageFormatter.class);
    new SaveStatsdPluginWrapper("host.not.known", 8125, messageFormatter);
  }

  @Test
  public void unknownHostWithFormatterAndSampleRateDoesNotFail() {
    StatsdMessageFormatter messageFormatter = mock(StatsdMessageFormatter.class);
    new SaveStatsdPluginWrapper("host.not.known", 8125, 0.3, messageFormatter);
  }
}
