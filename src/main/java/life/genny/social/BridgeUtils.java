package life.genny.social;

import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.ext.auth.oauth2.AccessToken;

public class BridgeUtils {
	
	
	private static Hashtable<AccessToken, String> tokens = new Hashtable<AccessToken, String>();

	public static Set<Entry<AccessToken, String>> getTokens() {
		return tokens.entrySet();
	}

	public static void setTokens(AccessToken token) {
		System.out.println("call");
		tokens.keySet().stream().filter(token1 -> token1.principal().equals(token.principal())).forEach(System.out::println);
		tokens.put(token, token.principal().getString("preferred_username"));
	}
	
}
