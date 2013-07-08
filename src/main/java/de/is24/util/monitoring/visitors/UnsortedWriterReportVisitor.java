package de.is24.util.monitoring.visitors;

import de.is24.util.monitoring.Counter;
import de.is24.util.monitoring.Historizable;
import de.is24.util.monitoring.HistorizableList;
import de.is24.util.monitoring.MultiValueProvider;
import de.is24.util.monitoring.ReportVisitor;
import de.is24.util.monitoring.State;
import de.is24.util.monitoring.StateValueProvider;
import de.is24.util.monitoring.Timer;
import de.is24.util.monitoring.Version;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;


/**
 * @author oschmitz
 */
public abstract class UnsortedWriterReportVisitor implements ReportVisitor {
  private static final Logger LOGGER = Logger.getLogger(UnsortedWriterReportVisitor.class);
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");

  protected Writer writer;

  public UnsortedWriterReportVisitor(Writer writer) {
    this.writer = writer;
  }

  protected void writeStringToWriter(String toWrite) {
    try {
      writer.write(toWrite);
      writer.write("\n");
    } catch (IOException e) {
      LOGGER.error("hoppla", e);
    }
  }

  public void reportCounter(Counter counter) {
    LOGGER.debug("+++ entering UnsortedWriterReportVisitor.reportCounter +++");

    String result = counter.getName() + " counter :" + counter.getCount();
    writeStringToWriter(result);
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.ReportVisitor#reportTimer(de.is24.util.monitoring.Timer)
   */
  public void reportTimer(Timer timer) {
    LOGGER.debug("+++ entering UnsortedWriterReportVisitor.reportTimer +++");

    long count = timer.getCount();
    long timersum = timer.getTimerSum();
    String result = timer.getName() + " timer : count = " + count + ", timerSum = " + timersum + ", mean = " +
      ((double) timersum / count);
    writeStringToWriter(result);
  }

  /* (non-Javadoc)
   * @see de.is24.util.monitoring.ReportVisitor#reportStateValue(de.is24.util.monitoring.StateValueProvider)
   */
  public void reportStateValue(StateValueProvider stateValueProvider) {
    LOGGER.debug("+++ entering UnsortedWriterReportVisitor.reportStateValue +++");

    String result = stateValueProvider.getName() + " state : " + stateValueProvider.getValue();
    writeStringToWriter(result);
  }

  @Override
  public void reportMultiValue(MultiValueProvider multiValueProvider) {
    LOGGER.debug("+++ entering UnsortedWriterReportVisitor.reportMultiValue +++");

    Collection<State> values = multiValueProvider.getValues();
    for (State state : values) {
      String result = state.name + " multi_state : " + state.value;
      writeStringToWriter(result);
    }
  }

  /* (non-Javadoc)
  * @see de.is24.util.monitoring.ReportVisitor#reportVersion(de.is24.util.monitoring.Version)
  */
  public void reportVersion(Version aVersion) {
    LOGGER.debug("+++ enter UnsortedWriterReportVisitor.reportVersion+++");

    String result = aVersion.getName() + " version : " + aVersion.getValue();
    writeStringToWriter(result);
  }

  /**
   *
   */
  public void reportHistorizableList(HistorizableList aHistorizableList) {
    LOGGER.debug("+++ entering UnsortedWriterReportVisitor.reportHistorizableList +++");

    StringBuffer result = new StringBuffer();
    result.append(aHistorizableList.getName()).append(" historizable :\n");
    for (Historizable historizable : aHistorizableList) {
      String timestamp = getFormattedDate(historizable);
      result.append(timestamp).append(" ");
      result.append(historizable.getValue()).append("\n");
    }
    writeStringToWriter(result.toString());
  }

  private String getFormattedDate(final Historizable historizable) {
    long milliSecondsSince1970 = historizable.getTimestamp().getTime();
    DateTime dateTime = new DateTime().withMillis(milliSecondsSince1970);
    return dateTime.toString(DATE_FORMAT);
  }
}
