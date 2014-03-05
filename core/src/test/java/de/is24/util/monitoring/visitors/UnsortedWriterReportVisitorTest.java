package de.is24.util.monitoring.visitors;

import org.junit.Test;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class UnsortedWriterReportVisitorTest {
  @Test
  public void reportedHistorizableHasTimestamp() throws Exception {
    StringWriter stringWriter = new StringWriter();
    UnsortedWriterReportVisitor unsortedWriterReportVisitor = new UnsortedWriterReportVisitor(stringWriter) {
    };

    Calendar calendar = GregorianCalendar.getInstance();
    calendar.set(2014, 2, 30, 11, 59, 22);

    String formattedDate = unsortedWriterReportVisitor.getFormattedDate(calendar.getTime());
    assertThat(formattedDate, is("30.03.2014 11:59:22"));


  }
}
