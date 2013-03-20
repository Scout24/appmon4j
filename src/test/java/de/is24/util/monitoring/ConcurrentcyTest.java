package de.is24.util.monitoring;

import org.junit.Before;
import org.junit.Test;
import java.text.NumberFormat;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;


public class ConcurrentcyTest {
  private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

  static {
    NUMBER_FORMAT.setGroupingUsed(true);
    NUMBER_FORMAT.setMinimumFractionDigits(3);
    NUMBER_FORMAT.setMaximumFractionDigits(3);
  }

  private static final int THREADS = 10, RUNS = 10, LOOPS = 10;

  private ThreadPoolExecutor executor;
  private Job observerJob, timerJob;
  private Random rnd;

  private AtomicLong exceptions;

  @Before
  public void setUp() throws Exception {
    this.observerJob = new ObserverJob();
    this.timerJob = new TimerJob();
    this.rnd = new Random();
    this.exceptions = new AtomicLong();
  }

  @Test
  public void testConcurrency() throws InterruptedException {
    for (int i = 0; i < LOOPS; i++) {
      InApplicationMonitor.resetInstanceForTesting();
      createExecutor(THREADS);

      executeJobs(timerJob, observerJob);
    }
    assertThat(exceptions.get(), is(0L));

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
}
