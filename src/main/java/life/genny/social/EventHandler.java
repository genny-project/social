package life.genny.social;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.ext.web.handler.BodyHandler;

public class EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);

    private static final String NETWORK_NAME = "Facebook";
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v2.8/me";
    private static final String PROTECTED_RESOURCE_URL2 = "https://graph.facebook.com/v2.8/me?fields=id,name,about,age_range,birthday,email,first_name,gender,last_name,relationship_status,sports,timezone,hometown,context,favorite_athletes,family,friends";
    private static final String PROTECTED_RESOURCE_URL3 = "https://graph.facebook.com/v2.8/me?fields=id,name,about,age_range,birthday,email,first_name,gender,last_name,relationship_status,timezone,hometown,favorite_athletes,family,friends";

	EventBus eventBus;
	
	Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
		@Override
		public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		    return ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()).toLocalDateTime();
		}
		
		 public JsonElement serialize(LocalDateTime date, Type typeOfSrc, JsonSerializationContext context) {
       return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // "yyyy-mm-dd"
   }
		}).create();		
	

	
    public EventHandler(EventBus eventBus) {
  
    		this.eventBus = eventBus;
    }



    public void handleGetEvent(RoutingContext context) {
		context.vertx().executeBlocking(future -> {
			try {

    	// Only event messages should go here
       String oauthCode = context.request().getParam("code");
       String state = context.request().getParam("state");
       String user = System.getenv("DEVUSER");
  
      //      context.vertx().eventBus().publish("auction." + auctionId, context.getBodyAsString());
 //    json.put("token", token);
 //   	JsonObject rawMessage = json.getJsonObject("data");
//		rawMessage.put("token", tokenAccessed.principal().toString());
    	logger.info("Got code:"+oauthCode+" and state="+state);
  //      eventBus.publish("events", json);
 	
        final String clientId = System.getenv("FACEBOOK_CLIENTID");
        final String clientSecret =  System.getenv("FACEBOOK_SECRET");
        final String secretState = "secret" + new Random().nextInt(999_999);
        final OAuth20Service service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .state(secretState)
                .callback("http://"+user+".outcome-hub.com:8083/social/oauth_callback/")
                .build(FacebookApi.instance());

  //      System.out.println("Trading the Request Token for an Access Token...");
        OAuth2AccessToken accessToken=null;
		try {
			accessToken = service.getAccessToken(oauthCode);
		} catch (IOException | InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        System.out.println("Got the Access Token!");
//        System.out.println("(if your curious it looks like this: " + accessToken
//                + ", 'rawResponse'='" + accessToken.getRawResponse() + "')");
//        System.out.println();

        // Now let's go and ask for a protected resource!
    //    System.out.println("Now we're going to access a protected resource...");
        final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL3);
        service.signRequest(accessToken, request);
        Response response;
		try {
			response = service.execute(request);
//	        System.out.println("Got it! Lets see what we found...");
	        System.out.println();
	        System.out.println(response.getCode());
	        System.out.println(response.getBody());
	        
	        // Process the data
	        String resultData = response.getBody();
	        
	        JsonObject jObject = new JsonObject(resultData.trim());

	        jObject.fieldNames().forEach(k ->
	        {
	        		Object fieldData = jObject.getValue(k);
	        		String attributeCode = "FBK_"+k.toUpperCase();
	        		if (fieldData instanceof String) {	        			
	        			System.out.println(attributeCode+":"+fieldData.toString()+":"+String.class.getSimpleName());
	        		}
	        		if (fieldData instanceof Integer) {	        			
	        			System.out.println(attributeCode+":"+Integer.parseInt(fieldData.toString())+":"+Integer.class.getSimpleName());
	        		}
	        		if (fieldData instanceof JsonObject) {	        			
	        			System.out.println(attributeCode+":"+fieldData.toString()+":"+JsonObject.class.getSimpleName());
	        		}
	        		
	        }); 	        // Save the data
	        

		} catch (InterruptedException | ExecutionException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// future.complete(kSession);
	} catch (Throwable t) {
		t.printStackTrace();
	}
	future.complete();
}, res -> {
	if (res.succeeded()) {
	}
});

            context.response()
                .setStatusCode(200)
                .end();
  
    }

	public EventBus getEventBus() {
		return eventBus;
	}

	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

 
}
