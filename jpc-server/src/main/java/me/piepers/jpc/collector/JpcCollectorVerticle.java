package me.piepers.jpc.collector;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.net.NetServer;
import io.vertx.reactivex.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * A simple verticle that listens to a port and receives incoming packets and appends it to a log file.
 */
public class JpcCollectorVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpcCollectorVerticle.class);
    private static final String CLOSED_CONNECTION_PUBLISH_ADDRESS = "jpc.collector";
    private static final String METRICS_PUBLISH_ADDRESS = "jpc.collector.metrics";
    // Check for unclosed connections interval.
    private static final long ORPHANED_CONNECTION_INTERVAL = TimeUnit.MINUTES.toMillis(60);
    // The time a connection "is allowed" to be inactive.
    private static final long STALE_CONNECTION_TIMEOUT_MINUTES = 30;
    private AtomicLong timerId;
    private NetServer server;
    private Map<String, NetSocketConnection> connections;

    @Override
    public void start(Promise<Void> startFuture) throws Exception {
        this.connections = new HashMap<>();
        this.server = vertx.createNetServer(new NetServerOptions().setPort(5279));
        this.server.connectHandler(netSocket -> this.handleConnection(netSocket));
        this.server
                .rxListen()
                .doOnSuccess(netServer -> LOGGER.debug("Server was started on port 5279. See jpc-logger for incoming transmissions."))
                .doOnError(throwable -> LOGGER.error("Unable to start TCP server.", throwable))
                .doOnError(Throwable::printStackTrace)
                .subscribe(netServer -> startFuture.complete(),
                        throwable -> startFuture.fail(throwable));

        this.vertx
                .eventBus()
                .<JsonObject>consumer(CLOSED_CONNECTION_PUBLISH_ADDRESS)
                .handler(this::handleClosedConnection);

        this.vertx
                .eventBus()
                .<JsonObject>consumer("get.connections",
                        message -> this.compileConnectionsReply(message));

        this.timerId = new AtomicLong(0L);
    }

    private void compileConnectionsReply(Message<JsonObject> message) {
        if (Objects.isNull(this.connections) || this.connections.isEmpty()) {
            message.reply(new JsonObject());
        } else {
            List<JsonObject> result = this.connections
                    .values()
                    .stream()
                    .<JsonObject>map(netSocketConnection ->
                            new JsonObject()
                                    .put("id", netSocketConnection.getId())
                                    .put("lastActive", netSocketConnection.getLastActive().toString())
                                    .put("created", netSocketConnection.getCreated().toString()))
                    .collect(Collectors.toList());

            message.reply(new JsonObject().put("count", result.size()).put("items", result));
        }
    }

    private void setupConnectionChecker() {
        if (this.timerId.get() == 0L && !this.connections.isEmpty()) {
            this.timerId.set(this.vertx
                    .setTimer(ORPHANED_CONNECTION_INTERVAL,
                            handler -> this.checkForStaleConnections()));
        }
    }

    private void checkForStaleConnections() {
        LocalDateTime now = LocalDateTime.now();
        LOGGER.debug("Checking for stale connections at {}.", now.toString());
        Observable
                .fromIterable(this.connections
                        .values())
                .filter(connection -> connection
                        .isStaleConnectionSuspect(Duration.ofMinutes(STALE_CONNECTION_TIMEOUT_MINUTES), now))
                .flatMapCompletable(connection -> connection.closeConnection())
                .andThen(Completable.fromAction(() -> this.timerId.set(0L)))
                .doOnComplete(() -> LOGGER.debug("Check for stale connections completed."))
                .subscribe(() -> this.setupConnectionChecker(),
                        throwable -> LOGGER.error("Could not close connection.", throwable));
    }

    // The sending class is responsible for closing the actual connection. This just maintains the map of the connections.
    private void handleClosedConnection(Message<JsonObject> jsonObjectMessage) {
        // Expects the id to be present in the body of the message.
        if (jsonObjectMessage.body().containsKey("id")) {
            String id = jsonObjectMessage.body().getString("id");
            LOGGER.debug("NetSocketConnection with id {} closed connection. Removing from map.", id);
            this.connections.remove(id);
        }
    }

    private void handleConnection(NetSocket netSocket) {
        String id = UUID.randomUUID().toString();
        NetSocketConnection netSocketConnection = NetSocketConnection.with(id, netSocket, this.vertx,
                CLOSED_CONNECTION_PUBLISH_ADDRESS, METRICS_PUBLISH_ADDRESS);
        this.connections.put(id, netSocketConnection);
        this.setupConnectionChecker();
    }
}
