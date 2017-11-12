package life.genny.channels;

import java.lang.reflect.Type;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kie.api.KieServices;
import org.kie.api.runtime.Globals;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

//import com.apple.eawt.AppEvent.UserSessionEvent;
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
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.ext.auth.oauth2.AccessToken;
import io.vertx.rxjava.ext.auth.oauth2.OAuth2Auth;
import io.vertx.rxjava.ext.auth.oauth2.providers.KeycloakAuth;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.KeycloakUtils;

import java.util.Random;
import java.util.Scanner;
import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
public class EBCHandlers {

	private static final Logger logger = LoggerFactory.getLogger(EBCHandlers.class);
	static KieServices ks = KieServices.Factory.get();
	static KieContainer kContainer;
	final static String qwandaApiUrl = System.getenv("REACT_APP_QWANDA_API_URL");
	final static String vertxUrl = System.getenv("REACT_APP_VERTX_URL");
	final static String hostIp = System.getenv("HOSTIP");
	static KieSession kSession;
	static String token;
	static Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
		@Override
		public LocalDateTime deserialize(final JsonElement json, final Type type,
				final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			return ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()).toLocalDateTime();
		}

		public JsonElement serialize(final LocalDateTime date, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // "yyyy-mm-dd"
		}
	}).create();
	
	
	public static void registerHandlers(final EventBus eventBus){
		EBConsumers.getFromSocial().subscribe(arg -> {
			logger.info("Received Facebook Code! - data");
			final JsonObject payload = new JsonObject(arg.body().toString());
			System.out.println("8888888888888888888888888888888888888888888888888888888888880");
			System.out.println("Facebook Code= "+payload.toString()); 
			System.out.println("8888888888888888888888888888888888888888888888888888888888880");
			try {
				getToken(payload);
			} catch (IOException | InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 JsonObject fbData = new JsonObject();
			 fbData.put("msg_type", "DATA_MSG");
			 fbData.put("data_type", "Answer");
			 fbData.put("items",  "this is a facebook data");
			EBProducers.getToData().write(fbData);
		});
		
	}
	
	
    private static final String NETWORK_NAME = "Facebook";
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v2.8/me";
    private static final String PROTECTED_RESOURCE_URL3 = "https://graph.facebook.com/v2.8/me?fields=id,name,about,age_range,birthday,email,first_name,gender,last_name,relationship_status,timezone,hometown,favorite_athletes,family,friends";
    
    
	public static void main(String...str) throws IOException, InterruptedException, ExecutionException {
	     JsonObject payload = new JsonObject();
	     payload.put("msg_type", "CMD_MSG");
	     payload.put("cmd_type", "SOCIAL_MEDIA_FB_FETCH");
	     payload.put("code", "facebookCode");

		logger.info("Received Facebook Code! - data");
		System.out.println("************************************************************");
		System.out.println("Facebook Code= "+payload.toString()); 
		System.out.println("************************************************************");
		getToken(payload);
		
	}


	public static void getToken(final JsonObject msg) throws IOException, InterruptedException, ExecutionException {
		if (msg.getString("msg_type").equalsIgnoreCase("CMD_MSG")) {
			if (msg.getString("cmd_type").equals("SOCIAL_MEDIA_FB_FETCH")) {
				final String msgString = msg.toString();
				System.out.println(msgString);
				
				final String clientId = System.getenv("FACEBOOK_CLIENTID");
		        final String clientSecret =  System.getenv("FACEBOOK_SECRET");
		        final String secretState = "secret93809";
		        System.out.println(clientId);
		        System.out.println(clientSecret);
		        System.out.println(secretState);
		        final OAuth20Service service = new ServiceBuilder(clientId)
		                .apiSecret(clientSecret)
		                .state(secretState)
		                .callback("http://anishmaharjan.outcome-hub.com:8085/social/oauth_callback/")
		                .build(FacebookApi.instance());

		        //final Scanner in = new Scanner(System.in, "UTF-8");

		        System.out.println();

		        // Get Authorization URL
		        final String authorizationUrl = service.getAuthorizationUrl();
		        System.out.println(authorizationUrl);
		        System.out.println("Paste the authorization code here");
		        System.out.print(">>");
		       // final String code = in.nextLine();
		        System.out.println();
		        
		         final OAuth2AccessToken accessToken = service.getAccessToken(msg.getString("code"));
		         System.out.println("Access Token ::" + accessToken + "Raw response ::" + accessToken.getRawResponse());
		        // System.out.println();
		        
		        final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL3);
		        service.signRequest(accessToken, request);
		        final Response response = service.execute(request);
		        
		        System.out.println("Got it! Lets see what we found...");
		        System.out.println();
		        System.out.println(response.getCode());
		        System.out.println(response.getBody());
		        
		        JsonObject fbData = new JsonObject(response.getBody());
		        EBProducers.getToData().write(fbData);
		        
		        // JsonObject jObject = new JsonObject(response.getBody().trim());
		        
		        // JsonObject answer = new JsonObject();
		     

		        // jObject.fieldNames().forEach(k ->
		        // {
		        // 		System.out.println(k);
		        // 		Object fieldData = jObject.getValue(k);
		        // 		System.out.println("Facebook :"+k+":"+fieldData.toString());
		        		
		    		   
		        // }); 

		        // System.out.println();
			} 			

		}
	}
}