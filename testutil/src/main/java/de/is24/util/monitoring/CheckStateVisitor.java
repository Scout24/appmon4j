package de.is24.util.monitoring;

import de.is24.util.monitoring.tools.DoNothingReportVisitor;


public class CheckStateVisitor extends DoNothingReportVisitor {
  private final String counterName;
  private boolean found = false;
  private long value = 0;

  public CheckStateVisitor(String counterName) {
    this.counterName = counterName;
  }

  @Override
  public void reportStateValue(StateValueProvider stateValueProvider) {
    if (stateValueProvider.getName().equals(counterName)) {
      found = true;
      value = stateValueProvider.getValue();
    }
  }


  public boolean isFound() {
    return found;
  }

  public long getValue() {
    return value;
  }
}
