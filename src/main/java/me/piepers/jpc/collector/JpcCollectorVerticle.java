package me.piepers.jpc.collector;

import io.vertx.core.Promise;
import io.vertx.core.net.NetServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.net.NetServer;
import io.vertx.reactivex.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * A simple verticle that listens to a port and receives incoming packets and appends it to a log file.
 */
public class JpcCollectorVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(JpcCollectorVerticle.class);
  private static final Logger JPC_LOGGER = LoggerFactory.getLogger("jpc-logger");
  private static final Logger JPC_LOGGER_INT = LoggerFactory.getLogger("jpc-logger-int");
  private static final Logger JPC_LOGGER_BTE = LoggerFactory.getLogger("jpc-logger-byte");
  private static final Logger JPC_LOGGER_S = LoggerFactory.getLogger("jpc-logger-string");
  private static final Logger DATA_LOGGER = LoggerFactory.getLogger("jpc-logger-data");

  Map<String, Integer> struct0104 = new HashMap<>() {{
    put("pvserial", 76);
    put("date", 0);
    put("pvstatus", 158);
    put("pvpowerin", 162);
    put("pv1voltage", 170);
    put("pv1current", 174);
    put("pv1watt", 178);
    put("pv2voltage", 186);
    put("pv2current", 190);
    put("pv2watt", 194);
    put("pvpowerout", 202);
    put("pvfrequency", 210);
    put("pvgridvoltage", 214);
    put("pvenergytoday", 262);
    put("pvenergytotal", 270);
    put("pvtemperature", 286);
    put("pvipmtemperature", 322);
  }};

  // Note: the scructure below is identical to the 0104 but the date seems to be present here.
  Map<String, Integer> struct0150 = new HashMap<>() {{
    put("pvserial", 76);
    put("date", 136);
    put("pvstatus", 158);
    put("pvpowerin", 162);
    put("pv1voltage", 170);
    put("pv1current", 174);
    put("pv1watt", 178);
    put("pv2voltage", 186);
    put("pv2current", 190);
    put("pv2watt", 194);
    put("pvpowerout", 202);
    put("pvfrequency", 210);
    put("pvgridvoltage", 214);
    put("pvenergytoday", 262);
    put("pvenergytotal", 270);
    put("pvtemperature", 286);
    put("pvipmtemperature", 322);
  }};

  char[] mask = {'G', 'r', 'o', 'w', 'a', 't', 't'};

  // TODO: keep a Map of netsockets as the JPC Logger doesn't always neatly closes the connection. Alternatively keep one server per inverter (and don't allow it to open multiple times).
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
    String uuid = UUID.randomUUID().toString();
    LocalDateTime localDateTime = LocalDateTime.now();

    JPC_LOGGER_S.debug("{} | {} | {}", l, uuid, buffer.toString());
    JPC_LOGGER_BTE.debug("{} | {} | {}", l, uuid, bytes);

    this.logAsHexString(uuid, bytes, l);

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
      // Extract the data first - TODO: this must all be async so map to something readable, extract data, send ACK.
      this.extractData(bytes, uuid, localDateTime);

      // Now, send an acknowledge.
      LOGGER.debug("Data message detected. Sending ACK");
      byte[] response = new byte[9];
      this.copyHeader(response, bytes);

      // Set the size to 3 (0x03)
      response[4] = 0x00;
      response[5] = 0x03;

      // Set the function (0x01 0x04)
      response[6] = 0x01;
      // Can be 0x04 or 0x05
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

  private void extractData(byte[] bytes, String uuid, LocalDateTime localDateTime) {
    // Detect which struct to take.
    StringBuilder uss = new StringBuilder();
    // Do not unscramble the first 8 positions.
    for (int i = 0; i < 8; i++) {
      uss.append(String.format("%02X", bytes[i]));
    }
    for (int i = 8, j = 0; i < bytes.length; i++, j++) {
      if (j == mask.length) {
        j = 0;
      }
      // Unscramble means: take the mask at a particular position and invoke an xor against the data at a specific position.
      // TODO: Now the logging as well as this processing iterate the buffer and do the same. Make sure this happens only once.
      uss.append(String.format("%02X", (Byte.toUnsignedInt(bytes[i])) ^ (byte) mask[j]));
    }

    String result = uss.toString();
    // TODO: a more sophisticated way of detecting which function/record it is would be nice.
    Map<String, Integer> struct = uss.substring(12, 16).equals("0150") ? this.struct0150 : this.struct0104;

    String pvserial = result.substring(struct.get("pvserial"), struct.get("pvserial") + 20);
    int pvstatus = Integer.valueOf(result.substring(struct.get("pvstatus"), struct.get("pvstatus") + 4), 16);
    Long pvpowerin = Long.parseLong(result.substring(struct.get("pvpowerin"), struct.get("pvpowerin") + 8), 16);
    int pv1voltage = Integer.valueOf(result.substring(struct.get("pv1voltage"), struct.get("pv1voltage") + 4), 16);
    int pv1current = Integer.valueOf(result.substring(struct.get("pv1current"), struct.get("pv1current") + 4), 16);
    Long pv1watt = Long.parseLong(result.substring(struct.get("pv1watt"), struct.get("pv1watt") + 8), 16);
    int pv2voltage = Integer.valueOf(result.substring(struct.get("pv2voltage"), struct.get("pv2voltage") + 4), 16);
    int pv2current = Integer.valueOf(result.substring(struct.get("pv2current"), struct.get("pv2current") + 4), 16);
    Long pv2watt = Long.parseLong(result.substring(struct.get("pv2watt"), struct.get("pv2watt") + 8), 16);
    Long pvpowerout = Long.parseLong(result.substring(struct.get("pvpowerout"), struct.get("pvpowerout") + 8), 16);
    int pvfrequentie = Integer.valueOf(result.substring(struct.get("pvfrequency"), struct.get("pvfrequency") + 4), 16);
    int pvgridvoltage = Integer.valueOf(result.substring(struct.get("pvgridvoltage"), struct.get("pvgridvoltage") + 4), 16);
    Long pvenergytoday = Long.parseLong(result.substring(struct.get("pvenergytoday"), struct.get("pvenergytoday") + 8), 16);
    Long pvenergytotal = Long.parseLong(result.substring(struct.get("pvenergytotal"), struct.get("pvenergytotal") + 8), 16);
    int pvtemperature = Integer.valueOf(result.substring(struct.get("pvtemperature"), struct.get("pvtemperature") + 4), 16);
    int pvipmtemperature = Integer.valueOf(result.substring(struct.get("pvipmtemperature"), struct.get("pvipmtemperature") + 4), 16);

    DATA_LOGGER.debug("hex decrypted: " + result + "\nuuid: " + uuid + "\ndate/time: " + localDateTime + "\npvserial: " + pvserial + "\npvstatus: " + pvstatus + "\npvpowerin: " + pvpowerin + "\npv1voltage: " + pv1voltage
      + "\npv1current: " + pv1current + "\npv1watt: " + pv1watt + "\npv2voltage: " + pv2voltage + "\npv2current: " + pv2current + "\npv2watt: " + pv2watt
      + "\npvpowerout: " + pvpowerout + "\npvfrequency: " + pvfrequentie + "\npvgridvoltage: " + pvgridvoltage + "\npvenergytoday: " + pvenergytoday
      + "\npvenergytotal: " + pvenergytotal + "\npvtemperature: " + pvtemperature + "\npvipmtemperature: " + pvipmtemperature);
  }

  private void logAsHexString(String uuid, byte[] bytes, int bufferLength) {

    StringBuilder sb = new StringBuilder();
    int[] numbers = new int[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      sb.append(String.format("%02X ", b));
      int n = Byte.toUnsignedInt(b);
      numbers[i] = n;
      numbers[i] = n;

    }

    JPC_LOGGER.debug("{} | {} | {}", bufferLength, uuid, sb.toString());
    JPC_LOGGER_INT.debug("{} | {} | {}", bufferLength, uuid, Arrays.toString(numbers));
  }

  private String asHexString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X ", b));
      int i = b;
    }
    return sb.toString();
  }

  private void copyHeader(byte[] into, byte[] bytes) {
    if (bytes.length < 5) {
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
