package de.is24.util.monitoring.measurement;

import de.is24.util.monitoring.HistorizableList;
import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.InApplicationMonitorRule;
import de.is24.util.monitoring.tools.DoNothingReportVisitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;


public class AboveThresholdHistorizableHandlerTest {
  @Rule
  public final InApplicationMonitorRule inApplicationMonitorRule = new InApplicationMonitorRule();

  InApplicationMonitor inApplicationMonitor;

  @Before
  public void setup() {
    inApplicationMonitor = inApplicationMonitorRule.getInApplicationMonitor();

  }

  @Test
  public void addHistorizableAboveThreshold() {
    AboveThresholdHistorizableHandler handler = new AboveThresholdHistorizableHandler("above", 1000);
    HistorizableExtractorVisitor extractorVisitor = new HistorizableExtractorVisitor("above");

    handler.handle("measurment", 1001);

    inApplicationMonitor.getCorePlugin().reportInto(extractorVisitor);

    assertThat(extractorVisitor.getExtractedList().size() == 1);
  }

  @Test
  public void doNotAddHistorizableAtThreshold() {
    AboveThresholdHistorizableHandler handler = new AboveThresholdHistorizableHandler("below", 1000);
    HistorizableExtractorVisitor extractorVisitor = new HistorizableExtractorVisitor("below");
    handler.handle("measurment", 1000);

    inApplicationMonitor.getCorePlugin().reportInto(extractorVisitor);

    assertThat(extractorVisitor.getExtractedList()).isNull();
  }

  @Test(expected = IllegalArgumentException.class)
  public void doNotAcceptNullAsThresholdName() throws Exception {
    new AboveThresholdHistorizableHandler(null, 1000);
  }

  @Test(expected = IllegalArgumentException.class)
  public void doNotAcceptNegativeThresholdValue() throws Exception {
    new AboveThresholdHistorizableHandler("test", -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void doNotAcceptZeroAsThresholdValue() throws Exception {
    new AboveThresholdHistorizableHandler("test", 0);
  }


  private class HistorizableExtractorVisitor extends DoNothingReportVisitor {
    private String nameToFetch;

    private HistorizableList extractedList;

    HistorizableExtractorVisitor(String nameToFetch) {
      this.nameToFetch = nameToFetch;
    }

    @Override
    public void reportHistorizableList(HistorizableList historizableList) {
      if (nameToFetch.equals(historizableList.getName())) {
        extractedList = historizableList;
      }
    }

    public HistorizableList getExtractedList() {
      return extractedList;
    }

  }

}
