package me.piepers.jpc;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.reactivex.core.AbstractVerticle;
import me.piepers.jpc.collector.JpcCollectorVerticle;

/**
 * The starting point of the JPC Collector application.
 */
public class CollectorApplication extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startFuture) throws Exception {
    this.vertx
      .rxDeployVerticle(JpcCollectorVerticle.class.getName(), new DeploymentOptions())
      .subscribe(result -> startFuture.complete(),
        throwable -> startFuture.fail(throwable));
  }
}
