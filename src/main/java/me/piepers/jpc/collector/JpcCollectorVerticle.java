package me.piepers.jpc.collector;

import io.vertx.core.Promise;
import io.vertx.core.net.NetServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
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
  private NetServer server;

  @Override
  public void start(Promise<Void> startFuture) throws Exception {
    this.server = vertx.createNetServer(new NetServerOptions().setPort(5279));
    this.server.connectHandler(netSocket -> this.handleConnection(netSocket));
    this.server
      .rxListen()
      .doOnSuccess(netServer -> LOGGER.debug("Server was started on port 5279"))
      .doOnError(throwable -> LOGGER.error("Unable to start TCP server.", throwable))
      .doOnError(Throwable::printStackTrace)
      .subscribe(netServer -> startFuture.complete(),
        throwable -> startFuture.fail(throwable));
  }

  private void handleConnection(NetSocket netSocket) {
    netSocket.handler(buffer -> LOGGER.debug("Received something with length {} from the JPC Logger:\n{}",
      buffer.length(), buffer.toString()));
    netSocket.closeHandler(v -> this.logAddresses(netSocket));
  }

  private void logAddresses(NetSocket netSocket) {
    io.vertx.core.net.NetSocket delegate = netSocket.getDelegate();
    LOGGER.debug("Socket with remote address {} and local address {} was closed.",
      Objects.nonNull(delegate.remoteAddress()) ? delegate.remoteAddress().host() : "unknown",
      Objects.nonNull(delegate.localAddress()) ? delegate.localAddress().host() : "unknown");
  }
}
