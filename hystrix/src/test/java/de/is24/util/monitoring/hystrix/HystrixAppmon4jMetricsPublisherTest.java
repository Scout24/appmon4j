package de.is24.util.monitoring.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.strategy.HystrixPlugins;
import de.is24.util.monitoring.CheckStateVisitor;
import de.is24.util.monitoring.CorePlugin;
import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.TestHelper;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class HystrixAppmon4jMetricsPublisherTest {
  @Test
  public void smoke_test() {
    TestHelper.resetInstanceForTesting();

    CorePlugin corePlugin = InApplicationMonitor.getInstance().getCorePlugin();
    HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixAppmon4jMetricsPublisher(corePlugin));

    new HystrixTestCommand().execute();

    CheckStateVisitor checkStateVisitor = new CheckStateVisitor("HystrixCommand.HystrixTestCommand.countSuccess");
    corePlugin.reportInto(checkStateVisitor);
    assertThat(checkStateVisitor.isFound(), is(true));
    assertThat(checkStateVisitor.getValue(), equalTo(1L));
  }

  private static class HystrixTestCommand extends HystrixCommand<Void> {
    public HystrixTestCommand() {
      super(HystrixCommandGroupKey.Factory.asKey("HystrixTestCommand"));
    }

    @Override
    protected Void run() throws Exception {
      return null;
    }
  }

}
