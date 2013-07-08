package de.is24.util.monitoring;

import de.is24.util.monitoring.visitors.HierarchyReportVisitor;
import de.is24.util.monitoring.visitors.StringWriterReportVisitor;
import de.is24.util.monitoring.visitors.ValueOrderedReportVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Random;


/**
 * @author oschmitz
 */
public class InApplicationMonitorValidation {
  private static final Logger LOGGER = LoggerFactory.getLogger(InApplicationMonitorValidation.class);


  @org.junit.BeforeClass
  public static void setupClass() {
    InApplicationMonitor instance = InApplicationMonitor.getInstance();
    instance.registerStateValue(new StateValueProvider() {
        /**
         *
         */
        Random rand = new Random();

        @Override
        public String getName() {
          return "test1";
        }

        /**
         *
         */

        @Override
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
  public void testStringReportVisitor() {
    InApplicationMonitor instance = InApplicationMonitor.getInstance();

    StringWriterReportVisitor visitor = new StringWriterReportVisitor();
    instance.getCorePlugin().reportInto(visitor);

    String result = visitor.toString();
    LOGGER.info(result);
    System.out.println(result);
  }

  @org.junit.Test
  public void testValueOrderedReportVisitor() throws Exception {
    InApplicationMonitor instance = InApplicationMonitor.getInstance();

    ValueOrderedReportVisitor visitor = new ValueOrderedReportVisitor();
    instance.getCorePlugin().reportInto(visitor);

    String result = visitor.toString();
    LOGGER.info(result);
    System.out.println(result);
  }

  @org.junit.Test
  public void testHierarchyReportVisitor() throws Exception {
    InApplicationMonitor instance = InApplicationMonitor.getInstance();

    HierarchyReportVisitor visitor = new HierarchyReportVisitor();
    instance.getCorePlugin().reportInto(visitor);

    String result = visitor.toString();
    LOGGER.info(result);
    System.out.println(result);
  }

}
