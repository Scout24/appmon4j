package de.is24.util.monitoring.statsd;

public class StatsdNoneGroupingHostMessageFormatter extends StatsdMessageFormatter {
  public StatsdNoneGroupingHostMessageFormatter(String appName, String localHostName) {
    super(appName, localHostName);
  }

  public StatsdNoneGroupingHostMessageFormatter(final String appName) {
    super(appName);
  }

  @Override
  public String formatSampledValue(String stat, double sampleRate) {
    return new StringBuilder().append(getAppName())
      .append(".")
      .append(getLocalHostName())
      .append(".")
      .append(stat)
      .append("|@")
      .append(sampleRate)
      .append("|")
      .toString();
  }

  @Override
  public String formatUnsampledValue(String stat) {
    StringBuilder builder = new StringBuilder();
    builder.append(getAppName()).append(".").append(getLocalHostName()).append(".").append(stat).append("||");
    return builder.toString();
  }
}
