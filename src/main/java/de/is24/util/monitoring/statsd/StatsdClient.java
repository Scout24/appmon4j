package de.is24.util.monitoring.statsd;

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
  private final StatsdMessageFormatter messageFormatter;

  public StatsdClient(String host, int port, String appName) throws UnknownHostException, SocketException {
    this(host, port, new StatsdHostGroupedMessageFormatter(appName));
  }

  public StatsdClient(String host, int port, StatsdMessageFormatter messageFormatter) throws UnknownHostException,
                                                                                             SocketException {
    this(new StatsdDatagrammSocket(InetAddress.getByName(host), port), messageFormatter);
  }

  StatsdClient(StatsdDatagrammSocket socket, StatsdMessageFormatter formatter) {
    this.socket = socket;
    this.messageFormatter = formatter;
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
    return new StringBuilder().append(key).append(":").append(value).append("|ms").toString();
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
    return new StringBuilder().append(key).append(":").append(magnitude).append("|c").toString();
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
          String key = messageFormatter.formatSampledValue(stat, sampleRate);
          if (doSend(key)) {
            retval = true;
          }
        }
      }
    } else {
      for (String stat : stats) {
        String key = messageFormatter.formatUnsampledValue(stat);
        if (doSend(key)) {
          retval = true;
        }
      }
    }

    return retval;
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
