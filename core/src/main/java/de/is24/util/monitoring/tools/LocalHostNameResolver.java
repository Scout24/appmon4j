package de.is24.util.monitoring.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.UnknownHostException;


public class LocalHostNameResolver {
  private static final Logger LOG = LoggerFactory.getLogger(LocalHostNameResolver.class);
  private final LocalHostProvider localHostProvider;

  public LocalHostNameResolver() {
    this(new LocalHostProvider());
  }

  public LocalHostNameResolver(LocalHostProvider localHostProvider) {
    this.localHostProvider = localHostProvider;
  }

  public String getLocalHostName() {
    try {
      String sometimesFullyQualifiedHostName = localHostProvider.getLocalHost().getHostName();
      int firstDotIndex = sometimesFullyQualifiedHostName.indexOf(".");
      String hostName = sometimesFullyQualifiedHostName;
      if (firstDotIndex >= 0) {
        hostName = sometimesFullyQualifiedHostName.substring(0, firstDotIndex);
      }
      return hostName;
    } catch (UnknownHostException e) {
      LOG.warn("Could not resolve local host: " + e.toString(), e);
      return "unknownHost";
    }
  }
}
