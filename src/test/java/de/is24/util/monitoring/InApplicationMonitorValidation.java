package de.is24.util.monitoring;

import java.util.Random;
import org.apache.log4j.Logger;
import de.is24.util.monitoring.visitors.HierarchyReportVisitor;
import de.is24.util.monitoring.visitors.StringWriterReportVisitor;
import de.is24.util.monitoring.visitors.ValueOrderedReportVisitor;
import org.junit.BeforeClass;


/**
 * @author oschmitz
 */
public class InApplicationMonitorValidation {
  private static final Logger LOGGER = Logger.getLogger(InApplicationMonitorValidation.class);


  @org.junit.BeforeClass
  public static void setupClass() {
    InApplicationMonitor instance = InApplicationMonitor.getInstance();
    instance.registerStateValue(new StateValueProvider() {
        /**
         *
         */
        Random rand = new Random();

        public String getName() {
          return "test1";
        }

        /**
         *
         */

        public long getValue() {
          LOGGER.debug("+++ entering InApplicationMonitorValidation.getValue +++");
          return rand.nextInt(300);
        }

      });

    instance.incrementCounter("test1");
    instance.incrementCounter("test1");
    instance.incrementCounter("test1");

    instance.incrementCounter("test2");
    instance.incrementCounter("test2");

    instance.incrementCounter("test1");
    instance.incrementCounter("test3");

    instance.addTimerMeasurement("test1", 200);
    instance.addTimerMeasurement("test1", 150);
    instance.addTimerMeasurement("test1", 100);
    instance.addTimerMeasurement("test1", 150);

  }

  @org.junit.Test
  public void testStringHierarchy() {
    InApplicationMonitor instance = InApplicationMonitor.getInstance();

    StringWriterReportVisitor visitor = new StringWriterReportVisitor();
    instance.reportInto(visitor);

    String result = visitor.toString();
    LOGGER.info(result);
    System.out.println(result);
  }

  @org.junit.Test
  public void testValueOrderedReportVisitor() throws Exception {
    InApplicationMonitor instance = InApplicationMonitor.getInstance();

    ValueOrderedReportVisitor visitor = new ValueOrderedReportVisitor();
    instance.reportInto(visitor);

    String result = visitor.toString();
    LOGGER.info(result);
    System.out.println(result);
  }

  @org.junit.Test
  public void testStringHiararchy() throws Exception {
    InApplicationMonitor instance = InApplicationMonitor.getInstance();

    HierarchyReportVisitor visitor = new HierarchyReportVisitor();
    instance.reportInto(visitor);

    String result = visitor.toString();
    LOGGER.info(result);
    System.out.println(result);
  }
}
