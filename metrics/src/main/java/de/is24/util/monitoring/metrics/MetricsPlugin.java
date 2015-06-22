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
    return null;
  }

  @Override
  public void initializeCounter(String name) {
  }

  @Override
  public void incrementCounter(String name, int increment) {
    metricRegistry.counter(name).inc(increment);
  }

  @Override
  public void incrementHighRateCounter(String name, int increment) {
    incrementCounter(name, increment);
  }

  @Override
  public void addTimerMeasurement(String name, long timing) {
    metricRegistry.timer(name).update(timing, TimeUnit.MILLISECONDS);
  }

  @Override
  public void addSingleEventTimerMeasurement(String name, long timing) {
    metricRegistry.timer(name).update(timing, TimeUnit.MILLISECONDS);
  }

  @Override
  public void addHighRateTimerMeasurement(String name, long timing) {
    metricRegistry.timer(name).update(timing, TimeUnit.MILLISECONDS);
  }

  @Override
  public void initializeTimerMeasurement(String name) {
  }

  @Override
  public void afterRemovalNotification() {
  }
}
