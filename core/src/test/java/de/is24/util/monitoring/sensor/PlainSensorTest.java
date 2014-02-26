package de.is24.util.monitoring.sensor;

import de.is24.util.monitoring.InApplicationMonitor;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class PlainSensorTest {
  private InApplicationMonitor monitorMock = mock(InApplicationMonitor.class);
  private Sensor sensor = new PlainSensor(monitorMock);

  @Before
  public void setup() {
    reset(monitorMock);
  }

  @Test
  public void testIncrementCounter() {
    sensor.incrementCounter("test");

    verify(monitorMock, times(1)).incrementCounter("test");
  }

  @Test
  public void testIncrementCounterWithIncrement() throws Exception {
    sensor.incrementCounter("test", 1);

    verify(monitorMock, times(1)).incrementCounter("test", 1);
  }

  @Test
  public void testAddTimerMeasurement() throws Exception {
    sensor.addTimerMeasurement("test", 1);

    verify(monitorMock, times(1)).addTimerMeasurement("test", 1);
  }

  @Test
  public void testAddTimerMeasurementWithStartEnd() throws Exception {
    sensor.addTimerMeasurement("test", 1, 2);

    verify(monitorMock, times(1)).addTimerMeasurement("test", 1, 2);
  }
}
