package de.is24.util.monitoring.status;

import de.is24.util.monitoring.Counter;
import de.is24.util.monitoring.Reportable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * this class implements a service state from observing two counters
 * and calculating a failure rate for different time windows.
 *
 * Current windows are  1min, 5min, 15min.
 * Therefor it samples the counters every 10 seconds.
 */
public class ServiceState {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceState.class);
  private final String name;
  private final String successCounterKey;
  private final String errorCounterKey;
  private long previousSuccessCounterValue = Long.MAX_VALUE;
  private long previousErrorCounterValue = Long.MAX_VALUE;

  private Counter successCounter;
  private Counter errorCounter;
  private SampledData sampledData;

  public ServiceState(String name, String successCounterKey, String errorCounterKey) {
    this.name = name;
    this.successCounterKey = successCounterKey;
    this.errorCounterKey = errorCounterKey;
    this.sampledData = new SampledData(name);
  }

  public void check() {
    long successCounterCount = (successCounter == null) ? Long.MAX_VALUE : successCounter.getCount();
    long successDelta;
    long errorCounterCount = (errorCounter == null) ? Long.MAX_VALUE : errorCounter.getCount();
    long errorDelta = 0;
    if (previousErrorCounterValue <= errorCounterCount) {
      errorDelta = errorCounterCount - previousErrorCounterValue;
    }
    if (previousSuccessCounterValue <= successCounterCount) {
      successDelta = successCounterCount - previousSuccessCounterValue;
      LOGGER.debug("successDelta : {} errorDelta : {}", successDelta, errorDelta);
      sampledData.addSample(successDelta, errorDelta);
    }
    previousErrorCounterValue = errorCounterCount;
    previousSuccessCounterValue = successCounterCount;
  }


  public void checkForReportable(Reportable reportable) {
    LOGGER.debug("checking reportable with name {}", reportable.getName());
    if ((successCounter == null) && (reportable instanceof Counter) && reportable.getName().equals(successCounterKey)) {
      LOGGER.info("found {}", successCounterKey);
      successCounter = (Counter) reportable;
    }
    if ((errorCounter == null) && (reportable instanceof Counter) && reportable.getName().equals(errorCounterKey)) {
      LOGGER.info("found {}", errorCounterKey);
      errorCounter = (Counter) reportable;
    }
  }

  public String getName() {
    return name;
  }
}
