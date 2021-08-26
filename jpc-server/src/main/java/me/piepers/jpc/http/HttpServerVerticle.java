package me.piepers.jpc.http;

import io.reactivex.Single;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import me.piepers.jpc.collector.JpcCollectorVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple http server that supports rest calls primarily for admin tasks.
 */
public class HttpServerVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpcCollectorVerticle.class);
    private static final int DEFAULT_HTTP_PORT = 8080;
    private int port;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        JsonObject httpServerConfig = context.config().getJsonObject("http_server");
        this.port = Objects.nonNull(httpServerConfig) ? httpServerConfig.getInteger("port", DEFAULT_HTTP_PORT) : DEFAULT_HTTP_PORT;
    }

    @Override
    public void start(Promise promise) {
        Router router = Router.router(vertx);
        Router subRouter = Router.router(vertx);
        subRouter.route(HttpMethod.GET, "/connections").handler(this::getTcpConnections);
        router.mountSubRouter("/api", subRouter);

        this.vertx
                .createHttpServer(new HttpServerOptions())
                .requestHandler(router)
                .rxListen(this.port)
                .doOnSuccess(result -> LOGGER.debug("Http Server has been started on port {}", this.port))
                .subscribe(result -> promise.complete(result),
                        throwable -> promise.fail(throwable));
    }

    private void getTcpConnections(RoutingContext routingContext) {
        this.getTcpConnections()
                .subscribe(result -> routingContext
                                .response()
                                .setStatusCode(200)
                                .putHeader("Content-Type", "application/json")
                                // FIXME: stricter in production
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(result.encode()),
                        throwable -> routingContext.fail(throwable));
    }

    private Single<JsonObject> getTcpConnections() {
        return vertx
                .eventBus()
                .<JsonObject>rxRequest("get.connections", new JsonObject())
                .map(result -> result.body());

    }
}
