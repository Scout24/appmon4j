package de.is24.util.monitoring.statsd;

import de.is24.util.monitoring.tools.LocalHostNameResolver;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;


class StatsdClient {
  private static final Logger LOG = Logger.getLogger(StatsdClient.class.getName());

  static Random rng = new Random();

  private final StatsdDatagrammSocket socket;
  private final String localHostName;
  private final String appName;

  public StatsdClient(String host, int port, String appName) throws UnknownHostException, SocketException {
    this(InetAddress.getByName(host), port, appName);
  }

  public StatsdClient(InetAddress host, int port, String appName) throws SocketException {
    this(new StatsdDatagrammSocket(host, port), new LocalHostNameResolver(), appName);
  }

  StatsdClient(StatsdDatagrammSocket socket, LocalHostNameResolver localHostNameResolver, String appName) {
    this.socket = socket;
    this.localHostName = localHostNameResolver.getLocalHostName().replaceAll("\\.", "_");
    this.appName = appName;
  }

  public void close() {
    socket.close();
  }

  public boolean timing(String key, int value) {
    return timing(key, value, 1.0);
  }

  public boolean timing(String key, int value, double sampleRate) {
    return send(sampleRate, formatTimer(key, value));
  }

  private String formatTimer(String key, int value) {
    // replacement because of synchronization for String.format("%s:%d|ms",

    StringBuilder builder = new StringBuilder();
    builder.append(key).append(":").append(value).append("|ms");
    return builder.toString();
  }

  public boolean decrement(String key) {
    return increment(key, -1, 1.0);
  }

  public boolean decrement(String key, int magnitude) {
    return decrement(key, magnitude, 1.0);
  }

  public boolean decrement(String key, int magnitude, double sampleRate) {
    magnitude = (magnitude < 0) ? magnitude : -magnitude;
    return increment(key, magnitude, sampleRate);
  }

  public boolean decrement(String... keys) {
    return increment(-1, 1.0, keys);
  }

  public boolean decrement(int magnitude, String... keys) {
    magnitude = (magnitude < 0) ? magnitude : -magnitude;
    return increment(magnitude, 1.0, keys);
  }

  public boolean decrement(int magnitude, double sampleRate, String... keys) {
    magnitude = (magnitude < 0) ? magnitude : -magnitude;
    return increment(magnitude, sampleRate, keys);
  }

  public boolean increment(String key) {
    return increment(key, 1, 1.0);
  }

  public boolean increment(String key, int magnitude) {
    return increment(key, magnitude, 1.0);
  }

  public boolean increment(String key, int magnitude, double sampleRate) {
    String stat = formatCounter(key, magnitude);
    return send(stat, sampleRate);
  }

  private String formatCounter(String key, int magnitude) {
    // replacement because of synchronization for String.format("%s:%s|c"
    StringBuilder builder = new StringBuilder();
    builder.append(key).append(":").append(magnitude).append("|c");
    return builder.toString();
  }

  public boolean increment(int magnitude, double sampleRate, String... keys) {
    String[] stats = new String[keys.length];
    for (int i = 0; i < keys.length; i++) {
      stats[i] = formatCounter(keys[i], magnitude);
    }
    return send(sampleRate, stats);
  }

  private boolean send(String stat, double sampleRate) {
    return send(sampleRate, stat);
  }

  private boolean send(double sampleRate, String... stats) {
    boolean retval = false; // didn't send anything
    if (sampleRate < 1.0) {
      for (String stat : stats) {
        if (rng.nextDouble() <= sampleRate) {
          String key = formatSampledValue(stat, sampleRate);
          if (doSend(key)) {
            retval = true;
          }
        }
      }
    } else {
      for (String stat : stats) {
        String key = formatUnsampledValue(stat);
        if (doSend(key)) {
          retval = true;
        }
      }
    }

    return retval;
  }

  protected String formatSampledValue(String stat, double sampleRate) {
    // replacement because of synchronization for String.format(Locale.ENGLISH, "%s|@%f|%s.%s"
    StringBuilder builder = new StringBuilder();
    builder.append(stat).append("|@").append(sampleRate).append("|").append(appName).append(".").append(localHostName);
    return builder.toString();
  }

  protected String formatUnsampledValue(String stat) {
    // replacement because of synchronization for String.format("%s||%s.%s"
    StringBuilder builder = new StringBuilder();
    builder.append(stat).append("||").append(appName).append(".").append(localHostName);
    return builder.toString();
  }

  private boolean doSend(String stat) {
    try {
      socket.send(stat);
      return true;
    } catch (IOException e) {
      LOG.error("Could not send stat " + stat + " to host " + socket, e);
    }
    return false;
  }
}
