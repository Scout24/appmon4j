package de.is24.util.monitoring.sensor;

import de.is24.util.monitoring.InApplicationMonitor;


public abstract class Sensor {
  private final InApplicationMonitor inApplicationMonitor;

  public Sensor(InApplicationMonitor inApplicationMonitor) {
    this.inApplicationMonitor = inApplicationMonitor;
  }

  protected InApplicationMonitor getInApplicationMonitor() {
    return inApplicationMonitor;
  }

  public abstract void incrementCounter(String name);

  public abstract void incrementCounter(String name, int increment);

  public abstract void addTimerMeasurement(String name, long timing);

  public abstract void addTimerMeasurement(String name, long start, long end);
}
