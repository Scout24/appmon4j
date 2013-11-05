package de.is24.util.monitoring.sensor;

import de.is24.util.monitoring.InApplicationMonitor;


public class CombinedSensor extends Sensor {
  private final Sensor[] sensors;

  public CombinedSensor(final InApplicationMonitor inApplicationMonitor, Sensor... sensors) {
    super(inApplicationMonitor);
    this.sensors = sensors;
  }

  public Sensor[] getSensors() {
    return sensors;
  }

  @Override
  public void incrementCounter(final String name) {
    for (Sensor sensor : sensors) {
      sensor.incrementCounter(name);
    }
  }

  @Override
  public void incrementCounter(final String name, final int increment) {
    for (Sensor sensor : sensors) {
      sensor.incrementCounter(name, increment);
    }
  }

  @Override
  public void addTimerMeasurement(final String name, final long timing) {
    for (Sensor sensor : sensors) {
      sensor.addTimerMeasurement(name, timing);
    }
  }

  @Override
  public void addTimerMeasurement(final String name, final long start, final long end) {
    for (Sensor sensor : sensors) {
      sensor.addTimerMeasurement(name, start, end);
    }
  }
}
