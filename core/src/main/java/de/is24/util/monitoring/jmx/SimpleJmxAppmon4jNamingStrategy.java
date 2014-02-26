package de.is24.util.monitoring.jmx;

public class SimpleJmxAppmon4jNamingStrategy implements JmxAppMon4JNamingStrategy {
  private final String jmxPrefix;

  public SimpleJmxAppmon4jNamingStrategy(String prefix) {
    this.jmxPrefix = prefix;
  }

  @Override
  public String getJmxPrefix() {
    return jmxPrefix;
  }
}
