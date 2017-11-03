package life.genny.channels;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.CorsHandler;
import life.genny.channels.EBCHandlers;
import life.genny.channels.EBProducers;
;

public class RouterHandlers {

  private static String vertxUrl = System.getenv("REACT_APP_VERTX_URL");
  private static String hostIP =
      System.getenv("HOSTIP") != null ? System.getenv("HOSTIP") : "127.0.0.1";

  private static final Logger logger = LoggerFactory.getLogger(EBCHandlers.class);

  public static CorsHandler cors() {
    return CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.OPTIONS).allowedHeader("X-PINGARUNER")
        .allowedHeader("Content-Type").allowedHeader("X-Requested-With");
  }

  public static void apiGetInitHandler(final RoutingContext routingContext) {
	  System.out.println("12121212121212121212");
    routingContext.request().bodyHandler(body -> {
      // System.out.println("init json=" + fullurl);
      URL aURL = null;
  
		String oauthCode = routingContext.request().getParam("code");
		String state = routingContext.request().getParam("state");
		String user = System.getenv("DEVUSER");

		// context.vertx().eventBus().publish("auction." + auctionId,
		// context.getBodyAsString());
		// json.put("token", token);
		// JsonObject rawMessage = json.getJsonObject("data");
		// rawMessage.put("token", tokenAccessed.principal().toString());
		logger.info("Got code:" + oauthCode + " and state=" + state);
		// eventBus.publish("events", json);

		final String clientId = System.getenv("FACEBOOK_CLIENTID");
		final String clientSecret = System.getenv("FACEBOOK_SECRET");
		final String socialCallbackUrl = System.getenv("FACEBOOK_CALLBACK_URL");
		final String secretState = "secret" + new Random().nextInt(999_999);  
      
      routingContext.response().end();
    });
  }


}
