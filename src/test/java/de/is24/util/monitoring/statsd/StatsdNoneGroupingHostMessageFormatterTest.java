package de.is24.util.monitoring.statsd;

import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;


public class StatsdNoneGroupingHostMessageFormatterTest {
  private StatsdMessageFormatter formatter = new StatsdNoneGroupingHostMessageFormatter("test", "testHost");

  @Test
  public void testFormatSampledValue() throws Exception {
    String expected = "test.testHost.stat|@0.01";
    assertThat(formatter.formatSampledValue("stat", 0.01)).isEqualTo(expected);
  }

  @Test
  public void testFormatUnsampledValue() throws Exception {
    String expected = "test.testHost.stat|";
    assertThat(formatter.formatUnsampledValue("stat")).isEqualTo(expected);
  }
}
