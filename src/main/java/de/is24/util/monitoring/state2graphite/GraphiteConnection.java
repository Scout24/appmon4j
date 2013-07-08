package de.is24.util.monitoring.state2graphite;

import de.is24.util.monitoring.tools.ConnectionState;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;


public class GraphiteConnection {
  private static final Logger LOGGER = Logger.getLogger(GraphiteConnection.class);
  private final String graphiteHost;
  private final int graphitePort;
  private ConnectionState connectionState = ConnectionState.UNKNOWN;
  private long lastReportTimestamp = 0;
  private static long connectionFailureLogDelay = 10 * 60 * 1000; // 10 minutes in milliseconds


  public GraphiteConnection(String graphiteHost, int graphitePort) {
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
        if (connectionState != ConnectionState.SUCCESS) {
          LOGGER.info("Connection to graphite Host " +
            ((connectionState == ConnectionState.UNKNOWN) ? "established" : "recovered"));
          connectionState = ConnectionState.SUCCESS;
        }

      } catch (IOException e) {
        String action = "write";
        handleException(e, action);
      } finally {
        socket.close();
      }
    } catch (IOException e) {
      String action = "connect";
      handleException(e, action);
    }
  }

  private void handleException(IOException e, String action) {
    // we log on state transition and every 10 minutes
    if (connectionState != ConnectionState.FAILED) {
      connectionState = ConnectionState.FAILED;
      LOGGER.warn("could not " + action + " to graphite host " + graphiteHost + " on port " + graphitePort, e);
      lastReportTimestamp = System.currentTimeMillis();
    }
    if ((System.currentTimeMillis() - lastReportTimestamp) > connectionFailureLogDelay) {
      LOGGER.warn("could not " + action + " to graphite host " + graphiteHost + " on port " + graphitePort, e);
      lastReportTimestamp = System.currentTimeMillis();
    }
  }

  @Override
  public String toString() {
    return "GraphiteConection:" + graphiteHost + "," + graphitePort;
  }
}
