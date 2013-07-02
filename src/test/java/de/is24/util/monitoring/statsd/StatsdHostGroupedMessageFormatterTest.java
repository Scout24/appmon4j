package de.is24.util.monitoring.statsd;

import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;


public class StatsdHostGroupedMessageFormatterTest {
  private StatsdMessageFormatter formatter = new StatsdHostGroupedMessageFormatter("test", "testHost");

  @Test
  public void testFormatSampledValue() throws Exception {
    String expected = "stat|@0.01|test.testHost";
    assertThat(formatter.formatSampledValue("stat", 0.01)).isEqualTo(expected);
  }

  @Test
  public void testFormatUnsampledValue() throws Exception {
    String expected = "stat||test.testHost";
    assertThat(formatter.formatUnsampledValue("stat")).isEqualTo(expected);
  }
}
