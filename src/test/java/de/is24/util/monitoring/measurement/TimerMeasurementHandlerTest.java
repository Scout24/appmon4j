package de.is24.util.monitoring.measurement;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.TestHelper;
import de.is24.util.monitoring.Timer;
import de.is24.util.monitoring.tools.DoNothingReportVisitor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;


public class TimerMeasurementHandlerTest {
  InApplicationMonitor inApplicationMonitor;
  TimerMeasurementHandler handler;
  TimerExtractor timerExtractor;

  @BeforeClass
  public static void setupClass() {
    TestHelper.setInstanceForTesting();
  }

  @AfterClass
  public static void tearDownClass() {
    TestHelper.resetInstanceForTesting();
  }

  @Before
  public void setup() {
    inApplicationMonitor = InApplicationMonitor.getInstance();

    handler = new TimerMeasurementHandler();

    timerExtractor = new TimerExtractor("test");
  }

  @Test
  public void addTimerMeasurementToInAppMonitor() throws Exception {
    handler.handle("test", 13);
    inApplicationMonitor.reportInto(timerExtractor);
    assertThat(timerExtractor.getExtractedTimer().getTimerSum() == 13);
    assertThat(timerExtractor.getExtractedTimer().getCount() == 1);
  }

  @Test(expected = AssertionError.class)
  public void nullNotAllowed() throws Exception {
    handler.handle(null, 13);
  }

  @Test(expected = AssertionError.class)
  public void emptyAfterTrimNotAllowed() throws Exception {
    handler.handle("  ", 13);
  }


  class TimerExtractor extends DoNothingReportVisitor {
    final String timerName;
    Timer extractedTimer;

    TimerExtractor(String timerName) {
      this.timerName = timerName;
    }

    @Override
    public void reportTimer(Timer timer) {
      if (timer.getName().equals(timerName)) {
        extractedTimer = timer;
      }
    }

    public Timer getExtractedTimer() {
      return extractedTimer;
    }

  }
}
