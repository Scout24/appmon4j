package de.is24.util.monitoring;

import de.is24.util.monitoring.tools.DoNothingReportVisitor;


public class CheckCounterVisitor extends DoNothingReportVisitor {
  private final String counterName;
  private boolean found = false;
  private long value = 0;

  public CheckCounterVisitor(String counterName) {
    this.counterName = counterName;
  }

  @Override
  public void reportCounter(Counter counter) {
    if (counter.getName().equals(counterName)) {
      found = true;
      value = counter.getCount();
    }
  }

  public boolean isFound() {
    return found;
  }

  public long getValue() {
    return value;
  }
}
