package de.is24.util.monitoring.keyhandler;

import de.is24.util.monitoring.InApplicationMonitor;
import org.apache.log4j.Logger;


/**
 * On a way to a more restrictive key pattern for values, this class may
 * help in finding key values, that get corrected by other Key Handler.
 */
public class DelegatingReportingKeyHandler implements KeyHandler {
  private static final Logger LOGGER = Logger.getLogger(DelegatingReportingKeyHandler.class);
  private final KeyHandler delegate;

  public DelegatingReportingKeyHandler(KeyHandler delegate) {
    this.delegate = delegate;
  }

  @Override
  public String handle(String name) {
    String result = delegate.handle(name);
    if (!result.equals(name)) {
      InApplicationMonitor.getInstance().incrementCounter("malformedKey");
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("corrected Key value from " + name + " to " + result);
      }
    }
    return result;
  }
}
