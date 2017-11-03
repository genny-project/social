package life.genny.social;

import io.vertx.rxjava.core.AbstractVerticle;

import io.vertx.rxjava.core.Future;
import life.genny.channels.Routers;
import life.genny.cluster.Cluster;


public class ServiceVerticle extends AbstractVerticle {

	@Override
	public void start() {
	    System.out.println("Setting up routes");
	    final Future<Void> startFuture = Future.future();
	    Cluster.joinCluster(vertx).compose(res -> {
	    	Routers.routers(vertx);
	
	      startFuture.complete();
	    }, startFuture);	}
}
