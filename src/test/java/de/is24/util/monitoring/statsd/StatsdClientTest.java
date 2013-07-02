package de.is24.util.monitoring.statsd;

import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.util.Random;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class StatsdClientTest {
  private StatsdDatagrammSocket socket;
  private StatsdClient target;

  @Before
  public void setUp() throws Exception {
    socket = mock(StatsdDatagrammSocket.class);
    target = new StatsdClient(socket, new StatsdHostGroupedMessageFormatter("test", "testHost"));
  }

  @Test
  public void shouldSendTiming() throws IOException {
    target.timing("testTiming", 42);
    verify(socket, times(1)).send("testTiming:42|ms||test.testHost");
  }

  @Test
  public void shouldDecrement() throws IOException {
    target.decrement("testDecrement");
    verify(socket, times(1)).send("testDecrement:-1|c||test.testHost");

  }

  @Test
  public void shouldDecrementByMagnitude() throws IOException {
    target.decrement("testDecrement", 23);
    verify(socket, times(1)).send("testDecrement:-23|c||test.testHost");
  }

  @Test
  public void shouldDecrementMultipleKeys() throws IOException {
    target.decrement("testDecrement1", "testDecrement2");
    verify(socket, times(1)).send("testDecrement1:-1|c||test.testHost");
    verify(socket, times(1)).send("testDecrement2:-1|c||test.testHost");
  }

  @Test
  public void shouldDecrementMultipleKeysByMagnitude() throws IOException {
    target.decrement(19, "testDecrement1", "testDecrement2");
    verify(socket, times(1)).send("testDecrement1:-19|c||test.testHost");
    verify(socket, times(1)).send("testDecrement2:-19|c||test.testHost");
  }

  @Test
  public void shouldDecrementMultipleKeysByMagnitudeUsingSampling() throws IOException {
    target.decrement(21, 1.0, "testDecrement1", "testDecrement2");
    verify(socket, times(1)).send("testDecrement1:-21|c||test.testHost");
    verify(socket, times(1)).send("testDecrement2:-21|c||test.testHost");
  }

  @Test
  public void shouldNotDecrementUsingZeroSampling() throws IOException {
    target.decrement("testDecrement1", 1, 0.0);
    verify(socket, times(0)).send(anyString());
  }

  @Test
  public void shouldIncrement() throws IOException {
    target.increment("testIncrement");
    verify(socket, times(1)).send("testIncrement:1|c||test.testHost");
  }

  @Test
  public void shouldIncrementByMagnitude() throws IOException {
    target.increment("testIncrement", 81);
    verify(socket, times(1)).send("testIncrement:81|c||test.testHost");
  }

  @Test
  public void normalIncrementShouldReturnTrue() throws IOException {
    socket.send(anyString());
    assertThat(target.increment("testIncrement")).isEqualTo(true);
  }

  @Test
  public void incrementShouldReturnFalseOnError() throws IOException {
    expectToFail();
    assertThat(target.increment("testIncrement")).isEqualTo(false);
  }

  @Test
  public void incrementShouldReturnTrueIfIncludedInSample() throws IOException {
    socket.send(anyString());
    mockRngAndReturn(0.4);
    assertThat(target.increment("testIncrement", 1, 0.5)).isEqualTo(true);
    verify(socket, times(1)).send("testIncrement:1|c|@0.5|test.testHost");
  }

  @Test
  public void incrementShouldReturnFalseIfExcludedFromSample() throws IOException {
    socket.send(anyString());
    mockRngAndReturn(0.6);
    assertThat(target.increment("testIncrement", 1, 0.5)).isEqualTo(false);
  }

  @Test
  public void incrementShouldReturnFalseIfIncludedInSampleOnError() throws IOException {
    expectToFail();
    mockRngAndReturn(0.4);
    assertThat(target.increment("testIncrement", 1, 0.5)).isEqualTo(false);
  }

  private void expectToFail() throws IOException {
    socket.send(anyString());
    when(socket).thenThrow(new IOException());
  }

  private void mockRngAndReturn(double value) {
    StatsdClient.rng = mock(Random.class);
    when(StatsdClient.rng.nextDouble()).thenReturn(value);
  }
}
