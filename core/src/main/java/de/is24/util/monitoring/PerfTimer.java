package de.is24.util.monitoring;

import de.is24.util.monitoring.measurement.MeasurementHandler;
import org.slf4j.LoggerFactory;


/**
 * Helper class to perform timer measurements. Once a timer it is created it measures the current system time.
 * Calling measure() will measure the time again, calculate the difference in milli seconds and return it.
 * <p>
 * Usage:
 * <pre>
 * final PerfTimer actionTimer = PerfTimer.createDebugTimer(getClass());
 * doAction();
 * System.out.println("Executing action took " + actionTimer.measure() + " ms");
 * </pre>
 *
 * PerfTimer provides an {@link InApplicationMonitor} (createMonitor()) connection as well as debug-only measurement facilities.
 *
 * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
 */
public abstract class PerfTimer {
  /**
   * @return a timer measuring at any time.
   */
  public static PerfTimer createTimer() {
    return create(true);
  }

  /**
   * @return a {@link PerfMonitor}. Use PerfMonitor.monitor({@link String})
   * to perform a measurement and submit it to the {@link InApplicationMonitor}.
   */
  public static PerfMonitor createMonitor() {
    return PerfMonitor.create();
  }

  /**
   * @param type the logger category used for debugging. A null argument performs equally to createTimer().
   * @return a timer that only performs valid measurements on debug level and below.
   */
  public static PerfTimer createDebugTimer(Class<?> type) {
    return create(LoggerFactory.getLogger(type).isDebugEnabled());
  }

  /**
   * @param type the logger category used for info-level debugging. A null argument performs equally to createTimer().
   * @return a timer that only performs valid measurements on info level and below.
   */
  public static PerfTimer createInfoTimer(Class<?> type) {
    return create(LoggerFactory.getLogger(type).isInfoEnabled());
  }

  /**
   * @param active determining whether to measure or not to measure.
   * @return a timer that only performs valid measurements if the argument is true.
   */
  private static PerfTimer create(final boolean active) {
    if (active) {
      return DefaultPerfTimer.create();
    }
    return NoPerfTimer.create();
  }

  /**
   * @return the time in milli seconds from the creation of this timer until the call of this method.
   */
  public abstract long measure();

  /**
   * A timer that always returns 0 on measurements for performance reasons (null object).
   *
   * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
   */
  private static final class NoPerfTimer extends PerfTimer {
    private static final PerfTimer INSTANCE = new NoPerfTimer();

    static PerfTimer create() {
      return INSTANCE;
    }

    private NoPerfTimer() {
    }

    @Override
    public long measure() {
      return 0L;
    }
  }

  /**
   * A timer implementing the default measurement behaviour based on system time.
   *
   * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
   */
  private static class DefaultPerfTimer extends PerfTimer {
    static PerfTimer create() {
      return new DefaultPerfTimer();
    }

    protected final long start;

    protected DefaultPerfTimer() {
      start = System.currentTimeMillis();
    }

    @Override
    public long measure() {
      return System.currentTimeMillis() - start;
    }
  }

  /**
   * A timer providing an interface for submitting measurements to InApplicationMonitor.
   * Simply call monitor(String) to submit the measurement.
   *
   * @author <a href="mailto:sschubert@immobilienscout24.de">Stefan Schubert</a>, IT-E, IS24
   */
  public static final class PerfMonitor extends DefaultPerfTimer {
    static PerfMonitor create() {
      return new PerfMonitor();
    }

    protected PerfMonitor() {
      super();
    }

    /**
    * Performs a measurement and submits the result to the InApplicationMonitor using the specified monitor name(s).
    *
    * @param monitorNames the keys where monitoring results for a class of measurements can be looked up.
    * @deprecated use handleMeasurement instead
    */
    @Deprecated
    public void monitor(String... monitorNames) {
      assert (monitorNames != null) && (monitorNames.length > 0);

      final long time = measure();

      for (String monitor : monitorNames) {
        assert (monitor != null) && (monitor.trim().length() > 0);
        InApplicationMonitor.getInstance().addTimerMeasurement(monitor, time);
      }
    }

    public void handleMeasurement(String monitorName, MeasurementHandler... handlers) {
      long time = measure();
      for (MeasurementHandler handler : handlers) {
        handler.handle(monitorName, time);
      }
    }

  }


}
