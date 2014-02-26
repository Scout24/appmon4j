
package de.is24.util.monitoring;

import de.is24.util.monitoring.keyhandler.DefaultKeyEscaper;
import de.is24.util.monitoring.keyhandler.KeyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 *
 * This is the central class of appmon4j.<br>
 * appmon4j is a lightweight, easy to use in application monitoring system
 * allowing measurements of "real traffic" performance values
 * in high throughput java applications.<br><br>
 *
 * This class is an "old school" singleton, which is accessed by using
 * the static getInstance() method.
 *
 * @author OSchmitz
 */
public class InApplicationMonitor {
  private static final Logger LOGGER = LoggerFactory.getLogger(InApplicationMonitor.class);
  protected static final Object semaphore = new Object();


  private volatile boolean monitorActive = true;
  private final CopyOnWriteArrayList<MonitorPlugin> plugins = new CopyOnWriteArrayList<MonitorPlugin>();

  private volatile KeyHandler keyHandler;
  private volatile CorePlugin corePlugin;
  protected static InApplicationMonitor instance;

  static {
    KeyHandler keyHandler = new DefaultKeyEscaper();
    CorePlugin corePlugin = new CorePlugin(null, keyHandler);
    instance = new InApplicationMonitor(corePlugin, keyHandler);
  }

  protected InApplicationMonitor(CorePlugin corePlugin, KeyHandler keyHandler) {
    this.keyHandler = keyHandler;
    this.corePlugin = corePlugin;
    registerPlugin(corePlugin);
  }


  /**
  * Delivers the Singleton instance of InApplicationMonitor.
  *
  * @return InApplicationMonitor Singleton
  */
  public static InApplicationMonitor getInstance() {
    return instance;
  }

  public static InApplicationMonitor initInstance(CorePlugin corePlugin, KeyHandler keyHandler) {
    CorePlugin previousCorePlugin;
    synchronized (semaphore) {
      LOGGER.info("+++ initializing InApplicationMonitor() +++");

      instance.keyHandler = keyHandler;

      LOGGER.info("syncing from previous core plugin");
      previousCorePlugin = instance.corePlugin;
      corePlugin.syncFrom(previousCorePlugin);

      instance.plugins.add(corePlugin);
      instance.plugins.remove(previousCorePlugin);
      instance.corePlugin = corePlugin;
      LOGGER.info("InApplicationMonitor updated successfully.");
    }
    if ((previousCorePlugin != null) && (previousCorePlugin != corePlugin)) {
      previousCorePlugin.destroy();
    }

    return instance;
  }


  /**
   * @see #isMonitorActive
   */
  public void activate() {
    monitorActive = true;
  }

  /**
   * @see #isMonitorActive
   */
  public void deactivate() {
    monitorActive = false;
  }

  /**
   * If true, monitoring is active.
   * If false incrementCounter and addTimer calls
   * will return without doing anything (thus not synchronizing on any resource).
   * Initialize calls will be processed however.
   * registerStateValue, registerVersion and add Historizable will be processed, too.
   * @return true if the monitor is currently active, false if not
   */
  public boolean isMonitorActive() {
    return monitorActive;
  }


  /**
   * @return Number of entries to keep for each Historizable list.
   * @deprecated use corePlugin directly, will be removed from InApplicationMonitor
   */
  @Deprecated
  public int getMaxHistoryEntriesToKeep() {
    return getCorePlugin().getMaxHistoryEntriesToKeep();
  }

  /**
   * Set the Number of entries to keep for each Historizable list.
   * Default is 5.
   * @deprecated use corePlugin directly, will be removed from InApplicationMonitor
   * @param aMaxHistoryEntriesToKeep Number of entries to keep
   */
  @Deprecated
  public void setMaxHistoryEntriesToKeep(int aMaxHistoryEntriesToKeep) {
    getCorePlugin().setMaxHistoryEntriesToKeep(aMaxHistoryEntriesToKeep);
  }

  /**
   * adds a new ReportableObserver that wants to be notified about new Reportables that are
   * registered on the InApplicationMonitor
   * @deprecated use corePlugin directly, will be removed from InApplicationMonitor
   * @param reportableObserver the class that wants to be notified
   */
  @Deprecated
  public void addReportableObserver(final ReportableObserver reportableObserver) {
    getCorePlugin().addReportableObserver(reportableObserver);
  }


