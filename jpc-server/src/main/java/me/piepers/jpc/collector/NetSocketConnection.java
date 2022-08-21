package me.piepers.jpc.collector;

import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.net.NetSocket;
import me.piepers.jpc.domain.GrowattDataMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * A NetSocketConnection represents a connection with a client and contains the logic to process incoming requests
 * and packages.
 */
public class NetSocketConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetSocketConnection.class);
    private static final Logger JPC_LOGGER = LoggerFactory.getLogger("jpc-logger");
    private static final Logger JPC_LOGGER_INT = LoggerFactory.getLogger("jpc-logger-int");
    private static final Logger JPC_LOGGER_BTE = LoggerFactory.getLogger("jpc-logger-byte");
    private static final Logger JPC_LOGGER_S = LoggerFactory.getLogger("jpc-logger-string");
    private static final Logger DATA_LOGGER = LoggerFactory.getLogger("jpc-logger-data");

    private final String id;
    private final LocalDateTime created;
    private LocalDateTime lastActive;

    private final NetSocket netSocket;
    private final Vertx vertx;
    private final String closedConnectionPublishAddress;
    private final String dataPublishAddress;

    static Map<String, Integer> STRUCT0104 = new HashMap<>() {{
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
    static Map<String, Integer> STRUCT0150 = new HashMap<>() {{
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

    private static final char[] MASK = {'G', 'r', 'o', 'w', 'a', 't', 't'};

    protected NetSocketConnection(String id, NetSocket netSocket, LocalDateTime created, LocalDateTime lastActive,
                                  Vertx vertx, String closedConnectionPublishAddress, String dataPublishAddress) {
        this.id = id;
        this.netSocket = netSocket;
        this.created = created;
        this.lastActive = lastActive;
        this.vertx = vertx;
        this.closedConnectionPublishAddress = closedConnectionPublishAddress;
        this.dataPublishAddress = dataPublishAddress;

        netSocket.handler(this::handleBuffer);
        netSocket.exceptionHandler(this::handleException);
        netSocket.closeHandler(v -> this.handleConnectionClose());
    }

    public static NetSocketConnection with(String id, NetSocket netSocket, Vertx vertx, String closedConnectionPublishAddress, String dataPublishAddress) {
        LocalDateTime now = LocalDateTime.now();

        NetSocketConnection netSocketConnection = new NetSocketConnection(id, netSocket, now, now, vertx, closedConnectionPublishAddress, dataPublishAddress);
        netSocketConnection.logAddresses("established");
        return netSocketConnection;
    }

    /**
     * Given a date and time, checks whether the last seen is past the duration that it is allowed to be inactive.
     *
     * @param allowed, the allowed time between which a connection can be active (based on "lastSeen") and the time
     *                 they must be decommissioned.
     * @param from,    the time to start counting from.
     * @return true in case the connection can be deemed stale or false otherwise.
     */
    public boolean isStaleConnectionSuspect(Duration allowed, LocalDateTime from) {
        return Objects.nonNull(this.lastActive) &&
                this.lastActive.isBefore(from) &&
                Duration.between(this.lastActive, from)
                        .compareTo(allowed) > 0;
    }

    private void handleConnectionClose() {
        this.logAddresses("closed");

        this.vertx
                .eventBus()
                .publish(closedConnectionPublishAddress, new JsonObject()
                        .put("message", "closed")
                        .put("id", this.id));
    }

    public Completable closeConnection() {
        LOGGER.debug("Closing connection with id {} because last activity was too long ago ({}).", id, lastActive.toString());
        return this.netSocket
                .rxClose();
    }

    private void handleException(Throwable throwable) {
        LOGGER.error("Something went wrong.", throwable);
    }

    private void handleBuffer(Buffer buffer) {
        this.lastActive = LocalDateTime.now();

        byte[] bytes = buffer.getBytes();
        int l = buffer.length();
        String uuid = UUID.randomUUID().toString();
        Instant localDateTime = Instant.now();

        JPC_LOGGER_S.debug("{} | {} | {}", l, uuid, buffer.toString());
        JPC_LOGGER_BTE.debug("{} | {} | {}", l, uuid, bytes);

        this.logAsHexString(uuid, bytes, l);

        if (this.isPingMessage(bytes)) {
            // The message appears to be a ping message. Just "echo" it.
            LOGGER.info("Echo'ing ping message.");
            netSocket
                    .rxWrite(buffer)
                    .doOnError(throwable -> throwable.printStackTrace())
                    .subscribe(() -> LOGGER.debug("Wrote echo to client"),
                            throwable -> LOGGER.error("Unable to echo ping request to client due to: ", throwable));
        }
        if (this.isAnnounce(bytes)) {
            // Announce?
            LOGGER.info("Announce message detected. Sending ACK");
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
            GrowattDataMessage growattDataMessage = this.extractData(bytes, uuid, localDateTime);

            // Now, send an acknowledge.
            LOGGER.info("Data message detected. Sending ACK");
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
                    .andThen(Completable.fromAction(() -> this.vertx.eventBus().publish(dataPublishAddress, JsonObject.mapFrom(growattDataMessage))))
                    .doOnError(throwable -> LOGGER.error("Something went wrong when publishing the data message to the event bus.", throwable))
                    .doOnError(throwable -> throwable.printStackTrace())
                    .subscribe(() -> LOGGER.debug("ACK sent and data published."),
                            throwable -> LOGGER.error("Unable to sent ACK for data message and publish to event bus due to: ", throwable));
        }
    }

    private GrowattDataMessage extractData(byte[] bytes, String uuid, Instant instant) {
        String result = this.unscramble(bytes);
        GrowattDataMessage growattDataMessage = GrowattDataMessage.from(uuid, instant, result);
        DATA_LOGGER.debug(growattDataMessage.asFormattedString(result));
        return growattDataMessage;
    }

    private String unscramble(byte[] bytes) {
        StringBuilder uss = new StringBuilder();
        // Do not unscramble the first 8 positions.
        for (int i = 0; i < 8; i++) {
            uss.append(String.format("%02X", bytes[i]));
        }
        for (int i = 8, j = 0; i < bytes.length; i++, j++) {
            if (j == MASK.length) {
                j = 0;
            }
            // Unscramble means: take the mask at a particular position and invoke an xor against the data at a specific position.
            // TODO: Now the logging as well as this processing iterate the buffer and do the same. Make sure this happens only once.
            uss.append(String.format("%02X", (Byte.toUnsignedInt(bytes[i])) ^ (byte) MASK[j]));
        }

        return uss.toString();
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
        System.arraycopy(bytes, 0, into, 0, 4);
    }

    private boolean isData(byte[] bytes) {
        return this.isFunction(bytes, (byte) 0x01, (byte) 0x04) || this.isFunction(bytes, (byte) 0x01, (byte) 0x50);
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

    private void logAddresses(String event) {
        io.vertx.core.net.NetSocket delegate = this.netSocket.getDelegate();
        LOGGER.info("Connection with remote address {} and local address {} was {}.",
                Objects.nonNull(delegate.remoteAddress()) ? delegate.remoteAddress().host() : "unknown",
                Objects.nonNull(delegate.localAddress()) ? delegate.localAddress().host() : "unknown", event);
    }

    public String getId() {
        return id;
    }

    public NetSocket getNetSocket() {
        return netSocket;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getLastActive() {
        return lastActive;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public String getClosedConnectionPublishAddress() {
        return closedConnectionPublishAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetSocketConnection that = (NetSocketConnection) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NetSocketConnection{" +
                "id='" + id + '\'' +
                ", created=" + created +
                ", lastActive=" + lastActive +
                ", vertx=" + vertx +
                ", publishAddress='" + closedConnectionPublishAddress + '\'' +
                '}';
    }
}

