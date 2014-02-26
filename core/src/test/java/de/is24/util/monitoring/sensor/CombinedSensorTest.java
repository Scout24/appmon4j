package de.is24.util.monitoring.sensor;

import de.is24.util.monitoring.InApplicationMonitor;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class CombinedSensorTest {
  private InApplicationMonitor monitorMock = mock(InApplicationMonitor.class);
  private Sensor sensor1 = mock(PlainSensor.class);
  private Sensor sensor2 = mock(HostSensor.class);
  private Sensor sensor = new CombinedSensor(monitorMock, sensor1, sensor2);

  @Before
  public void setup() {
    reset(sensor1, sensor2);
  }

  @Test
  public void testIncrementCounter() {
    sensor.incrementCounter("test");

    verify(sensor1, times(1)).incrementCounter("test");
    verify(sensor2, times(1)).incrementCounter("test");
  }

  @Test
  public void testIncrementCounterWithIncrement() throws Exception {
    sensor.incrementCounter("test", 1);

    verify(sensor1, times(1)).incrementCounter("test", 1);
    verify(sensor2, times(1)).incrementCounter("test", 1);
  }

  @Test
  public void testAddTimerMeasurement() throws Exception {
    sensor.addTimerMeasurement("test", 1);

    verify(sensor1, times(1)).addTimerMeasurement("test", 1);
    verify(sensor2, times(1)).addTimerMeasurement("test", 1);
  }

  @Test
  public void testAddTimerMeasurementWithStartEnd() throws Exception {
    sensor.addTimerMeasurement("test", 1, 2);

    verify(sensor1, times(1)).addTimerMeasurement("test", 1, 2);
    verify(sensor2, times(1)).addTimerMeasurement("test", 1, 2);
  }
}
