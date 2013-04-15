package de.is24.util.monitoring.tools;

import de.is24.util.monitoring.Counter;
import de.is24.util.monitoring.HistorizableList;
import de.is24.util.monitoring.MultiValueProvider;
import de.is24.util.monitoring.ReportVisitor;
import de.is24.util.monitoring.StateValueProvider;
import de.is24.util.monitoring.Timer;
import de.is24.util.monitoring.Version;


/**
 * convenience class for easy selective override of ReportVisitor
 */
public class DoNothingReportVisitor implements ReportVisitor {
  @Override
  public void reportCounter(Counter counter) {
  }

  @Override
  public void reportTimer(Timer timer) {
  }

  @Override
  public void reportStateValue(StateValueProvider stateValueProvider) {
  }

  @Override
  public void reportMultiValue(MultiValueProvider multiValueProvider) {
  }

  @Override
  public void reportHistorizableList(HistorizableList historizableList) {
  }

  @Override
  public void reportVersion(Version version) {
  }
}
