package de.is24.util.monitoring;

/**
 * Classes can register themselves as ReportableObserver if they want to be notified
 * about new reportables that are registered on the InApplicationMonitor
 *
 * Implementations should overwrite toString(), to provide some context to CorePlugin.getRegisteredReportableObservers()
 *
 */
public interface ReportableObserver {
  /**
   * This method is called for each reportable that is registered on the InApplicationMonitor.
   * Additionally, the method is called for each reportable that has been registered before
   * the ReportableObserver has registered itself.
   *
   * It is guaranteed that this method is called at least once for each reportable in the InApplicationMonitor.
   *
   * @param reportable the reportable metric to process
   */
  void addNewReportable(Reportable reportable);

}
