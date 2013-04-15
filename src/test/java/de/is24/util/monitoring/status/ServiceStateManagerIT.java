package de.is24.util.monitoring.status;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.StateValueProvider;
import de.is24.util.monitoring.TestHelper;
import de.is24.util.monitoring.tools.DoNothingReportVisitor;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class ServiceStateManagerIT {
  private static final Logger LOGGER = Logger.getLogger(ServiceStateManagerIT.class);
  private ServiceStateManager serviceStateManager;

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
    serviceStateManager = new ServiceStateManager();
  }

  @After
  public void tearDown() {
    serviceStateManager.shutdown();
  }

  @Test
  public void observeReportables() throws Exception {
    ServiceState serviceState = new ServiceState("lala", "lala.ok", "lala.fail");
    serviceStateManager.addServiceState(serviceState);
    InApplicationMonitor.getInstance().incrementCounter("lala.ok");
    InApplicationMonitor.getInstance().incrementCounter("lala.fail");
    serviceStateManager.initialize();
    Thread.sleep(10 * 1000);
    InApplicationMonitor.getInstance().incrementCounter("lala.ok");
    InApplicationMonitor.getInstance().incrementCounter("lala.fail");
    Thread.sleep(60 * 1000);

    StateValueExtractorReportVisitor stateValueExtractorReportVisitor = new StateValueExtractorReportVisitor(
      "lala.oneMinuteFailureRate");
    InApplicationMonitor.getInstance().reportInto(stateValueExtractorReportVisitor);
    assertThat(stateValueExtractorReportVisitor.getValue(), is(50L));

    InApplicationMonitor.getInstance().incrementCounter("lala.ok");
    Thread.sleep(60 * 1000);
    InApplicationMonitor.getInstance().reportInto(stateValueExtractorReportVisitor);
    assertThat(stateValueExtractorReportVisitor.getValue(), is(0L));


  }

  private class StateValueExtractorReportVisitor extends DoNothingReportVisitor {
    private String stateValueName;
    private long value;

    StateValueExtractorReportVisitor(String stateValueName) {
      this.stateValueName = stateValueName;
    }

    @Override
    public void reportStateValue(StateValueProvider stateValueProvider) {
      if (stateValueProvider.getName().equals(stateValueName)) {
        this.value = stateValueProvider.getValue();
      }
    }

    public long getValue() {
      return value;
    }
  }

}
