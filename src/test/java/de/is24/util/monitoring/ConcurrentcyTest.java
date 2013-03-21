package de.is24.util.monitoring;

import de.is24.util.monitoring.jmx.SimpleJmxAppmon4jNamingStrategy;
import de.is24.util.monitoring.keyhandler.DefaultKeyEscaper;
import de.is24.util.monitoring.keyhandler.KeyHandler;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import java.text.NumberFormat;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;


public class ConcurrentcyTest {
  private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
  private static final Logger LOGGER = Logger.getLogger(ConcurrentcyTest.class);

  static {
    NUMBER_FORMAT.setGroupingUsed(true);
    NUMBER_FORMAT.setMinimumFractionDigits(3);
    NUMBER_FORMAT.setMaximumFractionDigits(3);
  }

  private static final int THREADS = 10, RUNS = 10, LOOPS = 10;

  private ThreadPoolExecutor executor;
  private Job observerJob, timerJob, initJob;
  private Random rnd;

  private AtomicLong exceptions;
  private AtomicLong successfullInits;
  private AtomicLong failedInits;

  @Before
  public void setUp() throws Exception {
    this.observerJob = new ObserverJob();
    this.timerJob = new TimerJob();
    this.initJob = new InitJob();
    this.rnd = new Random();
    this.exceptions = new AtomicLong();
    this.successfullInits = new AtomicLong();
    this.failedInits = new AtomicLong();
  }

  @Test
  public void testConcurrency() throws InterruptedException {
    for (int i = 0; i < LOOPS; i++) {
      LOGGER.info("#########################   run " + i + " ###########################");
      LOGGER.info("#########################   run " + i + " ###########################");
      LOGGER.info("#########################   run " + i + " ###########################");
      LOGGER.info("#########################   run " + i + " ###########################");
      InApplicationMonitor.resetInstanceForTesting();
      assertFalse(JMXTestHelper.checkInApplicationMonitorJMXBeanRegistered());

      createExecutor(THREADS);

      executeJobs(timerJob, observerJob, initJob);
      assertTrue(JMXTestHelper.checkInApplicationMonitorJMXBeanRegistered());
    }
    assertThat(exceptions.get(), is(0L));
    assertThat(successfullInits.get(), is((long) LOOPS));


  }

  private void createExecutor(int threads) {
    this.executor = new ScheduledThreadPoolExecutor(threads);
    this.executor.setMaximumPoolSize(threads);
  }


  private void executeJobs(Runnable... jobs) throws InterruptedException {
    for (int i = 0; i < RUNS; i++) {
      for (Runnable job : jobs) {
        executor.execute(job);
      }
    }
    executor.shutdown();

    executor.awaitTermination(100, TimeUnit.SECONDS);
  }

  private abstract class Job implements Runnable {
    public void run() {
      try {
        doJob();
      } catch (Exception e) {
        LOGGER.info("oops", e);
        exceptions.incrementAndGet();
      }
    }

    protected abstract void doJob();
  }


  private class ObserverJob extends Job {
    @Override
    protected void doJob() {
      InApplicationMonitor.getInstance().addReportableObserver(new ReportableObserver() {
          @Override
          public void addNewReportable(Reportable reportable) {
            try {
              Thread.sleep(1);
            } catch (InterruptedException e) {
              Thread.interrupted();
            }
          }
        });
    }

  }

  private class TimerJob extends Job {
    @Override
    protected void doJob() {
      String key = "t" + rnd.nextInt(100000);
      InApplicationMonitor.getInstance().addTimerMeasurement(key, 1);
    }

  }

  private class InitJob extends Job {
    @Override
    protected void doJob() {
      KeyHandler keyEscaper = new DefaultKeyEscaper();
      try {
        CorePlugin corePlugin = new CorePlugin(new SimpleJmxAppmon4jNamingStrategy("is24"), keyEscaper);
        InApplicationMonitor.initInstance(corePlugin, keyEscaper);
        successfullInits.incrementAndGet();
      } catch (IllegalStateException e) {
        failedInits.incrementAndGet();
      }
    }

  }

}
