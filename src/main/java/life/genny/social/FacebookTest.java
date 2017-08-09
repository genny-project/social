package life.genny.social;

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

public final class FacebookTest {

    private static final String NETWORK_NAME = "Facebook";
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v2.8/me";
    private static final String PROTECTED_RESOURCE_URL3 = "https://graph.facebook.com/v2.8/me?fields=id,name,about,age_range,birthday,email,first_name,gender,last_name,relationship_status,timezone,hometown,favorite_athletes,family,friends";

    private FacebookTest() {
    }

    public static void main(String... args) throws IOException, InterruptedException, ExecutionException {
        // Replace these with your client id and secret
        final String clientId = System.getenv("FACEBOOK_CLIENTID");
        final String clientSecret =  System.getenv("FACEBOOK_SECRET");
        final String secretState = "secret" + new Random().nextInt(999_999);
        final OAuth20Service service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .state(secretState)
                .callback("http://acrow.outcome-hub.com:8083/social/oauth_callback/")
                .build(FacebookApi.instance());

        final Scanner in = new Scanner(System.in, "UTF-8");

        System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        final String authorizationUrl = service.getAuthorizationUrl();
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize ScribeJava here:");
        System.out.println(authorizationUrl);
        System.out.println("And paste the authorization code here");
        System.out.print(">>");
        final String code = in.nextLine();
        System.out.println();

//        System.out.println("And paste the state from server here. We have set 'secretState'='" + secretState + "'.");
//        System.out.print(">>");
//        final String value = in.nextLine();
//        if (secretState.equals(value)) {
//            System.out.println("State value does match!");
//        } else {
//            System.out.println("Ooops, state value does not match!");
//            System.out.println("Expected = " + secretState);
//            System.out.println("Got      = " + value);
//            System.out.println();
//        }

        // Trade the Request Token and Verfier for the Access Token
        System.out.println("Trading the Request Token for an Access Token...");
        final OAuth2AccessToken accessToken = service.getAccessToken(code);
        System.out.println("Got the Access Token!");
        System.out.println("(if you're curious it looks like this: " + accessToken
                + ", 'rawResponse'='" + accessToken.getRawResponse() + "')");
        System.out.println();

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL3);
        service.signRequest(accessToken, request);
        final Response response = service.execute(request);
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());
        JsonObject jObject = new JsonObject(response.getBody().trim());

        jObject.fieldNames().forEach(k ->
        {
        System.out.println(k);
        		Object fieldData = jObject.getValue(k);
        		System.out.println("Facebook :"+k+":"+fieldData.toString());
        }); 

        System.out.println();
 
    }
}