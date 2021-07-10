package me.piepers.jpc;

import io.reactivex.Completable;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import me.piepers.jpc.client.InfluxDbClientVerticle;
import me.piepers.jpc.collector.JpcCollectorVerticle;
import me.piepers.jpc.http.HttpServerVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The starting point of the JPC Collector application.
 */
public class CollectorApplication extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectorApplication.class);

    @Override
    public void start(Promise<Void> startFuture) throws Exception {

        // Se thte main configuration of our application to be used.
        ConfigStoreOptions mainConfigStore = new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", "config/app-conf.json"));

        // In this case for instance the configuration to connect to the influxdb.
        ConfigStoreOptions nonPublicConfigStore = new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", "config/non-public-conf.json"));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(mainConfigStore)
                .addStore(nonPublicConfigStore);

        ConfigRetriever configRetriever = ConfigRetriever.create(this.vertx, options);

        configRetriever
                .rxGetConfig()
                .flatMapCompletable(configuration -> Completable.fromAction(() -> LOGGER.info("Deploying JPC Collector"))
                        .andThen(this.vertx.rxDeployVerticle(JpcCollectorVerticle.class.getName(), new DeploymentOptions().setConfig(configuration)).ignoreElement())
                        .andThen(this.vertx.rxDeployVerticle(HttpServerVerticle.class.getName(), new DeploymentOptions().setConfig(configuration)).ignoreElement())
                        .andThen(this.vertx.rxDeployVerticle(InfluxDbClientVerticle.class.getName(), new DeploymentOptions().setConfig(configuration)).ignoreElement()))
                .doOnComplete(() -> LOGGER.info("Application successfully deployed."))
                .subscribe(() -> startFuture.complete(),
                        throwable -> startFuture.fail(throwable));
    }
}
