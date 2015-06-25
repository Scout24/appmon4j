package de.is24.util.monitoring.metrics;

import com.codahale.metrics.MetricRegistry;
import de.is24.util.monitoring.AbstractMonitorPlugin;
import java.util.concurrent.TimeUnit;


public class MetricsPlugin extends AbstractMonitorPlugin {
  private MetricRegistry metricRegistry;

  public MetricsPlugin(MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
  }

  @Override
  public String getUniqueName() {
    return "MetricsPlugin";
  }

  @Override
  public void initializeCounter(String name) {
  }

  @Override
  public void incrementCounter(String name, int increment) {
    metricRegistry.counter(withCountersPrefix(name)).inc(increment);
  }

  @Override
  public void incrementHighRateCounter(String name, int increment) {
    incrementCounter(name, increment);
  }

  @Override
  public void addTimerMeasurement(String name, long timing) {
    metricRegistry.timer(withTimersPrefix(name)).update(timing, TimeUnit.MILLISECONDS);
  }

  @Override
  public void addSingleEventTimerMeasurement(String name, long timing) {
    addTimerMeasurement(name, timing);
  }

  @Override
  public void addHighRateTimerMeasurement(String name, long timing) {
    addTimerMeasurement(name, timing);
  }

  @Override
  public void initializeTimerMeasurement(String name) {
  }

  @Override
  public void afterRemovalNotification() {
  }

  private String withTimersPrefix(String name) {
    return "timers." + name;
  }

  private String withCountersPrefix(String name) {
    return "counters." + name;
  }
}
