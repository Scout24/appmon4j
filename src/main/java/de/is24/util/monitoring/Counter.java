package de.is24.util.monitoring;

import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Counters are used to count events.
 * Counters can only be incremented by 1
 * <pre>
 * Examples :
 *   number of XYZ errors / Exceptions
 *   number of handled events
 *   number of failed events
 * </pre>
 * @author OSchmitz
 */
public class Counter implements Reportable {
  private static final Logger LOGGER = Logger.getLogger(Counter.class);
  private final String fName;
  private final AtomicLong count = new AtomicLong();

  /**
   * This class is only constructed by {@link InApplicationMonitor}
   * @param name name of this Counter
   */
  Counter(String name) {
    fName = name;
  }

  /**
   * Implements the visitor pattern to read this Counter
   */
  public void accept(ReportVisitor aVisitor) {
    LOGGER.debug("+++ entering Counter.accept +++");
    aVisitor.reportCounter(this);
  }

  /**
   * Increment the value of this counter by one
   */
  public void increment() {
    count.addAndGet(1);
  }

  /**
   * <p>Increase the counter by the specified amount.</p>
   *
   * @param   increment
   *          the added to add
   */
  public void increment(long increment) {
    count.addAndGet(increment);
  }

  /**
   * Initialize with 0
   */
  public void initialize() {
    count.set(0);
  }

  /**
   * get the value of this counter
   * @return current count.
   */
  public long getCount() {
    return count.get();
  }

  /**
   * @return name of this Counter.
   */
  public String getName() {
    return fName;
  }
}
