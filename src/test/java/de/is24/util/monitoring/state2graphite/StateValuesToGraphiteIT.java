package de.is24.util.monitoring.state2graphite;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.InApplicationMonitorRule;
import de.is24.util.monitoring.MultiValueProvider;
import de.is24.util.monitoring.ReportVisitor;
import de.is24.util.monitoring.SimpleStateValueProvider;
import de.is24.util.monitoring.State;
import de.is24.util.monitoring.StateValueProvider;
import de.is24.util.monitoring.tools.LocalHostNameResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class StateValuesToGraphiteIT {
  @Rule
  public final InApplicationMonitorRule inApplicationMonitorRule = new InApplicationMonitorRule();
  private GraphiteConnection graphiteConnection;
  private StateValuesToGraphite target;


  @Before
  public void setUp() throws Exception {
    LocalHostNameResolver localHostNameResolver = mock(LocalHostNameResolver.class);
    when(localHostNameResolver.getLocalHostName()).thenReturn("testHost");

    graphiteConnection = mock(GraphiteConnection.class);
    target = new StateValuesToGraphite("testAppName", localHostNameResolver, graphiteConnection);
    InApplicationMonitor.getInstance().registerStateValue(new SimpleStateValueProvider("StateTest", 4711));
  }

  @After
  public void tearDown() {
    target.shutdown();
  }

  @Test
  public void useAppNameHostNameAndStateAsPrefix() throws Exception {
    // wait for 2 seconds
    Thread.sleep(2000);
    verify(graphiteConnection, times(1)).send(startsWith("testAppName.testHost.states."));
  }

  @Test
  public void useGraphiteFormatting() throws Exception {
    // wait for 2 seconds
    Thread.sleep(2000);
    verify(graphiteConnection, times(1)).send(contains("testAppName.testHost.states.StateTest 4711 "));
  }

  @Test
  public void exceptionFromStateValueProviderShouldNotKillJob() throws Exception {
    InApplicationMonitor.getInstance().registerStateValue(new StateValueProvider() {
        @Override
        public long getValue() {
          throw new RuntimeException("fail");
        }

        @Override
        public String getName() {
          return "will.fail";
        }
      });

    // wait for 12 seconds to ensure failing state value has been called at least once
    // and Scheduler is still running
    Thread.sleep(12000);
    verify(graphiteConnection, times(2)).send(contains("testAppName.testHost.states.StateTest 4711 "));
  }

  @Test
  public void handleMultiValueProviders() throws Exception {
    final List<State> states = new ArrayList<State>();
    final String multiValueName = "test.multi.value";
    states.add(new State(multiValueName, "test1", 123432));
    states.add(new State(multiValueName, "test2", 98765));

    InApplicationMonitor.getInstance().getCorePlugin().registerMultiValueProvider(new MultiValueProvider() {
        @Override
        public Collection<State> getValues() {
          return states;
        }

        @Override
        public String getName() {
          return multiValueName;
        }

        @Override
        public void accept(ReportVisitor visitor) {
        }
      });

    // wait for 12 seconds to ensure failing state value has been called at least once
    // and Scheduler is still running
    Thread.sleep(2000);
    verify(graphiteConnection, times(1)).send(contains("testAppName.testHost.states.StateTest 4711 "));
    verify(graphiteConnection, times(1)).send(contains("testAppName.testHost.states.test.multi.value.test1 123432 "));
    verify(graphiteConnection, times(1)).send(contains("testAppName.testHost.states.test.multi.value.test2 98765 "));
  }
}
