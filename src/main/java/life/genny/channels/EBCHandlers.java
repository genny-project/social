package life.genny.channels;

import java.lang.reflect.Type;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import com.google.gson.FieldNamingPolicy;

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
import javassist.tools.framedump;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.DateTimeDeserializer;
import life.genny.qwanda.Link;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QEventMessage;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.MergeUtil;
import life.genny.qwandautils.QwandaUtils;

import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.Person;

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
	              final JsonDeserializationContext jsonDeserializationContext)
	              throws JsonParseException {
	            return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	          }

		public JsonElement serialize(final LocalDateTime date, final Type typeOfSrc, final JsonSerializationContext context) {
			return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // "yyyy-mm-dd"
		}
	}).create();
	
	
	public static void registerHandlers(final EventBus eventBus){
		EBConsumers.getFromSocial().subscribe(arg -> {
			logger.info("Received Facebook Code! - data");
			final JsonObject payload = new JsonObject(arg.body().toString());
			System.out.println("------------------------------------------------------------------------");
			System.out.println("Facebook Code   ::   "+payload.toString()); 
			System.out.println("------------------------------------------------------------------------\n");
			String token = payload.getString("token");
			String userCode= KeycloakUtils.getDecodedToken(token).getString("preferred_username");
			//System.out.println(userCode);
			try {
				getFacebookData(payload, userCode);
			} catch (IOException | InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
		
	}
	
	
    private static final String NETWORK_NAME = "Facebook";
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v2.8/me";
    private static final String PROTECTED_RESOURCE_URL3 = "https://graph.facebook.com/v2.8/me?fields=id,name,about,age_range,birthday,email,first_name,gender,last_name,relationship_status,timezone,hometown,favorite_athletes,family,friends";
    public static final String sourceCode = "PER_USER1";
    public static final String linkFriend = "LNK_FRIEND";
    public static final String linkFamily= "LNK_FRIEND";
    
	public static void getFacebookData(final JsonObject msg, final String state) throws IOException, InterruptedException, ExecutionException {
//		System.out.println("Here is the user state   ::   " + state);
//		final String msgString = msg.toString();
//		System.out.println(msgString);
			
		final String clientId = System.getenv("FACEBOOK_CLIENTID");
		final String clientSecret =  System.getenv("FACEBOOK_SECRET");
		final String callbackUrl =  System.getenv("SOCIAL_CALLBACK_URL");
		final String secretState = state;
		System.out.println("Client ID   ::   "+ clientId);
		System.out.println("Client Secret   ::   "+ clientSecret);
		System.out.println("Secret State   ::   "+ secretState);
		final OAuth20Service service = new ServiceBuilder(clientId)
				.apiSecret(clientSecret)
				.state(secretState)
				.callback(callbackUrl)
				.build(FacebookApi.instance());

		// Get Authorization URL
		final String authorizationUrl = service.getAuthorizationUrl();
		System.out.println("Recieved Authorization URl   ::   "+authorizationUrl);
		// Get Access Token
		final OAuth2AccessToken accessToken = service.getAccessToken(msg.getString("value"));
		System.out.println("Access Token   ::   " + accessToken);
		System.out.println("Raw response   ::   " + accessToken.getRawResponse());
		
		final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL3);
		service.signRequest(accessToken, request);
		final Response response = service.execute(request);
		
		System.out.println("------------------------------------------------------------------------");
		System.out.println("SUCCESS CODE   ::   "+response.getCode());
		System.out.println("FACEBOOK DATA  ::   "+response.getBody());
		System.out.println("------------------------------------------------------------------------\n");
		
		// GET answer data
		String targetCode= msg.getString("targetCode");
		Boolean expired= msg.getBoolean("expired");
		Boolean refused= msg.getBoolean("refused");
		String token= msg.getString("token");
		
		System.out.println("Target Code   ::   "+ targetCode);
		System.out.println("Expired       ::   "+ expired);
		System.out.println("Refused       ::   "+ refused);

		JsonObject fbData = new JsonObject(response.getBody().trim());
		System.out.println("------------------------------------------------------------------------");
		
		// Initialize JsonObjects
		JsonObject friendObj = new JsonObject();
		JsonObject familyObj = new JsonObject();
		JsonArray friendList = new JsonArray();
		JsonArray familyList = new JsonArray();		
		int totalFriends = 0;
		
		for (String key : fbData.fieldNames()) {
			
			String attributeCode = getAttributeCode(key);
			Object fieldValue = fbData.getValue(key);
			System.out.println(attributeCode + "   ::   " + fieldValue.toString());
			
			// Store friends and family
			if (attributeCode.equals("FBK_FRIENDS")) {
				friendObj = fbData.getJsonObject(key);
				friendList = friendObj.getJsonArray("data");
				totalFriends = friendObj.getJsonObject("summary").getInteger("total_count");
				continue;
			}
			if (attributeCode.equals("FBK_FAMILY")) {
				familyObj = fbData.getJsonObject(key);
				familyList = familyObj.getJsonArray("data");		
				continue;
			}
			
			// PREPARE JSON to send answer
			JsonObject data = new JsonObject();
			data.put("sourceCode", "SOC_FB_BASIC");
			data.put("targetCode", targetCode);
			data.put("expired", expired);
			data.put("refused", refused);
			data.put("weight", 1);
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

		}
		System.out.println("----------------------------------------------------------------------");
		System.out.println("\nFRIEND CLASS TYPE  ::  " + friendObj.getClass().getSimpleName());
		System.out.println("\nTOTAL FRIENDS      ::  " + totalFriends);
		System.out.println("\nFRIEND OBJ         ::  " + friendObj);
		System.out.println("\nFRIEND LIST        ::  " + friendList);
		
		System.out.println("----------------------------------------------------------------------");
		System.out.println("\nFAMILY CLASS TYPE  ::  " + familyObj.getClass().getSimpleName());
		System.out.println("\nFAMILY OBJ         ::  " + familyObj);
		System.out.println("\nFAMILY LIST        ::  " + familyList);
		System.out.println("----------------------------------------------------------------------");
		
		for(Object obj1 : friendList) {
			
			// convert plain object -> JsonObject
			JsonObject friendobj = (JsonObject) obj1;
			
			//get name, id, targetCode, image_url			
			Long id = Long.parseLong(friendobj.getString("id"));
			String code = getTargetCode(friendobj);
			String name = friendobj.getString("name");
			
			String idValue= friendobj.getString("id");
			String image_url= getFacebookImage(idValue);
			
			
						
			System.out.println("FACEBOOK ID   ::  "+ id);		
			System.out.println("BE CODE       ::  "+ code);
			System.out.println("NAME          ::  "+ name);
			System.out.println("FACEBOOK IMG  ::  "+ image_url);
			System.out.println("---------------------------------------------");
			
			Link link = new Link(sourceCode, code, linkFriend);
			Answer imgAnswer = new Answer(code, code, "FBK_IMGURL", image_url);
			Answer idAnswer = new Answer(code, code, "FBK_ID", idValue);
			List<Answer> answerList = new ArrayList<Answer>();	
			answerList.add(imgAnswer);
			answerList.add(idAnswer);
			
			for(Object obj2 : familyList) {
				// convert plain object -> JsonObject
				JsonObject familyobj = (JsonObject) obj2;
				
				if( ( familyobj.getString("id") ).equals( friendobj.getString("id") ) ) {					
					String relationship= familyobj.getString("relationship");
					System.out.println("Relationship found   ::   "+relationship );
					Answer relnAnswer = new Answer(code, code, "FBK_RELATIONSHIP", relationship);
					answerList.add(relnAnswer);
				}
				
			}
			
			createBaseEntity(link, name, token, answerList);		
			System.out.println("----------------------------------------------------------------------\n");
		}
		
		

	}
	
	public static String getAttributeCode(String key) {
		String initial = "FBK_";
		String fieldKey = key.toUpperCase();
		String attributeCode = initial + fieldKey;
		return attributeCode;
	}
	
	public static String getTargetCode(JsonObject friendobj) {
		Long id = Long.parseLong(friendobj.getString("id"));
		String initial= "PER_";
		String code = initial + id;
		return code;
	}
	
	public static String getFacebookImage(String idValue) {
		String image_url= "http://graph.facebook.com/" + idValue + "/picture" ;
		return image_url;
	}
	
	public static boolean createBaseEntity(Link link, String name, String token, List<Answer> answerList) {
		
		BaseEntity be = new BaseEntity(link.getTargetCode(), name);
		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		
		Gson gson1 = new Gson();
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer());
		gson1 = gsonBuilder.create();
        
        String jsonBE = gson1.toJson(be);
        try {
        		// save BE
            QwandaUtils.apiPostEntity(qwandaServiceUrl + "/qwanda/baseentitys", jsonBE, token);
            // link PER_USER1 to friends
            QwandaUtils.apiPostEntity(qwandaServiceUrl + "/qwanda/entityentitys", gson1.toJson(link),token);
            // save attributes
            int i=1;
			for (Answer answer : answerList) {
				System.out.println("Answer      " + i + "::   " + answer.toString());
				QwandaUtils.apiPostEntity(qwandaServiceUrl + "/qwanda/answers",
						gson1.toJson(answer), token);
				i++;
			}                 
            
        }catch (Exception e) {
            e.printStackTrace();
        }
		
		return true;
		
	}
	
	public static void main(String...str) throws IOException, InterruptedException, ExecutionException {
		
	}
}