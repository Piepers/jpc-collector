package me.piepers.jpc.collector;

import io.netty.util.collection.ByteCollections;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.eventbus.Message;
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
    private static final String CLOSED_CONNECTION_PUBLISH_ADDRESS = "jpc.collector";

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
                CLOSED_CONNECTION_PUBLISH_ADDRESS);
        this.connections.put(id, netSocketConnection);
    }
}
