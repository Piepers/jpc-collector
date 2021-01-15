package me.piepers.jpc.collector;

import io.vertx.core.Promise;
import io.vertx.core.net.NetServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.net.NetServer;
import io.vertx.reactivex.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple verticle that listens to a port and receives incoming packets and appends it to a log file.
 */
public class JpcCollectorVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(JpcCollectorVerticle.class);
  private static final Logger JPC_LOGGER = LoggerFactory.getLogger("jpc-logger");
  private static final Logger JPC_LOGGER_INT = LoggerFactory.getLogger("jpc-logger-int");

  private NetServer server;

  @Override
  public void start(Promise<Void> startFuture) throws Exception {
    this.server = vertx.createNetServer(new NetServerOptions().setPort(5279));
    this.server.connectHandler(netSocket -> this.handleConnection(netSocket));
    this.server
      .rxListen()
      .doOnSuccess(netServer -> LOGGER.debug("Server was started on port 5279. See jpc-logger for incoming transmissions."))
      .doOnError(throwable -> LOGGER.error("Unable to start TCP server.", throwable))
      .doOnError(Throwable::printStackTrace)
      .subscribe(netServer -> startFuture.complete(),
        throwable -> startFuture.fail(throwable));
  }

  private void handleConnection(NetSocket netSocket) {
    netSocket.handler(buffer -> this.handleBuffer(buffer));
    netSocket.closeHandler(v -> this.logAddresses(netSocket));
  }

  private void handleBuffer(Buffer buffer) {
    StringBuilder sb = new StringBuilder();
    StringBuilder intSb = new StringBuilder();
    for(byte b : buffer.getBytes()) {
      sb.append(String.format("%02X ", b));
      int i = b;
      intSb.append(i + " ");
    }
//    LOGGER.debug("Received with length {}: {}", buffer.length(), sb.toString());

    JPC_LOGGER.debug("{} | {}", buffer.length(), sb.toString());
    JPC_LOGGER_INT.debug("{} | {}", buffer.length(), intSb.toString());
  }

  private void logAddresses(NetSocket netSocket) {
    io.vertx.core.net.NetSocket delegate = netSocket.getDelegate();
    LOGGER.debug("Socket with remote address {} and local address {} was closed.",
      Objects.nonNull(delegate.remoteAddress()) ? delegate.remoteAddress().host() : "unknown",
      Objects.nonNull(delegate.localAddress()) ? delegate.localAddress().host() : "unknown");
  }
}
