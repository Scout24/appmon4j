package de.is24.util.monitoring.sensor;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.tools.LocalHostNameResolver;


public class HostSensor extends Sensor {
  private final String localHostName;

  public HostSensor(final InApplicationMonitor inApplicationMonitor, LocalHostNameResolver localHostNameResolver) {
    super(inApplicationMonitor);
    this.localHostName = localHostNameResolver.getLocalHostName() + ".";
  }

  @Override
  public void incrementCounter(final String name) {
    getInApplicationMonitor().incrementCounter(localHostName + name);
  }

  @Override
  public void incrementCounter(final String name, final int increment) {
    getInApplicationMonitor().incrementCounter(localHostName + name, increment);
  }

  @Override
  public void addTimerMeasurement(final String name, final long timing) {
    getInApplicationMonitor().addTimerMeasurement(localHostName + name, timing);
  }

  @Override
  public void addTimerMeasurement(final String name, final long start, final long end) {
    getInApplicationMonitor().addTimerMeasurement(localHostName + name, start, end);
  }
}