  /**
   * Allow disconnection of observers, mainly for testing
   * @deprecated use corePlugin directly, will be removed from InApplicationMonitor
   * @param reportableObserver
   */
  @Deprecated
  public void removeReportableObserver(final ReportableObserver reportableObserver) {
    getCorePlugin().removeReportableObserver(reportableObserver);
  }

  /**
   * Implements the {@link InApplicationMonitor} side of the Visitor pattern.
   * Iterates through all registered {@link Reportable} instances and calls
   * the corresponding method on the {@link ReportVisitor} implementation.
   * @param reportVisitor The {@link ReportVisitor} instance that shall be visited
   * by all regieteres {@link Reportable} instances.
   * @deprecated use corePlugin directly, will be removed from InApplicationMonitor
   */
  @Deprecated
  public void reportInto(ReportVisitor reportVisitor) {
    getCorePlugin().reportInto(reportVisitor);
  }

  /**
   * Increment the named {@link Counter} by one.
   * @param name name of the {@link Counter} to increment
   */
  public void incrementCounter(String name) {
    incrementCounter(name, 1);
  }

  /**
   * Increment the named {@link Counter} by one.
   * Using this method instead of incrementCounter is a hint to some plugins
   * that this is an event that may happen very often. Plugins may use sampling to
   * to limit load or network traffic.
   * @param name name of the {@link Counter} to increment
   */
  public void incrementHighRateCounter(String name) {
    if (monitorActive) {
      String escapedName = keyHandler.handle(name);
      for (MonitorPlugin p : getPlugins()) {
        p.incrementHighRateCounter(escapedName, 1);
      }
    }
  }

  /**
  * <p>Increase the specified counter by a variable amount.</p>
  *
  * @param   name
  *          the name of the {@code Counter} to increase
  * @param   increment
  *          the added to add
  */
  public void incrementCounter(String name, int increment) {
    if (monitorActive) {
      String escapedName = keyHandler.handle(name);
      for (MonitorPlugin p : getPlugins()) {
        p.incrementCounter(escapedName, increment);
      }
    }
  }

  /**
   * If you want to ensure existance of a counter, for example you want to prevent
   * spelling errors in an operational monitoring configuration, you may initialize a counter
   * using this method. The plugins will decide how to handle this initialization.
   * @param name the name of the counter to be initialized
   */
  public void initializeCounter(String name) {
    String escapedName = keyHandler.handle(name);
    for (MonitorPlugin p : getPlugins()) {
      p.initializeCounter(escapedName);
    }
  }

  /**
   * Add a timer measurement for the given name.
   * {@link Timer}s allow adding timer measurements, implicitly incrementing the count
   * Timers count and measure timed events.
   * The application decides which unit to use for timing.
   * Miliseconds are suggested and some {@link ReportVisitor} implementations
   * may imply this.
   *
   * @param name name of the {@link Timer}
   * @param timing number of elapsed time units for a single measurement
   */
  public void addTimerMeasurement(String name, long timing) {
    if (monitorActive) {
      String escapedName = keyHandler.handle(name);
      for (MonitorPlugin p : getPlugins()) {
        p.addTimerMeasurement(escapedName, timing);
      }
    }
  }

  /**
   * Add a timer measurement for a rarely occuring event with given name.
   * This allows Plugins to to react on the estimated rate of the event.
   * Namely the statsd plugin will not sent these, as the requires storage
   * is in no relation to the value of the data.
   * {@link Timer}s allow adding timer measurements, implicitly incrementing the count
   * Timers count and measure timed events.
   * The application decides which unit to use for timing.
   * Miliseconds are suggested and some {@link ReportVisitor} implementations
   * may imply this.
   *
   * @param name name of the {@link Timer}
   * @param timing number of elapsed time units for a single measurement
   */
  public void addSingleEventTimerMeasurement(String name, long timing) {
    if (monitorActive) {
      String escapedName = keyHandler.handle(name);
      for (MonitorPlugin p : getPlugins()) {
        p.addSingleEventTimerMeasurement(escapedName, timing);
      }
    }
  }

