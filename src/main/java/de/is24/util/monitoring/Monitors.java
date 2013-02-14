package de.is24.util.monitoring;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Wrapper class for monitors. Simplifies boilerplate administration.
 *
 * @param <T> the type of monitor beans
 */
class Monitors<T extends Reportable> {
  private final Map<String, T> monitors = new ConcurrentHashMap<String, T>();

  private final List<ReportableObserver> reportableObservers;

  public Monitors(List<ReportableObserver> reportableObservers) {
    this.reportableObservers = reportableObservers;
  }

  public T get(String key, Factory<T> factory) {
    T result = get(key);
    if (result == null) {
      synchronized (monitors) {
        result = get(key);
        if (result == null) {
          result = factory.createMonitor();
          put(key, result);
          notifyReportableObservers(result);
        }
      }
    }
    return result;
  }

  public T get(String key) {
    return monitors.get(key);
  }

  public T put(String key, T reportable) {
    return monitors.put(key, reportable);
  }

  private void notifyReportableObservers(Reportable reportable) {
    for (ReportableObserver reportableObserver : reportableObservers) {
      reportableObserver.addNewReportable(reportable);
    }
  }

  public void accept(ReportVisitor reportVisitor) {
    for (Reportable reportable : getMonitors()) {
      reportable.accept(reportVisitor);
    }
  }

  public Collection<T> getMonitors() {
    return monitors.values();
  }

  public interface Factory<T> {
    T createMonitor();
  }
}
