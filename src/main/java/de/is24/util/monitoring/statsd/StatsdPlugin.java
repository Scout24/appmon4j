package de.is24.util.monitoring.statsd;

import de.is24.util.monitoring.AbstractMonitorPlugin;
import java.net.SocketException;
import java.net.UnknownHostException;


/**
 * InApplicationMonitor plugin. Forwards monitoring to a Statsd server.
 *
 * Enable via InApplicationMonitor.getInstance().registerPlugin(new StatsdPlugin("myStatsdHost", 1234));
 */
public class StatsdPlugin extends AbstractMonitorPlugin {
  private final StatsdClient delegate;
  private final String uniqueName;
  private double sampleRate;
  private double highVolumeSampleRate;

  /**
  * Create a Statsd plugin with a specified host and port.
  * caller needs to call register on the plugin to register it into the InApplicationMonitor
  *
  * @param host the host of the Statsd server.
  * @param port the port of the Statsd server.
  * @param appName a short application identifier to fulfill IS24 / Graphite naming scheme requirements through statsd
  * @throws UnknownHostException if there is no such host as specified.
  * @throws SocketException if the socket to the host could not be opened.
  */
  public StatsdPlugin(String host, int port, String appName) throws UnknownHostException, SocketException {
    this(host, port, appName, 1.0);
  }

  /**
   * Create a Statsd plugin with a specified host and port.
   * caller needs to call register on the plugin to register it into the InApplicationMonitor
   *
   * @param host statsd host name
   * @param port udp port statsd is listening on
   * @param appName a short application identifier to fulfill IS24 / Graphite naming scheme requirements through statsd
   * @param sampleRate a default sample rate to use for all metrics handled
   * @throws UnknownHostException
   * @throws SocketException
   */
  public StatsdPlugin(String host, int port, String appName, double sampleRate) throws UnknownHostException,
                                                                                       SocketException {
    this(new StatsdClient(host, port, appName), getUniqeName(host, port, sampleRate), sampleRate);
  }

  public StatsdPlugin(String host, int port, StatsdMessageFormatter statsdMessageFormatter)
               throws SocketException, UnknownHostException {
    this(new StatsdClient(host, port, statsdMessageFormatter), getUniqeName(host, port, 1.0), 1.0);
  }

  public StatsdPlugin(String host, int port, double sampleRate, StatsdMessageFormatter statsdMessageFormatter)
               throws SocketException, UnknownHostException {
    this(new StatsdClient(host, port, statsdMessageFormatter), getUniqeName(host, port, sampleRate), sampleRate);
  }

  StatsdPlugin(StatsdClient client, String uniqeName, double sampleRate) {
    this.delegate = client;
    this.uniqueName = uniqeName;
    if (sampleRate < 0) {
      throw new IllegalArgumentException("negative sample rate not permitted");
    }
    this.sampleRate = sampleRate;
    initHighVolumeSampleRate();
  }

  private static String getUniqeName(final String host, final int port, final double sampleRate) {
    return "StatsdPlugin_" + host + "_" + port + "_" + sampleRate;
  }

  @Override
  public void afterRemovalNotification() {
    delegate.close();
  }

  private void initHighVolumeSampleRate() {
    this.highVolumeSampleRate = sampleRate * 0.1;
  }


  @Override
  public String getUniqueName() {
    return uniqueName;
  }

  /*
    Colons are used to separate values in calls to statsd,
    thus they should not be part of the key
     */
  private String sanitizeKey(String key) {
    return key.replaceAll(":", "_");
  }

  @Override
  public void incrementCounter(String key, int increment) {
    delegate.increment(sanitizeKey(key), increment, sampleRate);
  }

  @Override
  public void incrementHighRateCounter(String key, int increment) {
    delegate.increment(sanitizeKey(key), increment, highVolumeSampleRate);
  }

  @Override
  public void initializeCounter(String name) {
    // we do not initialize counters as this makes no sense for statsd / graphite
  }

  @Override
  public void addTimerMeasurement(String key, long timing) {
    delegate.timing(sanitizeKey(key), (int) timing, sampleRate);
  }

  @Override
  public void addSingleEventTimerMeasurement(String name, long timing) {
    // we do not write rare events to statsd, as this fills the harddrive of underlying
    // graphite service. with almost empty files.
  }

  @Override
  public void initializeTimerMeasurement(String name) {
    // we do not initialize Timers as this makes no sense for statsd / graphite
  }

  @Override
  public void addHighRateTimerMeasurement(String key, long timing) {
    delegate.timing(sanitizeKey(key), (int) timing, highVolumeSampleRate);
  }
}
