package de.is24.util.monitoring.tools;

import de.flapdoodle.embed.process.runtime.Network;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;


/**
 * Mock for a graphite server using non-blocking IO according to http://www.onjava.com/pub/a/onjava/2002/09/04/nio.html?page=2
 */
public class GraphiteMockServer extends ExternalResource implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(GraphiteMockServer.class);

  private ServerSocketChannel server;
  private int port;
  private Selector selector;
  private Thread thread;

  @Override
  public void before() throws Throwable {
    LOG.info("Graphite Mock before...");
    server = ServerSocketChannel.open();
    server.configureBlocking(false);
    port = Network.getFreeServerPort();
    LOG.info("GraphiteMockServer starting : port={}", port);
    server.socket().bind(new InetSocketAddress(port));
    selector = Selector.open();
    server.register(selector, SelectionKey.OP_ACCEPT);

    thread = new Thread(this);
    thread.start();
    LOG.info("... done Graphite Mock before");
  }

  @Override
  public void after() {
    LOG.info("Graphite Mock after");
    try {
      thread.interrupt();
    } catch (Exception e) {
    }
    try {
      selector.close();
    } catch (IOException e) {
    }
    try {
      server.close();
    } catch (Exception e) {
    }
  }

  public int getPort() {
    return port;
  }

  @Override
  public void run() {
    Charset charset = Charset.forName("ISO-8859-1");
    CharsetDecoder decoder = charset.newDecoder();

    try {
      while (!thread.isInterrupted()) {
        selector.select();

        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove();

          if (!key.isValid()) {
            continue;
          }

          if (key.isAcceptable()) {
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            continue;
          }

          if (key.isReadable()) {
            SocketChannel client = (SocketChannel) key.channel();
            int BUFFER_SIZE = 1024;
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            try {
              int bytesRead = client.read(buffer);
              if (bytesRead > 0) {
                buffer.flip();

                CharBuffer charBuffer = decoder.decode(buffer);
                LOG.info(charBuffer.toString());
              }
            } catch (Exception e) {
              e.printStackTrace();
            }

            continue;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }


  }
}
