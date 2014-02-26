package de.is24.util.monitoring;

public interface MonitorPlugin {
  String getUniqueName();

  void initializeCounter(String name);

  void incrementCounter(String name, int increment);

  void incrementHighRateCounter(String name, int increment);

  void addTimerMeasurement(String name, long timing);

  void addSingleEventTimerMeasurement(String name, long timing);

  void addHighRateTimerMeasurement(String name, long timing);

  void initializeTimerMeasurement(String name);

  void register();

  void afterRemovalNotification();
}
