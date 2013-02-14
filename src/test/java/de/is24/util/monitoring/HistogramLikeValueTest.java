package de.is24.util.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Random;
import org.junit.Test;
import de.is24.util.monitoring.helper.HistogramLikeValue;
import de.is24.util.monitoring.visitors.HistogramLikeValueAnalysisVisitor;
import de.is24.util.monitoring.visitors.StringWriterReportVisitor;


public class HistogramLikeValueTest {
  private HistogramLikeValue histogramLikeValue;

  private void dump() {
    System.out.println("=====");

    StringWriterReportVisitor stringWriterReportVisitor = new StringWriterReportVisitor();
    InApplicationMonitor.getInstance().reportInto(stringWriterReportVisitor);
    System.out.println(stringWriterReportVisitor.toString());
  }

  private void assertBinSize(long lowerBinLimit, long expectedSize) {
    String binName = histogramLikeValue.getBaseName() + ".biggerThan" + lowerBinLimit;
    Counter binCounter = InApplicationMonitor.getInstance().getCorePlugin().getCounter(binName);
    assertNotNull(binCounter);
    assertEquals(expectedSize, binCounter.getCount());
  }


  @Test
  public void testGrouping() {
    histogramLikeValue = new HistogramLikeValue("groupingTest", 1000);
    histogramLikeValue.addValue(50);
    histogramLikeValue.addValue(250);
    histogramLikeValue.addValue(50);
    histogramLikeValue.addValue(1500);
    histogramLikeValue.addValue(1800);
    histogramLikeValue.addValue(750);

    dump();

    Timer timer = InApplicationMonitor.getInstance().getCorePlugin().getTimer("groupingTest.total");
    assertNotNull(timer);
    assertEquals(4400L, timer.getTimerSum());

    assertBinSize(0, 4L);
    assertBinSize(1000, 2L);
  }

  @Test
  public void testMaxLimit() {
    histogramLikeValue = new HistogramLikeValue("maxLimitTest", 1000, 5000);

    histogramLikeValue.addValue(50);
    histogramLikeValue.addValue(1100);
    histogramLikeValue.addValue(1300);
    histogramLikeValue.addValue(1600);
    histogramLikeValue.addValue(3000);
    histogramLikeValue.addValue(5500);
    histogramLikeValue.addValue(10500);

    dump();

    assertBinSize(0, 1L);
    assertBinSize(1000, 3L);
    assertBinSize(3000, 1L);

    String binName = histogramLikeValue.getBaseName() + ".biggerThan" + 5000;
    Counter binCounter = InApplicationMonitor.getInstance().getCorePlugin().getCounter(binName);
    assertNotNull(binCounter);
    assertEquals(2L, binCounter.getCount());
  }

  @Test
  public void testRecordedMaxLimit() {
    histogramLikeValue = new HistogramLikeValue("maxLimitTest", 1, 5);

    histogramLikeValue.addValue(1);
    histogramLikeValue.addValue(2);
    histogramLikeValue.addValue(6);
    dump();

    assertBinSize(1, 1);
    assertBinSize(2, 1);
    assertBinSize(5, 1);

    StateValueProvider stateValue = InApplicationMonitor.getInstance()
      .getCorePlugin()
      .getStateValue(
        "maxLimitTest.currentMax");
    assertNotNull(stateValue);
    assertEquals(6L, stateValue.getValue());

    histogramLikeValue.addValue(42);
    dump();

    assertEquals(42L, stateValue.getValue());
  }


  @Test
  public void testAnalysisVisitor() {
    histogramLikeValue = new HistogramLikeValue("testAnalysisVisitor", 1000, 5000);

    histogramLikeValue.addValue(500);
    histogramLikeValue.addValue(500);
    histogramLikeValue.addValue(500);
    histogramLikeValue.addValue(500);
    histogramLikeValue.addValue(500);
    histogramLikeValue.addValue(500);
    histogramLikeValue.addValue(50);
    histogramLikeValue.addValue(999);
    histogramLikeValue.addValue(1500);
    histogramLikeValue.addValue(50000);

    dump();

    HistogramLikeValueAnalysisVisitor histogramLikeValueAnalysisVisitor = new HistogramLikeValueAnalysisVisitor(
      histogramLikeValue.getBaseName());
    InApplicationMonitor.getInstance().reportInto(histogramLikeValueAnalysisVisitor);

    System.out.println(histogramLikeValueAnalysisVisitor.toString());

  }

  @Test
  public void testAnalysisVisitorNotRepeatable() {
    histogramLikeValue = new HistogramLikeValue("testAnalysisVisitor", 1000, 60000);


    Random r = new Random();
    for (int i = 0; i < 10000; i++) {
      float nextFloat = r.nextFloat();
      histogramLikeValue.addValue((long) (nextFloat * 60000));
    }
    histogramLikeValue.addValue(65000);

    dump();

    HistogramLikeValueAnalysisVisitor histogramLikeValueAnalysisVisitor = new HistogramLikeValueAnalysisVisitor(
      histogramLikeValue.getBaseName());
    InApplicationMonitor.getInstance().reportInto(histogramLikeValueAnalysisVisitor);

    System.out.println(histogramLikeValueAnalysisVisitor.toString());

  }

}
