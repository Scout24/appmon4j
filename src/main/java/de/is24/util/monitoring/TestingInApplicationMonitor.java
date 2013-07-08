package de.is24.util.monitoring;

import de.is24.util.monitoring.keyhandler.DefaultKeyEscaper;
import de.is24.util.monitoring.keyhandler.KeyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


// TODO move this class, and maybe some other tools to a dedicated Testing module and provide a dedicated jar
public class TestingInApplicationMonitor extends InApplicationMonitor {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestingInApplicationMonitor.class);
  private static final ThreadLocal<CorePlugin> threadLocalCorePlugin = new InheritableThreadLocal<CorePlugin>();
  private static final ThreadLocal<List<MonitorPlugin>> threadLocalPluginsList =
    new InheritableThreadLocal<List<MonitorPlugin>>();

  public TestingInApplicationMonitor(CorePlugin corePlugin, KeyHandler keyHandler) {
    super(corePlugin, keyHandler);
  }

  /**
   * This will fail if tests are run multi threaded use with utmost care.
   */
  public static InApplicationMonitor initInstanceForTesting(CorePlugin corePlugin, KeyHandler keyHandler) {
    synchronized (semaphore) {
      if (instance != null) {
        instance.getCorePlugin().destroy();
      }
      LOGGER.info("+++ Changing InApplicationMonitor() for Testing only +++");
      instance = new TestingInApplicationMonitor(corePlugin, keyHandler);
      LOGGER.info("InApplicationMonitor changed successfully.");
      return instance;
    }
  }

  /**
   * This will fail if tests are run multi threaded use with utmost care.
   */
  public static void resetInstanceForTesting() {
    synchronized (semaphore) {
      if (instance != null) {
        instance.getCorePlugin().destroy();
      }

      KeyHandler keyHandler = new DefaultKeyEscaper();
      CorePlugin corePlugin = new CorePlugin(null, keyHandler);
      instance = new TestingInApplicationMonitor(corePlugin, keyHandler);
      LOGGER.info("Reset InApplicationMonitor for Testing.");
    }
  }


  @Override
  public CorePlugin getCorePlugin() {
    CorePlugin result = threadLocalCorePlugin.get();
    if (result == null) {
      result = super.getCorePlugin();
    }
    return result;
  }

  @Override
  public List<MonitorPlugin> getPlugins() {
    List<MonitorPlugin> result = threadLocalPluginsList.get();
    if (result == null) {
      result = super.getPlugins();
    }
    return result;
  }


  /**
  * this method allows Tests running multi threaded to achieve some level of isolation against
  * activities from other threads. All methods handed over to the CorePlugin will be handled
  * by a dedicated CorePlugin instance for the calling thread, and all threads created
  * by this thread.
  *
  * So this is currently only helpfull for StateValueProviders, MultiValueProviders and Versions.
  *
  * This core plugin will even "survive" calls to @seet resetInstanceForTesting or @see initInstanceForTesting
  * calls in other threads.
  *
  * The dedicated CorePlugin will not be accessible via JMX, only the original CorePlugins data will be available
  * via JMX.
  */
  @Override
  public void setThreadLocalState() {
    CorePlugin corePlugin = new CorePlugin(null, getKeyHandler());
    threadLocalCorePlugin.set(corePlugin);

    CopyOnWriteArrayList<MonitorPlugin> monitorPlugins = new CopyOnWriteArrayList<MonitorPlugin>();
    monitorPlugins.add(corePlugin);
    threadLocalPluginsList.set(monitorPlugins);
  }

  @Override
  public void resetThreadLocalState() {
    threadLocalCorePlugin.remove();
    threadLocalPluginsList.remove();
  }
}
