package de.is24.util.monitoring.agent;

import de.is24.util.monitoring.CorePlugin;
import de.is24.util.monitoring.InApplicationMonitor;
import de.is24.util.monitoring.keyhandler.DefaultKeyEscaper;
import de.is24.util.monitoring.keyhandler.KeyHandler;
import de.is24.util.monitoring.state2graphite.StateValuesToGraphite;
import java.lang.instrument.Instrumentation;


/**
 * This is a java agent implementation, which allows appmon4j instrumentation without changing the application code.
 *
 * As a first step, this agent allows to configure the JMXExporter and a StateValue2Graphite Plugin.
 *
 * In later steps this agent will also provide bytecode modification of classes to be monitored before
 * the class is actually loaded by any classloader.
 * <p>
 * You have to specify a configuration file to use for configuration of the agent:
 * <p>
 * <code>
 * java -javaagent:&lt;path-to-appmon4j-agent.jar&gt;=&lt;config-file&gt; &lt;further-parameters&gt; &lt;MainClassName&gt;
 * </code>
 * <p>
 *
 */
public class Appmon4JAgent /*implements ClassFileTransformer*/ {
  private CorePlugin corePlugin;

  /**
   * Constructor - creates a new Appmon4JAgent
   *
   * @param configuration the Appmon4JAgentConfiguration to use to set up the agent.
   *        Must not be null!
   */
  protected Appmon4JAgent(final Appmon4JAgentConfiguration configuration, final Instrumentation instrumentation) {
    initializeInApplicationMonitorAndCorePlugin();

    if (configuration.isJmxExportConfigured()) {
      log("Will configure JMX Exporter, which in turn will configure InApplicationMonitor ...");

      String jmxExporterSource = configuration.getJmxExporterSource();
      if (configuration.isJmxExportFile()) {
        log("... config JMXExporter from file " + jmxExporterSource);
        corePlugin.readJMXExporterPatternFromFile(jmxExporterSource);
      } else {
        log("... config JMXExporter from directory " + jmxExporterSource);
        corePlugin.readJMXExporterPatternFromDir(jmxExporterSource);
      }
      log("... JMX Exporter configured successfully.");
    } else {
      log("JMX Exporter not configured.");
    }

    if (configuration.isGraphiteConfigured()) {
      log("Will configure StateValuesToGraphite Plugin ...");
      new StateValuesToGraphite(
        configuration.getGraphiteHost(),
        configuration.getGraphitePort(),
        configuration.getGraphiteAppNamePrefix());
      log("... StateValuesToGraphite configured.");
    } else {
      log("StateValuesToGraphite not configured.");
    }

    /*
    if (configuration.isInstrumentationConfigured()) {
      instrumentation.addTransformer(transformer);
    }
    */

  }

  private void log(String message) {
    System.out.println("Appmon4jAgent: " + message);
  }

  private synchronized void initializeInApplicationMonitorAndCorePlugin() {
    if (corePlugin == null) {
      KeyHandler keyHandler = getKeyHandler();
      corePlugin = new CorePlugin(null, keyHandler);
      InApplicationMonitor.initInstance(corePlugin, keyHandler);
    }
  }

  private KeyHandler getKeyHandler() {
    return new DefaultKeyEscaper();
  }

  /**
  * @see java.lang.instrument.ClassFileTransformer#transform(ClassLoader, String,
  *      Class, java.security.ProtectionDomain, byte[])
  * /
  public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
                          final ProtectionDomain protectionDomain, final byte[] classfileBuffer)
                   throws IllegalClassFormatException {
    byte[] instrumentedBytecode = classfileBuffer;

    throw new RuntimeException("not yet used");
    //return instrumentedBytecode;
  }
  */

  /**
   * The premain method to be implemented by java agents to provide an entry point for the instrumentation.
   *
   * @param agentArgs arguments passed to the agent.
   * @param instrumentation the Instrumentation.
   */
  public static void premain(final String agentArgs, final Instrumentation instrumentation) {
    System.out.println("Initialiting Appmon4jAgent");

    Appmon4JAgentConfiguration configuration = null;
    try {
      configuration = Appmon4JAgentConfiguration.load(agentArgs);
    } catch (Exception e) {
      System.err.println("failed to load appmon4j agent configuration from " + agentArgs + ".");
      e.printStackTrace();
    }

    if (configuration != null) {
      new Appmon4JAgent(configuration, instrumentation);
    } else {
      System.err.println("appmon4j Agent: Unable to start up. No valid configuration found. Will do nothing. ");
    }
  }
}
