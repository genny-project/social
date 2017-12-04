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
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import io.vertx.core.json.JsonArray;
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
	     
	     JsonArray items = new JsonArray();
	     items.add(payload);
	     System.out.println(items);
	     
	     JsonObject msg = new JsonObject();
	     msg.put("data_type", "Answer");
	     msg.put("msg_type", "DATA_MSG");
	     msg.put("code", "facebookCode");
	     msg.put("items", items);

		logger.info("Received Facebook Code! - data");
		System.out.println("************************************************************");
		System.out.println("Facebook Code= "+payload.toString()); 
		System.out.println("************************************************************");
		
		logger.info("Received Facebook Code! - data");
		System.out.println("************************************************************");
		System.out.println("Answer Message= "+msg.toString()); 
		System.out.println("************************************************************");
		//getToken(payload);
		
	}


	public static void getToken(final JsonObject msg) throws IOException, InterruptedException, ExecutionException {

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
				.callback("http://localhost:3000/?data_state=%7B%22sourceCode%22%3A%22SOC_FB_BASIC%22%2C%22targetCode%22%3A%22PER_USER1%22%2C%22attributeCode%22%3A%22PRI_FB_BASIC%22%2C%22askId%22%3A%229%22%7D")
				.build(FacebookApi.instance());

		//final Scanner in = new Scanner(System.in, "UTF-8");

		System.out.println();

		// Get Authorization URL
		final String authorizationUrl = service.getAuthorizationUrl();
		System.out.println(authorizationUrl);
		// final String code = in.nextLine();
		System.out.println();
		
		final OAuth2AccessToken accessToken = service.getAccessToken(msg.getString("value"));
		System.out.println("Access Token ::" + accessToken + "Raw response ::" + accessToken.getRawResponse());
		// System.out.println();
		
		final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL3);
		service.signRequest(accessToken, request);
		final Response response = service.execute(request);
		
		System.out.println("-----------------------------------");
		System.out.println("SUCCESS CODE ::"+response.getCode());
		System.out.println("FACEBOOK DATA ::"+response.getBody());
		System.out.println("-----------------------------------");
		
		// GET answer data
		String targetCode= msg.getString("targetCode");
		Boolean expired= msg.getBoolean("expired");
		Boolean refused= msg.getBoolean("refused");
		String weight= msg.getString("weight");
		String token= msg.getString("token");
		
		System.out.println(targetCode);
		System.out.println(expired);
		System.out.println(refused);
		System.out.println(weight);
		System.out.println(token);

		JsonObject fbData = new JsonObject(response.getBody().trim());
		System.out.println("-----------------------------------");
		fbData.fieldNames().forEach(key ->
		{
			
			String initial= "FBK_";
			String fieldKey= key.toUpperCase();
			String attributeCode= initial + fieldKey;	
			Object fieldValue = fbData.getValue(key);
			System.out.println(attributeCode + "::" +fieldValue.toString() );        
			
			// PREPARE JSON to send answer
			JsonObject data = new JsonObject();
			data.put("sourceCode", "SOC_FB_BASIC");
			data.put("targetCode",targetCode); 
			data.put("expired",expired); 
			data.put("refused", refused);  
			data.put("weight", weight);
			data.put("attributeCode", attributeCode);
			data.put("value", fieldValue.toString());

			JsonArray items = new JsonArray();
			items.add(data);

			// PREPARE answer message
			JsonObject obj = new JsonObject();
			obj.put("msg_type", "DATA_MSG");
			obj.put("data_type", "Answer");
			obj.put("items", items);
			obj.put("token", token);
			EBProducers.getToData().write(obj);
		}); 
		System.out.println("-----------------------------------");
	
		// EBProducers.getToData().write(obj);

	}
}