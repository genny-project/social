package life.genny.channels;


import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;

public class Routers {

	private static int serverPort = 8085;
	
	public static void routers(Vertx vertx) {
		Router router = Router.router(vertx);
		router.route().handler(RouterHandlers.cors());
		router.route(HttpMethod.GET, "/social/oauth_callback").handler(RouterHandlers::apiGetInitHandler);
		vertx.createHttpServer().requestHandler(router::accept).listen(serverPort);
	}
}
