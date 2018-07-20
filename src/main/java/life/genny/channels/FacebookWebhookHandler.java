package life.genny.channels;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.ext.web.RoutingContext;
import life.genny.channel.Producer;
import life.genny.qwanda.Answer;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.qwandautils.SecurityUtils;

import life.genny.util.PropertiesJsonDeserializer;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.BaseEntityUtils2;
import life.genny.utils.VertxUtils;

public class FacebookWebhookHandler {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static final String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");

	private static String FB_VERIFY_TOKEN = System.getenv("FB_VERIFY_TOKEN") != null
			? (System.getenv("FB_VERIFY_TOKEN"))
			: "MEMBERHUB";

	public static void apiPostFacebookWebhookHandler(final RoutingContext routingContext) {
		routingContext.request().bodyHandler(body -> {

			// /facebook?hub.mode=subscribe&hub.challenge=1974814172&hub.verify_token=MEMBERHUB2
			JsonObject webhookPayload = body.toJsonObject();
			System.out.println("Facebook Webhook!");
			System.out.println(webhookPayload);

			// {"entry":[
			// {"time":1528025366,
			// "changes":[
			// {"field":"birthday_date",
			// "value":"06/14/1961"}
			// ],
			// "id":"101305713992774",
			// "uid":"101305713992774"}
			// ],
			// "object":"user"}

			if (webhookPayload.containsKey("object")) {
				String objType = webhookPayload.getString("object");

				if ("user".equalsIgnoreCase(objType)) {
					JsonArray entrys = webhookPayload.getJsonArray("entry");
					for (int entryId = 0; entryId < entrys.size(); entryId++) {
						JsonObject entry = entrys.getJsonObject(entryId);
						String realm = System.getenv("PROJECT_REALM");
						log.info("service realm:" + realm);
						String token = BaseEntityUtils2.generateServiceToken(realm);
						log.info("service token:" + token);

						String fbid = entry.getString("uid");
						log.info("fbid:" + fbid);
	
						BaseEntity user  = null;
						if (!fbid.equals("0")) {
							String jsonBe = getBaseEntityJsonByAttributeAndValue("FBK_ID", fbid, token);
							user = JsonUtils.fromJson(jsonBe, BaseEntity.class);
						}
							JsonArray changes = entry.getJsonArray("changes");
							for (int changeId = 0; changeId < changes.size(); changeId++) {
								JsonObject change = changes.getJsonObject(changeId);
								String field = change.getString("field");
								String value = null;
								
								try {
									value = change.getString("value");
								} catch (Exception e) {
									try {
										JsonArray valueArray = change.getJsonArray("value");
										value = valueArray.toString();
									} catch (Exception e1) {
										JsonObject valueJson = change.getJsonObject("value");
										value = valueJson.toString();
									}
								}

								if (!fbid.equals("0")) {
									log.info("Test facebook data sent!");
									sendFacebookAnswer(user, field, value);
								} else {
									log.info("field:"+field+",value:"+value);
								}
							}
	
					}
				}

			}

			routingContext.response().putHeader("Content-Type", "application/json");
			routingContext.response().setStatusCode(200).end();

		});
	}

	public static void sendFacebookAnswer(BaseEntity user, String facebookField, String value) {
		String targetCode = user.getCode();
		String attributeCode = "FBK_" + facebookField.toUpperCase();
		Answer answer = new Answer("SOC_FB_BASIC", targetCode, attributeCode, value);
		QDataAnswerMessage msg = new QDataAnswerMessage(answer);
		String payload = JsonUtils.toJson(msg);
		VertxUtils.publish(user, "data", payload);
	}

	/**
	 *
	 * @param qwandaServiceUrl
	 * @param decodedToken
	 * @param token
	 * @return baseEntity user for the decodedToken passed
	 */
	public static String getBaseEntityJsonByAttributeAndValue(final String attributeCode, final String value,
			final String token) {

		try {
			String beJson = null;
			beJson = QwandaUtils.apiGet(
					qwandaServiceUrl + "/qwanda/baseentitys/test2?pageSize=" + 1000 + "&" + attributeCode + "=" + value,
					token);
			return beJson;

		} catch (IOException e) {
			log.error("Error in fetching Base Entity from Qwanda Service");
		}
		return null;

	}

	public static void apiGetFacebookWebhookHandler(final RoutingContext routingContext) {
		routingContext.request().bodyHandler(body -> {

			// /facebook?hub.mode=subscribe&hub.challenge=1974814172&hub.verify_token=MEMBERHUB2
			final HttpServerRequest req = routingContext.request();
			String challenge = req.getParam("hub.challenge");
			String mode = req.getParam("hub.mode");
			String verify = req.getParam("hub.verify_token");
			if (FB_VERIFY_TOKEN.equalsIgnoreCase(verify)) {
				System.out.println("Facebook GET Webhook! got " + challenge + ":" + mode + ":" + verify);
				routingContext.response().putHeader("Content-Type", "application/json");
				routingContext.response().setStatusCode(200).end(challenge);
			} else {
				System.out.println("Facebook GET Webhook! Error! verify=" + verify);
				routingContext.response().putHeader("Content-Type", "application/json");
				routingContext.response().setStatusCode(200).end(challenge);

			}

		});
	}

}
