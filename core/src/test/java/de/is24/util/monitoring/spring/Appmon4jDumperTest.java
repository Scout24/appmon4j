package de.is24.util.monitoring.spring;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.InApplicationMonitorRule;
import de.is24.util.monitoring.SimpleStateValueProvider;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.NullAppender;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.GenericApplicationContext;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;


/**
 * Test for {@link Appmon4jDumper}.
 *
 * @author <a href="mailto:sebastian.kirsch@immobilienscout24.de">Sebastian Kirsch</a>
 */
public class Appmon4jDumperTest {
  @Rule
  public final InApplicationMonitorRule inApplicationMonitorRule = new InApplicationMonitorRule();
  private Appender appender;
  private final List<LoggingEvent> loggingEvents = new ArrayList<LoggingEvent>();

  @Before
  public void instrumentLoggingForTesting() {
    loggingEvents.clear();
    appender = new NullAppender() {
      @Override
      public void doAppend(LoggingEvent event) {
        loggingEvents.add(event);
      }
    };
    Logger.getLogger(Appmon4jDumper.class).addAppender(appender);
  }

  @After
  public void clearLoggingInstrumentation() {
    loggingEvents.clear();
    Logger.getLogger(Appmon4jDumper.class).removeAppender(appender);
  }

  @Test
  public void dumpContainsReportingValues() {
    InApplicationMonitor inApplicationMonitor = inApplicationMonitorRule.getInApplicationMonitor();
    String counterName = "DummyCounter";
    String timerName = "DummyTimer";
    String stateValueName = "DummyStateValue";
    inApplicationMonitor.incrementCounter(counterName);
    inApplicationMonitor.addTimerMeasurement(timerName, 42L);
    inApplicationMonitor.registerStateValue(new SimpleStateValueProvider(stateValueName, 42L));


    Appmon4jDumper objectUnderTest = new Appmon4jDumper(inApplicationMonitorRule.getInApplicationMonitor());
    objectUnderTest.onApplicationEvent(new ContextClosedEvent(new GenericApplicationContext()));

    assertThat(loggingEvents,
      Matchers.<LoggingEvent>hasItem(
        allOf(
          loggingEventWithMessageContaining(counterName),
          loggingEventWithMessageContaining(timerName),
          loggingEventWithMessageContaining(stateValueName))));
  }

  private Matcher<? super LoggingEvent> loggingEventWithMessageContaining(String messagePart) {
    return new FeatureMatcher<LoggingEvent, String>(containsString(messagePart),
      "LoggingEvent having message being", "message") {
      @Override
      protected String featureValueOf(LoggingEvent actual) {
        return actual.getRenderedMessage();
      }
    };
  }

}
