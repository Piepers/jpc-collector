package me.piepers.jpc.client;

import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects metrics of solar panels. Metrics are first converted to a Pojo and then sent across the event bus. This
 * veritcle subscribes to an address to which these metrics are published and tries to put them in a specific bucket of
 * the Influx Database.
 */
public class InfluxDbClientVerticle extends AbstractVerticle {
    private static final String METRICS_PUBLISH_ADDRESS = "jpc.collector.metrics";
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDbClientVerticle.class);

    private InfluxConfig influxConfig;
    @Override
    public void init(io.vertx.core.Vertx vertx, Context context) {
        super.init(vertx, context);
        this.influxConfig = InfluxConfig.from(context.config());
    }

    @Override
    public void start(Promise<Void> startFuture){
        // Connect to the influxdb?

        // Start the consumer.
        this.vertx
                .eventBus()
                .<JsonObject>consumer("jpc.collector.metrics", message ->this.handleMetricsMessage(message.body()));
    }

    private void handleMetricsMessage(JsonObject message) {

    }

}
