package de.is24.util.monitoring.measurement;

import de.is24.util.monitoring.InApplicationMonitor;


public class TimerMeasurementHandler implements MeasurementHandler {
  @Override
  public void handle(String monitorName, long measurement) {
    assert (monitorName != null) && (monitorName.trim().length() > 0);
    InApplicationMonitor.getInstance().addTimerMeasurement(monitorName, measurement);
  }
}