  /**
   * Add a timer measurement for a often occuring event with given name.
   * This allows Plugins to to react on the estimated rate of the event.
   * Namely the statsd plugin will use sampling on these, to reduce network traffic.
   * {@link Timer}s allow adding timer measurements, implicitly incrementing the count
   * Timers count and measure timed events.
   * The application decides which unit to use for timing.
   * Miliseconds are suggested and some {@link ReportVisitor} implementations
   * may imply this.
   *
   * @param name name of the {@link Timer}
   * @param timing number of elapsed time units for a single measurement
   */
  public void addHighRateTimerMeasurement(String name, long timing) {
    if (monitorActive) {
      String escapedName = keyHandler.handle(name);

      for (MonitorPlugin p : getPlugins()) {
        p.addHighRateTimerMeasurement(escapedName, timing);
      }
    }
  }


  /**
  * Add a timer measurement for the given name.
  * {@link Timer}s allow adding timer measurements, implicitly incrementing the count
  * Timers count and measure timed events.
  * The application decides which unit to use for timing.
  * Miliseconds are suggested and some {@link ReportVisitor} implementations
  * may imply this.
  *
  * @param name name of the {@link Timer}
  * @param begin number of elapsed time units at the beginning of the single measurement
  * @param end number of elapsed time units at the end of the single measurement
  */
  public void addTimerMeasurement(String name, long begin, long end) {
    addTimerMeasurement(name, end - begin);
  }

  /**
   * If you want to ensure existence of a timer, for example you want to prevent
   * spelling errors in an operational monitoring configuration, you may initialize a timer
   * using this method. The plugins will decide how to handle this initialization.
   * @param name the name of the timer to be initialized
   */
  public void initializeTimerMeasurement(String name) {
    String escapedName = keyHandler.handle(name);
    for (MonitorPlugin p : getPlugins()) {
      p.initializeTimerMeasurement(escapedName);
    }
  }

  /**
   * Add a state value provider to this appmon4j instance.
   * {@link StateValueProvider} instances allow access to a numeric
   * value (long), that is already available in the application.
   *
   * @param stateValueProvider the StateValueProvider instance to add
   */
  public void registerStateValue(StateValueProvider stateValueProvider) {
    getCorePlugin().registerStateValue(stateValueProvider);
  }

  /**
   * This method was intended to register module names with their
   * current version identifier.
   * This could / should actually be generalized into an non numeric
   * state value
   *
   * @param name name of the versionized "thing" (class, module etc.)
   * @param version identifier of the version
   */
  public void registerVersion(String name, String version) {
    Version versionToAdd = new Version(keyHandler.handle(name), version);
    getCorePlugin().registerVersion(versionToAdd);
  }

  /**
   * add a {@link Historizable} instance to the list identified by historizable.getName()
   *
   * @param historizable the historizable to add
   */
  public void addHistorizable(Historizable historizable) {
    getCorePlugin().addHistorizable(keyHandler.handle(historizable.getName()), historizable);
  }


  /**
   * Register a plugin to able to hook into monitoring with your own monitor.
   *
   * @param plugin the plugin to adapt a new monitor.
   */
  public void registerPlugin(MonitorPlugin plugin) {
    plugins.addIfAbsent(plugin);
  }

  public List<String> getRegisteredPluginKeys() {
    List<String> installedPluginKeys = new ArrayList<String>();
    for (MonitorPlugin plugin : getPlugins()) {
      installedPluginKeys.add(plugin.getUniqueName());
    }
    return installedPluginKeys;
  }

  public void removeAllPlugins() {
    getPlugins().clear();
    getPlugins().add(corePlugin);
  }


  protected KeyHandler getKeyHandler() {
    return keyHandler;
  }

  protected List<MonitorPlugin> getPlugins() {
    return plugins;
  }

  public CorePlugin getCorePlugin() {
    return corePlugin;
  }

  public void setThreadLocalState() {
    throw new UnsupportedOperationException(
      "setThreadLocalState not supported on production InApplicationMonitor, initialize Testing Version");
  }


  public void resetThreadLocalState() {
    throw new UnsupportedOperationException(
      "resetThreadLocalState not supported on production InApplicationMonitor, initialize Testing Version");
  }
}
