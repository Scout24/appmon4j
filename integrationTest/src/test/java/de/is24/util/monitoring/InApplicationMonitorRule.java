package de.is24.util.monitoring;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;


/**
 * JUnit rule with which to safely set up, tear down & access an instance of {@link InApplicationMonitor}.
 *
 * @author <a href="mailto:sebastian.kirsch@immobilienscout24.de">Sebastian Kirsch</a>
 */
public class InApplicationMonitorRule extends TestWatcher {
  private InApplicationMonitor inApplicationMonitor;

  @Override
  protected void starting(Description description) {
    inApplicationMonitor = TestHelper.setInstanceForTesting();
  }

  @Override
  protected void finished(Description description) {
    inApplicationMonitor = null;
    TestHelper.resetInstanceForTesting();
  }

  public InApplicationMonitor getInApplicationMonitor() {
    if (inApplicationMonitor == null) {
      throw new IllegalStateException();
    }
    return inApplicationMonitor;
  }

}
