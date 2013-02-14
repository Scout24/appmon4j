package de.is24.util.monitoring.state2graphite;

import org.apache.log4j.Logger;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;


public class GraphiteConnection {
  private final Logger LOGGER = Logger.getLogger(GraphiteConnection.class);
  private final String graphiteHost;
  private final int graphitePort;

  GraphiteConnection(String graphiteHost, int graphitePort) {
    this.graphiteHost = graphiteHost;
    this.graphitePort = graphitePort;
  }

  /**
   * Part of this code taken from http://neopatel.blogspot.de/2011/04/logging-to-graphite-monitoring-tool.html
   */
  public void send(String msg) {
    try {
      Socket socket = new Socket(graphiteHost, graphitePort);

      try {
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        try {
          writer.write(msg);
          writer.flush();
        } finally {
          try {
            writer.close();
          } catch (IOException ioe) {
            //ignore, we want to see the outer exception if any
            LOGGER.info("could not close writer");
          }
        }
      } catch (IOException e) {
        LOGGER.warn("could not write to graphite host " + graphiteHost + " on port " + graphitePort, e);
      } finally {
        socket.close();
      }
    } catch (IOException e) {
      LOGGER.warn("could not connect to graphite host " + graphiteHost + " on port " + graphitePort, e);
    }
  }

}
