package de.is24.util.monitoring.agent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Appmon4JAgentConfiguration {
  private boolean instrumentationConfigured;
  private boolean jmxExportConfigured;
  private boolean jmxExportFile;
  private String jmxExporterSource;
  private boolean graphiteConfigured;
  private String graphiteHost;
  private String graphiteAppNamePrefix;
  private int graphitePort;

  private static final String JXMEXPORT_PATTERN_FILE_NAME = "JMXExporter.patternFileName";
  private static final String JXMEXPORT_PATTERN_DIRECTORY_NAME = "JMXExporter.patternDirectoryName";

  private static final String GRAPHITE_HOST = "graphite.host";
  private static final String GRAPHITE_APP_NAME_PREFIX = "graphite.appNamePrefix";
  private static final String GRAPHITE_PORT = "graphite.port";


  public Appmon4JAgentConfiguration(Properties properties) {
    checkJMXExporterConfig(properties);
    checkGraphiteConfig(properties);

  }

  private void checkJMXExporterConfig(Properties properties) {
    jmxExporterSource = properties.getProperty(JXMEXPORT_PATTERN_DIRECTORY_NAME);
    jmxExportConfigured = true;
    if (jmxExporterSource != null) {
      jmxExportFile = false;
    } else {
      jmxExporterSource = properties.getProperty(JXMEXPORT_PATTERN_FILE_NAME);
      if (jmxExporterSource != null) {
        jmxExportFile = true;
      } else {
        jmxExportConfigured = false;
      }
    }
  }

  private void checkGraphiteConfig(Properties properties) {
    graphiteHost = properties.getProperty(GRAPHITE_HOST);
    graphiteAppNamePrefix = properties.getProperty(GRAPHITE_APP_NAME_PREFIX, "Appmon4jAgent");
    graphitePort = Integer.parseInt(properties.getProperty(GRAPHITE_PORT, "2003"));
    graphiteConfigured = (graphiteHost != null) && (graphiteHost.trim().length() > 0);
  }

  public static Appmon4JAgentConfiguration load(String propertiesFileName) throws IOException {
    InputStream inputStream = new File(propertiesFileName).toURI().toURL().openStream();
    Properties properties = new Properties();
    properties.load(inputStream);
    return new Appmon4JAgentConfiguration(properties);
  }

  public boolean isInstrumentationConfigured() {
    return instrumentationConfigured;
  }

  public void setInstrumentationConfigured(boolean instrumentationConfigured) {
    this.instrumentationConfigured = instrumentationConfigured;
  }

  public boolean isJmxExportConfigured() {
    return jmxExportConfigured;
  }

  public boolean isJmxExportFile() {
    return jmxExportFile;
  }

  public String getJmxExporterSource() {
    return jmxExporterSource;
  }

  public boolean isGraphiteConfigured() {
    return graphiteConfigured;
  }

  public void setGraphiteConfigured(boolean graphiteConfigured) {
    this.graphiteConfigured = graphiteConfigured;
  }

  public String getGraphiteHost() {
    return graphiteHost;
  }

  public String getGraphiteAppNamePrefix() {
    return graphiteAppNamePrefix;
  }

  public int getGraphitePort() {
    return graphitePort;
  }
}
