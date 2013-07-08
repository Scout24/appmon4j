/*
 * Created on 12.02.2005
 */
package de.is24.util.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * StateValueProviders allow access to a numeric value (long),
 * that is already available in the application
 * @author OSchmitz
 */
public abstract class StateValueProvider implements Reportable {
  private static final Logger LOGGER = LoggerFactory.getLogger(StateValueProvider.class);


  /**
   * Implements the visitor pattern to read this StateValueProvider
   */
  public void accept(ReportVisitor aVisitor) {
    LOGGER.debug("+++ entering StateValueProvider.accept +++");
    aVisitor.reportStateValue(this);
  }

  /**
   * @return return the value of the state value
   */
  public abstract long getValue();

  /**
   * the name of the state value
   */
  public abstract String getName();
}
