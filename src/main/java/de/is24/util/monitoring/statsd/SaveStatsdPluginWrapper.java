package de.is24.util.monitoring.statsd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class wraps around creation of a StatsdPlugin, to allow the application to start,
 * even if the StatsdPlugin can not be initialized due to an error during DNS resolving of the
 * statsd host given. In this case no StatsdPlugin will be installed, a warning will be logged.
 * This is helpfull of you want to use spring xml initialization and do not want the Application
 * Context to stop starting due to an error while setting up monitoring.
 */
public class SaveStatsdPluginWrapper {
  private static final Logger LOGGER = LoggerFactory.getLogger(SaveStatsdPluginWrapper.class);
  private StatsdPlugin instance;


  public SaveStatsdPluginWrapper(String host, int port, String appName) {
    this(host, port, appName, 1.0);
  }

  public SaveStatsdPluginWrapper(String host, int port, String appName, double sampleRate) {
    try {
      instance = new StatsdPlugin(host, port, appName, sampleRate);
    } catch (Exception e) {
      LOGGER.warn("Error instantiating StatsdPlugin", e);
    }
  }

  public SaveStatsdPluginWrapper(String host, int port, StatsdMessageFormatter statsdMessageFormatter) {
    try {
      instance = new StatsdPlugin(host, port, statsdMessageFormatter);
    } catch (Exception e) {
      LOGGER.warn("Error instantiating StatsdPlugin", e);
    }
  }

  public SaveStatsdPluginWrapper(String host, int port, double sampleRate,
                                 StatsdMessageFormatter statsdMessageFormatter) {
    try {
      instance = new StatsdPlugin(host, port, sampleRate, statsdMessageFormatter);
    } catch (Exception e) {
      LOGGER.warn("Error instantiating StatsdPlugin", e);
    }
  }

  public void register() {
    if (instance != null) {
      instance.register();
    } else {
      LOGGER.warn("registration of SaveStatsdPluginWrapper failed, StatsdPlugin not instantiated");
    }
  }
}
