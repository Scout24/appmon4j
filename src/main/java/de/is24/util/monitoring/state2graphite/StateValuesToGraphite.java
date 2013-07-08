package de.is24.util.monitoring.state2graphite;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.MultiValueProvider;
import de.is24.util.monitoring.Reportable;
import de.is24.util.monitoring.ReportableObserver;
import de.is24.util.monitoring.State;
import de.is24.util.monitoring.StateValueProvider;
import de.is24.util.monitoring.tools.LocalHostNameResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class StateValuesToGraphite implements ReportableObserver {
  private static final Logger LOGGER = LoggerFactory.getLogger(StateValuesToGraphite.class);
  private ScheduledExecutorService ex;
  private Map<String, StateValueProvider> stateValues;
  private Map<String, MultiValueProvider> multiValueProviders;
  private GraphiteConnection graphiteClient;

  public StateValuesToGraphite(String graphiteHost, int graphitePort, String appName) {
    this(appName, new LocalHostNameResolver(), new GraphiteConnection(graphiteHost, graphitePort));
  }


  StateValuesToGraphite(String appName, LocalHostNameResolver localHostNameResolver,
                        GraphiteConnection graphiteClient) {
    this.graphiteClient = graphiteClient;

    String keyPrefix = appName + "." + localHostNameResolver.getLocalHostName() + ".states";
    stateValues = new ConcurrentHashMap<String, StateValueProvider>();
    multiValueProviders = new ConcurrentHashMap<String, MultiValueProvider>();
    InApplicationMonitor.getInstance().getCorePlugin().addReportableObserver(this);
    ex = Executors.newSingleThreadScheduledExecutor();
    ex.scheduleAtFixedRate(new ReportStateValuesJob(graphiteClient, keyPrefix), 1, 10, TimeUnit.SECONDS);
  }


  @Override
  public void addNewReportable(Reportable reportable) {
    if (reportable instanceof StateValueProvider) {
      stateValues.put(reportable.getName(), (StateValueProvider) reportable);
    } else if (reportable instanceof MultiValueProvider) {
      multiValueProviders.put(reportable.getName(), (MultiValueProvider) reportable);
    }
  }


  public void shutdown() {
    InApplicationMonitor.getInstance().getCorePlugin().removeReportableObserver(this);
    ex.shutdown();
  }

  private class ReportStateValuesJob implements Runnable {
    private final GraphiteConnection graphiteClient;
    private final String keyPrefix;

    public ReportStateValuesJob(GraphiteConnection graphiteClient, String keyPrefix) {
      this.graphiteClient = graphiteClient;
      this.keyPrefix = keyPrefix;
    }

    @Override
    public void run() {
      LOGGER.debug("writing {} state values to graphite", stateValues.size());

      Long curTimeInSec = System.currentTimeMillis() / 1000;
      StringBuilder lines = new StringBuilder();

      addStateValues(curTimeInSec, lines);
      addMultiStates(curTimeInSec, lines);
      graphiteClient.send(lines.toString());
    }

    private void addStateValues(Long curTimeInSec, StringBuilder lines) {
      for (StateValueProvider stateValueProvider : stateValues.values()) {
        try {
          StringBuilder line = new StringBuilder();
          line.append(keyPrefix)
          .append(".")
          .append(stateValueProvider.getName())
          .append(" ")
          .append(stateValueProvider.getValue())
          .append(" ")
          .append(curTimeInSec)
          .append("\n");

          // only add complete lines if getValue throws Exception
          lines.append(line.toString());
        } catch (Exception e) {
          LOGGER.warn("getting StateValue failed for {}", stateValueProvider.getName());
        }
      }
    }

    private void addMultiStates(Long curTimeInSec, StringBuilder lines) {
      for (MultiValueProvider multiValueProvider : multiValueProviders.values()) {
        Collection<State> values = multiValueProvider.getValues();
        for (State state : values) {
          StringBuilder line = new StringBuilder();
          line.append(keyPrefix)
          .append(".")
          .append(multiValueProvider.getName())
          .append(".")
          .append(state.name)
          .append(" ")
          .append(state.value)
          .append(" ")
          .append(curTimeInSec)
          .append("\n");

          lines.append(line.toString());
        }
      }
    }

  }

  @Override
  public String toString() {
    return "StateValuesToGraphite:" + graphiteClient.toString();
  }
}
