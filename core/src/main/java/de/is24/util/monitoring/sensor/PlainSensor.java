package de.is24.util.monitoring.sensor;

import de.is24.util.monitoring.InApplicationMonitor;


public class PlainSensor extends Sensor {
  public PlainSensor(final InApplicationMonitor inApplicationMonitor) {
    super(inApplicationMonitor);
  }

  @Override
  public void incrementCounter(final String name) {
    getInApplicationMonitor().incrementCounter(name);
  }

  @Override
  public void incrementCounter(final String name, final int increment) {
    getInApplicationMonitor().incrementCounter(name, increment);
  }

  @Override
  public void addTimerMeasurement(final String name, final long timing) {
    getInApplicationMonitor().addTimerMeasurement(name, timing);
  }

  @Override
  public void addTimerMeasurement(final String name, final long start, final long end) {
    getInApplicationMonitor().addTimerMeasurement(name, start, end);
  }
}
