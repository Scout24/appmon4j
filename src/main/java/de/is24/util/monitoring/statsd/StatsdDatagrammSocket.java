package de.is24.util.monitoring.statsd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

class StatsdDatagrammSocket {
  private final InetAddress host;
  private final int port;
  private final DatagramSocket socket;

  public StatsdDatagrammSocket(InetAddress host, int port) throws SocketException {
    this.host = host;
    this.port = port;
    this.socket = new DatagramSocket();
  }

  public void send(String stat) throws IOException {
    byte[] data = stat.getBytes();
    socket.send(new DatagramPacket(data, data.length, host, port));
  }

  @Override
  public String toString() {
    return host.toString() + ':' + port;
  }
}