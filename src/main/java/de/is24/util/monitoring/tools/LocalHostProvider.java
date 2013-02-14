package de.is24.util.monitoring.tools;

import java.net.InetAddress;
import java.net.UnknownHostException;


class LocalHostProvider {
  public InetAddress getLocalHost() throws UnknownHostException {
    return InetAddress.getLocalHost();
  }
}
