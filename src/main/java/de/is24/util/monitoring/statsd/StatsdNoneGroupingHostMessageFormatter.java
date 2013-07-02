package de.is24.util.monitoring.statsd;

public class StatsdNoneGroupingHostMessageFormatter implements StatsdMessageFormatter {
  private final String appName;
  private final String localHostName;

  public StatsdNoneGroupingHostMessageFormatter(String appName, String localHostName) {
    this.appName = appName;
    this.localHostName = localHostName;
  }

  public String formatSampledValue(String stat, double sampleRate) {
    StringBuilder builder = new StringBuilder();
    builder.append(stat).append("|@").append(sampleRate).append("|").append(appName).append(".").append(localHostName);
    return builder.toString();
  }

  public String formatUnsampledValue(String stat) {
    StringBuilder builder = new StringBuilder();
    builder.append(stat).append("||").append(appName).append(".").append(localHostName);
    return builder.toString();
  }
}
