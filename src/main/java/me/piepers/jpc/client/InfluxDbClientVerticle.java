package me.piepers.jpc.client;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import me.piepers.jpc.domain.GrowattDataMessage;
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
    private String connectionString;

    @Override
    public void init(io.vertx.core.Vertx vertx, Context context) {
        super.init(vertx, context);
        this.influxConfig = InfluxConfig.from(context.config());
        this.connectionString = influxConfig.getProtocol() + "://" + influxConfig.getHost() + ":" + influxConfig.getPort();
    }

    @Override
    public void start(Promise<Void> startFuture) {
        // Start the consumer.
        this.vertx
                .eventBus()
                .<JsonObject>consumer(METRICS_PUBLISH_ADDRESS,
                        message -> this.handleMetricsMessage(message.body()));
    }

    private void handleMetricsMessage(JsonObject data) {
        LOGGER.debug("Measurement received, writing to influxDb...");
        // FIXME: must be able to deal with unresponsive influxdb, I/O and other related errors. Could be an Observable or Completable that autocloses (Completable.using?).
        try (InfluxDBClient client = InfluxDBClientFactory
                .create(connectionString, influxConfig.getToken().toCharArray());
             WriteApi writeApi = client.getWriteApi()) {
            LOGGER.debug("Adding the following to influx:\n{}", data.encodePrettily());
            writeApi.writeMeasurement(this.influxConfig.getBucketName(), this.influxConfig.getOrganization(),
                    WritePrecision.S, new GrowattDataMessage(data));
        }
    }
}
