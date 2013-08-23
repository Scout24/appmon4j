package de.is24.util.monitoring.sensor;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.tools.LocalHostNameResolver;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class HostSensorTest {
  private static final String HOSTNAME = "devcat01";
  private InApplicationMonitor monitorMock = mock(InApplicationMonitor.class);
  private LocalHostNameResolver hostNameResolver = mock(LocalHostNameResolver.class);
  private Sensor sensor;

  @Before
  public void setup() {
    reset(monitorMock);
    when(hostNameResolver.getLocalHostName()).thenReturn(HOSTNAME);
    sensor = new HostSensor(monitorMock, hostNameResolver);
  }

  @Test
  public void testIncrementCounter() {
    sensor.incrementCounter("test");

    verify(monitorMock, times(1)).incrementCounter(HOSTNAME + ".test");
  }

  @Test
  public void testIncrementCounterWithIncrement() throws Exception {
    sensor.incrementCounter("test", 1);

    verify(monitorMock, times(1)).incrementCounter(HOSTNAME + ".test", 1);
  }

  @Test
  public void testAddTimerMeasurement() throws Exception {
    sensor.addTimerMeasurement("test", 1);

    verify(monitorMock, times(1)).addTimerMeasurement(HOSTNAME + ".test", 1);
  }

  @Test
  public void testAddTimerMeasurementWithStartEnd() throws Exception {
    sensor.addTimerMeasurement("test", 1, 2);

    verify(monitorMock, times(1)).addTimerMeasurement(HOSTNAME + ".test", 1, 2);
  }
}
