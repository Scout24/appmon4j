package de.is24.util.monitoring.measurement;

public interface MeasurementHandler {
  void handle(String monitorName, long measurement);
}
