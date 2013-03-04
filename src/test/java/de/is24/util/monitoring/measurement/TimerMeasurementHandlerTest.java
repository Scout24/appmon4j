package de.is24.util.monitoring.measurement;

import de.is24.util.monitoring.Counter;
import de.is24.util.monitoring.HistorizableList;
import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.ReportVisitor;
import de.is24.util.monitoring.StateValueProvider;
import de.is24.util.monitoring.TestHelper;
import de.is24.util.monitoring.Timer;
import de.is24.util.monitoring.Version;
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


  class TimerExtractor implements ReportVisitor {
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

    @Override
    public void reportCounter(Counter counter) {
    }

    @Override
    public void reportStateValue(StateValueProvider stateValueProvider) {
    }

    @Override
    public void reportHistorizableList(HistorizableList historizableList) {
    }

    @Override
    public void reportVersion(Version version) {
    }
  }
}
