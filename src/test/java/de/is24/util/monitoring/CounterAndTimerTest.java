package de.is24.util.monitoring;

import junit.framework.TestCase;

import java.text.NumberFormat;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class CounterAndTimerTest extends TestCase {
  private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

  static {
    NUMBER_FORMAT.setGroupingUsed(true);
    NUMBER_FORMAT.setMinimumFractionDigits(3);
    NUMBER_FORMAT.setMaximumFractionDigits(3);
  }

  private static final int THREADS = 10, RUNS = 50, ITERATIONS = 1000, OVERALL = RUNS * ITERATIONS;

  private ThreadPoolExecutor executor;
  private Counter counter;
  private Timer timer;
  private Job counterJob, timerJob;
  private Random rnd;

  private AtomicLong timerSum;

  protected void setUp() throws Exception {
    this.counter = new Counter("test");
    this.timer = new Timer("test");
    this.counterJob = new CounterJob();
    this.timerJob = new TimerJob();
    this.rnd = new Random();
    this.timerSum = new AtomicLong();
  }


  public void testAddMeasurement() throws Exception {
    createExecutor(THREADS);

    executeJob(timerJob);

    assertEquals(OVERALL, timer.getCount());
    assertEquals(timerSum.get(), timer.getTimerSum());
    System.out.println("Timer measured " + timer.getCount() + " times.");
    System.out.println("Timer measured a total of " + timer.getTimerSum() + " ms.");
    System.out.println("Timer measured an average of " + timer.getTimerAvg() + " ms.");
    System.out.println("Timer measured a standard deviance of " + timer.getTimerStdDev() + " ms.");


    waitASecond();

    setUp();

    executePerformanceTest(timerJob);
  }


  public void testIncrement() throws Exception {
    createExecutor(THREADS);

    executeJob(counterJob);

    assertEquals(OVERALL, counter.getCount());
    System.out.println("Counter measured " + counter.getCount() + " times.");

    waitASecond();

    setUp();

    executePerformanceTest(counterJob);
  }


  private void waitASecond() throws InterruptedException {
    System.gc();
    Thread.sleep(1000);
    System.gc();
  }


  private void executePerformanceTest(Job job) throws InterruptedException {
    for (int threads = 8; threads >= 1; threads -= 1) {
      createExecutor(threads);

      job.reset();
      executeJob(job);

      System.out.println("Executed " + job.getClass().getSimpleName() + " test (" + RUNS + "x" + ITERATIONS +
        " times using " + threads + " threads) in avg. " + NUMBER_FORMAT.format(job.getTimerAvgMs()) +
        " (std. deviance " + NUMBER_FORMAT.format(job.getTimerStdDevMs()) + ") ms.");
    }
  }


  private void createExecutor(int threads) {
    this.executor = new ScheduledThreadPoolExecutor(threads);
    this.executor.setMaximumPoolSize(threads);
  }


  private void executeJob(Runnable job) throws InterruptedException {
    for (int i = 0; i < RUNS; i++) {
      executor.execute(job);
    }
    executor.shutdown();

    executor.awaitTermination(100, TimeUnit.SECONDS);
  }

  private abstract class Job implements Runnable {
    private final AtomicInteger executions = new AtomicInteger();
    private final AtomicLong timerSum = new AtomicLong();
    private final AtomicLong timerSumOfSquares = new AtomicLong();

    public void run() {
      final long start = System.nanoTime();
      doJob();

      final long time = System.nanoTime() - start;
      executions.incrementAndGet();
      timerSum.addAndGet(time);
      timerSumOfSquares.addAndGet(time * time);
    }

    protected abstract void doJob();

    public double getTimerAvgMs() {
      return Math.average(executions.get(), timerSum.get()) / (1000 * 1000d);
    }

    public double getTimerStdDevMs() {
      return Math.stdDeviation(executions.get(), timerSum.get(), timerSumOfSquares.get()) / (1000 * 1000d);
    }

    public void reset() {
      executions.set(0);
      timerSum.set(0);
      timerSumOfSquares.set(0);
    }
  }


  private class CounterJob extends Job {
    protected void doJob() {
      try {
        for (int i = 0; i < ITERATIONS; i++) {
          final long millis = (long) java.lang.Math.pow(rnd.nextInt(10), 3);
          counter.increment();
          timerSum.addAndGet(millis);
        }
      } catch (RuntimeException e) {
        e.printStackTrace();
      }
    }

  }

  private class TimerJob extends Job {
    protected void doJob() {
      try {
        for (int i = 0; i < ITERATIONS; i++) {
          final long millis = (long) java.lang.Math.pow(rnd.nextInt(10), 3);
          timerSum.addAndGet(millis);
          timer.addMeasurement(millis);
        }
      } catch (RuntimeException e) {
        e.printStackTrace();
      }
    }

  }
}
