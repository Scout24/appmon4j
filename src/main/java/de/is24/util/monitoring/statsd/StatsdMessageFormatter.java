package de.is24.util.monitoring.statsd;

import de.is24.util.monitoring.tools.LocalHostNameResolver;


public abstract class StatsdMessageFormatter {
  private final String appName;
  private final String localHostName;

  public StatsdMessageFormatter(final String appName, final String localHostName) {
    this.appName = appName;
    this.localHostName = localHostName;
  }

  public StatsdMessageFormatter(final String appName, LocalHostNameResolver localHostNameResolver) {
    this(appName, localHostNameResolver.getLocalHostName().replaceAll("\\.", "_"));
  }

  public StatsdMessageFormatter(final String appName) {
    this(appName, new LocalHostNameResolver().getLocalHostName().replaceAll("\\.", "_"));
  }

  public abstract String formatSampledValue(String stat, double sampleRate);

  public abstract String formatUnsampledValue(String stat);

  String getAppName() {
    return appName;
  }

  String getLocalHostName() {
    return localHostName;
  }
}
