package de.is24.util.monitoring.status;

import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.Reportable;
import de.is24.util.monitoring.ReportableObserver;
import org.apache.log4j.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ServiceStateManager implements ReportableObserver {
  private static final Logger LOGGER = Logger.getLogger(ServiceStateManager.class);
  private ScheduledExecutorService ex;
  private Map<String, ServiceState> serviceStates;

  public ServiceStateManager() {
    ex = Executors.newSingleThreadScheduledExecutor();
    ex.scheduleAtFixedRate(new ServiceStateJob(), 5, 60, TimeUnit.SECONDS);
    serviceStates = new ConcurrentHashMap<String, ServiceState>();
  }

  public void addServiceState(ServiceState serviceState) {
    serviceStates.put(serviceState.getName(), serviceState);
  }

  /**
   * after adding all serviceState objects once call this method to connect ServiceStates with Monitors
   */
  public void initialize() {
    InApplicationMonitor.getInstance().getCorePlugin().addReportableObserver(this);
  }

  @Override
  public void addNewReportable(Reportable reportable) {
    for (ServiceState state : serviceStates.values()) {
      state.checkForReportable(reportable);
    }
  }

  public void shutdown() {
    InApplicationMonitor.getInstance().getCorePlugin().removeReportableObserver(this);
    ex.shutdown();
  }

  private class ServiceStateJob implements Runnable {
    @Override
    public void run() {
      LOGGER.debug("updating service state");
      for (ServiceState state : serviceStates.values()) {
        LOGGER.debug("checking " + state.getName());
        state.check();
      }
      LOGGER.debug("done with service state");
    }
  }
}
