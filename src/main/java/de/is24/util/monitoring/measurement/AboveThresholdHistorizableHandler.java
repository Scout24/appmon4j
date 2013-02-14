package de.is24.util.monitoring.measurement;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.SimpleHistorizable;


public class AboveThresholdHistorizableHandler implements MeasurementHandler {
  private String thresholdName;
  private long threshold;

  public AboveThresholdHistorizableHandler(String thresholdName, long threshold) {
    if ((thresholdName == null) || (thresholdName.trim().length() == 0)) {
      throw new IllegalArgumentException("threshold Name must not be null or empty");
    }
    if (threshold <= 0) {
      throw new IllegalArgumentException("threshold must be positive");
    }
    this.thresholdName = thresholdName.trim();
    this.threshold = threshold;
  }

  @Override
  public void handle(String monitorName, long measurement) {
    if (measurement > threshold) {
      InApplicationMonitor.getInstance().addHistorizable(new SimpleHistorizable(thresholdName,
          "measurement of " + monitorName + " with " + measurement + " above threshold " + threshold));
    }
  }

}
