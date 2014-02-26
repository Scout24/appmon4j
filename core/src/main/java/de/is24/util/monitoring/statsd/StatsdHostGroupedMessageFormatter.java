package de.is24.util.monitoring.statsd;

public class StatsdHostGroupedMessageFormatter extends StatsdMessageFormatter {
  public StatsdHostGroupedMessageFormatter(String appName, String localHostName) {
    super(appName, localHostName);
  }

  public StatsdHostGroupedMessageFormatter(final String appName) {
    super(appName);
  }

  @Override
  public String formatSampledValue(String stat, double sampleRate) {
    StringBuilder builder = new StringBuilder();
    builder.append(stat)
    .append("|@")
    .append(sampleRate)
    .append("|")
    .append(getAppName())
    .append(".")
    .append(getLocalHostName());
    return builder.toString();
  }

  @Override
  public String formatUnsampledValue(String stat) {
    StringBuilder builder = new StringBuilder();
    builder.append(stat).append("||").append(getAppName()).append(".").append(getLocalHostName());
    return builder.toString();
  }
}
