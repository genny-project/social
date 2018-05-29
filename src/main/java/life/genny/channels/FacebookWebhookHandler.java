package life.genny.channels;

import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.ext.web.RoutingContext;
import life.genny.channel.Producer;
import life.genny.util.PropertiesJsonDeserializer;

public class FacebookWebhookHandler {
	
	  private static String FB_VERIFY_TOKEN =  System.getenv("FB_VERIFY_TOKEN") != null ? (System.getenv("FB_VERIFY_TOKEN")) : "MEMBERHUB";

	 private static final Gson gson = new GsonBuilder()
		        .registerTypeAdapter(Properties.class, PropertiesJsonDeserializer.getPropertiesJsonDeserializer())
		        .create();
	  
	  public static void apiPostFacebookWebhookHandler(final RoutingContext routingContext) {
		    routingContext.request().bodyHandler(body -> {
		    	
		    	// /facebook?hub.mode=subscribe&hub.challenge=1974814172&hub.verify_token=MEMBERHUB2
		    	  JsonObject webhookPayload = body.toJsonObject();
		    	  System.out.println("Facebook Webhook!");
		    	  System.out.println(webhookPayload);
		    	  
		    	  
		    	  
		    	  
		          routingContext.response().putHeader("Content-Type", "application/json");
		          routingContext.response().setStatusCode(200).end();

		    });
		  }
	  
	  public static void apiGetFacebookWebhookHandler(final RoutingContext routingContext) {
		    routingContext.request().bodyHandler(body -> {
		    	
		    	// /facebook?hub.mode=subscribe&hub.challenge=1974814172&hub.verify_token=MEMBERHUB2
		    	   final HttpServerRequest req = routingContext.request();
				    String challenge = req.getParam("hub.challenge");
				    String mode = req.getParam("hub.mode");
				    String verify = req.getParam("hub.verify_token");
				    if (FB_VERIFY_TOKEN.equalsIgnoreCase(verify )) {
		    	  System.out.println("Facebook GET Webhook! got "+challenge+":"+mode+":"+verify);
		          routingContext.response().putHeader("Content-Type", "application/json");
		          routingContext.response().setStatusCode(200).end(challenge);
				    } else {
				    	  System.out.println("Facebook GET Webhook! Error! verify="+verify);
				          routingContext.response().putHeader("Content-Type", "application/json");
				          routingContext.response().setStatusCode(200).end(challenge);
			    	
				    }

		    });
		  }
	  
	
}
