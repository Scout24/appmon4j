/*
 * Created on 12.02.2005
 */
package de.is24.util.monitoring;

/**
 * ReportVisitors are used to read content from the registered {@link Reportable} instances
 * of the {@link InApplicationMonitor}
 *
 * @author OSchmitz
 */
public interface ReportVisitor {
  /**
   * method called for each {@link Counter}
   * @param counter
   */
  void reportCounter(Counter counter);


  /**
   * method called for each {@link Timer}
   * @param timer
   */
  void reportTimer(Timer timer);

  /**
   * method called for each {@link StateValueProvider}
   * @param stateValueProvider
   */
  void reportStateValue(StateValueProvider stateValueProvider);

  /**
   * method called for each {@link MultiValueProvider}
   * @param multiValueProvider
   */
  void reportMultiValue(MultiValueProvider multiValueProvider);


  /**
  * method called for each {@link HistorizableList}
  * @param historizableList
  */
  void reportHistorizableList(HistorizableList historizableList);

  /**
   * method called for each {@link Version}
   * @param version
   */
  void reportVersion(Version version);
}
