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
  private static final Logger JPC_LOGGER_BTE = LoggerFactory.getLogger("jpc-logger-byte");
  private static final Logger JPC_LOGGER_S = LoggerFactory.getLogger("jpc-logger-string");

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
    this.logAddresses(netSocket, "established");
    netSocket.handler(buffer -> this.handleBuffer(netSocket, buffer));
    netSocket.closeHandler(v -> this.logAddresses(netSocket, "closed"));
  }

  private void handleBuffer(NetSocket netSocket, Buffer buffer) {
    byte[] bytes = buffer.getBytes();
    int l = buffer.length();
    JPC_LOGGER_S.debug("{} | {}", l, buffer.toString());
    JPC_LOGGER_BTE.debug("{} | {}", l, bytes);

    this.logAsHexString(bytes, l);

    if (this.isPingMessage(bytes)) {
      // The message appears to be a ping message. Just "echo" it.
      LOGGER.debug("Echo'ing ping message.");
      netSocket
        .rxWrite(buffer)
        .doOnError(throwable -> throwable.printStackTrace())
        .subscribe(() -> LOGGER.debug("Wrote echo to client"),
          throwable -> LOGGER.error("Unable to echo ping request to client due to: ", throwable));
    }
    if (this.isAnnounce(bytes)) {
      // Announce?
      LOGGER.debug("Announce message detected. Sending ACK");
      // Append the length, followed by the function (0x01 0x03) and an empty body (0x00)
      byte[] response = new byte[9];
      this.copyHeader(response, bytes);

      // Set the size to 0x00 0x03
      response[4] = 0x00;
      response[5] = 0x03;

      // Set the function (0x01 0x03)
      response[6] = 0x01;
      response[7] = 0x03;
      // And an empty body (0x00)
      response[8] = 0x00;

      LOGGER.debug("Compiled announce response as {} (hex)", this.asHexString(response));

      netSocket
        .rxWrite(Buffer.buffer(response))
        .doOnError(throwable -> throwable.printStackTrace())
        .subscribe(() -> LOGGER.debug("ACK sent for announce message."),
          throwable -> LOGGER.error("Unable to ACK the announce message due to: ", throwable));
    }
    if (this.isIdentify(bytes)) {
      LOGGER.debug("Identify item detected. Not doing anything (just log the message)");
    }
    if (this.isData(bytes)) {
      // We need to acknowledge the data but at the moment we do nothing else than that.
      LOGGER.debug("Data message detected. Sending ACK");
      byte[] response = new byte[9];
      this.copyHeader(response, bytes);

      // Set the size to 3 (0x03)
      response[4] = 0x00;
      response[5] = 0x03;

      // Set the function (0x01 0x04)
      response[6] = 0x01;
      response[7] = bytes[7];
      // Add an empty body (0x00)
      response[8] = 0x00;

      LOGGER.debug("Compiled data response as {} (hex)", this.asHexString(response));

      netSocket
        .rxWrite(Buffer.buffer(response))
        .doOnError(throwable -> throwable.printStackTrace())
        .subscribe(() -> LOGGER.debug("ACK sent for data message."),
          throwable -> LOGGER.error("Unable to sent ACK for data message due to: ", throwable));
    }
  }

  private void logAsHexString(byte[] bytes, int bufferLength){

    StringBuilder sb = new StringBuilder();
    StringBuilder intSb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X ", b));
      int i = b;
      intSb.append(i + " ");
    }

    JPC_LOGGER.debug("{} | {}", bufferLength, sb.toString());
    JPC_LOGGER_INT.debug("{} | {}", bufferLength, intSb.toString());

  }

  private String asHexString(byte[] bytes){
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X ", b));
      int i = b;
    }
    return sb.toString();
  }

  private void copyHeader(byte[] into, byte[] bytes) {
    if(bytes.length < 5){
      throw new IllegalStateException("Bytes too small");
    }

    // Copy the first 4 elements of the original message (this is the header of the message)
    for (int i = 0; i < 4; i++) {
      into[i] = bytes[i];
    }
  }

  private boolean isData(byte[] bytes) {
    return this.isFunction(bytes, (byte) 0x01, (byte) 0x04) || this.isFunction(bytes, (byte) 0x01, (byte) 0x05);
  }

  private boolean isIdentify(byte[] bytes) {
    return this.isFunction(bytes, (byte) 0x01, (byte) 0x19);
  }

  private boolean isPingMessage(byte[] bytes) {
    return this.isFunction(bytes, (byte) 0x01, (byte) 0x16);
  }

  private boolean isAnnounce(byte[] bytes) {
    return this.isFunction(bytes, (byte) 0x01, (byte) 0x03);
  }

  // A function consists out of 2 bytes which could be represented differently but written out to make it a bit more verbose.
  private boolean isFunction(byte[] bytes, byte part1, byte part2) {
    return this.meetsExceedsMinLength(bytes) && bytes[6] == part1 && bytes[7] == part2;
  }

  private boolean meetsExceedsMinLength(byte[] bytes) {
    return bytes.length >= 8;
  }

  private void logAddresses(NetSocket netSocket, String event) {
    io.vertx.core.net.NetSocket delegate = netSocket.getDelegate();
    LOGGER.debug("Socket with remote address {} and local address {} was {}.",
      Objects.nonNull(delegate.remoteAddress()) ? delegate.remoteAddress().host() : "unknown",
      Objects.nonNull(delegate.localAddress()) ? delegate.localAddress().host() : "unknown", event);
  }
}
