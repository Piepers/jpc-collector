package me.piepers.jpc.http;

import io.vertx.reactivex.core.AbstractVerticle;
import me.piepers.jpc.collector.JpcCollectorVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple http server that supports rest calls primarily for admin tasks.
 */
public class HttpServerVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpcCollectorVerticle.class);


}
