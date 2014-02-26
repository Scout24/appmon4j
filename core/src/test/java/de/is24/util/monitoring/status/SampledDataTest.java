package de.is24.util.monitoring.status;

import de.is24.util.monitoring.InApplicationMonitorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class SampledDataTest {
  @Rule
  public final InApplicationMonitorRule inApplicationMonitorRule = new InApplicationMonitorRule();
  SampledData sampledData;

  @Before
  public void setup() {
    sampledData = new SampledData(this.getClass().getName());
  }

  @Test
  public void returnZeroIfNoData() {
    sampledData.addSample(0, 0);
    assertThat(sampledData.getOnMinuteFailureRate(), is(0.0f));
  }

  @Test
  public void returnZeroIfNoFailureHappend() {
    sampledData.addSample(1, 0);
    assertThat(sampledData.getOnMinuteFailureRate(), is(0.0f));
  }

  @Test
  public void return50PercentIfOneErrorOneSuccess() {
    sampledData.addSample(1, 1);
    assertThat(sampledData.getOnMinuteFailureRate(), is(0.5f));
  }

  @Test
  public void takeLast5ValuesForFiveMinuteRate() {
    sampledData.addSample(0, 5); // out of window
    sampledData.addSample(1, 0);
    sampledData.addSample(1, 0);
    sampledData.addSample(1, 0);
    sampledData.addSample(1, 0);
    sampledData.addSample(1, 0);
    assertThat(sampledData.getFiveMinuteFailureRate(), is(0.0f));
  }

  @Test
  public void calculateFiveMinuteRate() {
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    sampledData.addSample(9, 1);
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    assertThat(sampledData.getFiveMinuteFailureRate(), is(0.02f));
  }

  @Test
  public void calculateFifteenMinuteRate() {
    sampledData.addSample(0, 70); // out of window
    sampledData.addSample(9, 1);
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    sampledData.addSample(9, 1);
    sampledData.addSample(10, 0);
    sampledData.addSample(10, 0);
    assertThat(sampledData.getFifteenMinuteFailureRate(), is((float) 2 / 150));
  }

}
