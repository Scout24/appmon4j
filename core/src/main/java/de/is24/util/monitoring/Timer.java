package de.is24.util.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Timers count and measure timed events. <br>
 * Timers allow adding timer measurements, implicitly incrementing the count<br>
 * <pre> Examples
 *    DB Query duration
 *    rendering duration
 *    parsing time of xml input
 * </pre>
 * <br>
 * Note that operations might not appear consistent especially with only a few measurements
 * as none of the timer operations are atomic. With many measurements these inconsistencies
 * should not be notable anymore though.
 *
 * @author OSchmitz
 */
public class Timer implements Reportable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Timer.class);
  private final String name;
  private final AtomicLong count = new AtomicLong();
  private final AtomicLong timerSum = new AtomicLong();
  private final AtomicLong timerSumOfSquares = new AtomicLong();

  /**
   * This class is only constructed by {@link InApplicationMonitor}
   * @param name name of this Timer
   */
  Timer(String name) {
    this.name = name;
  }


  /**
   * Implements the visitor pattern to read this StateValueProvider
   */
  @Override
  public void accept(ReportVisitor aVisitor) {
    LOGGER.debug("+++ entering Timer.accept +++");
    aVisitor.reportTimer(this);
  }

  /**
   * Add a timer measurement for this timer.<br>
   * The application decides which unit to use for timing.
   * Milliseconds are suggested and some {@link ReportVisitor} implementations
   * may imply this.
   * @param durationInMillis
   */
  public void addMeasurement(long durationInMillis) {
    count.addAndGet(1);
    timerSum.addAndGet(durationInMillis);
    timerSumOfSquares.addAndGet(durationInMillis * durationInMillis);
  }

  /**
   * initialize with 0
   */
  public void initializeMeasurement() {
    count.set(0);
    this.timerSum.set(0);
    this.timerSumOfSquares.set(0);
  }

  @Override
  public String getName() {
    return name;
  }

  public long getCount() {
    return count.get();
  }

  /**
  * @return the sum of all timer measurements.
  */
  public long getTimerSum() {
    return timerSum.get();
  }

  /**
   * Note that this calculation might be notably inconsistent
   * unless there are many measurements as a timer is not atomic.
   *
   * @return the average of all timer measurements.
   */
  public double getTimerAvg() {
    return Math.average(count.get(), timerSum.get());
  }

  /**
   * Note that this calculation might be notably inconsistent
   * unless there are many measurements as a timer is not atomic.
   *
   * @return the standard deviation of all timer measurements.
   */
  public double getTimerStdDev() {
    return Math.stdDeviation(count.get(), timerSum.get(), timerSumOfSquares.get());

  }
}
