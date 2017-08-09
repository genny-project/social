package life.genny.social;

public class SocialMediaAccountNotFoundException extends RuntimeException {
    public SocialMediaAccountNotFoundException(String accountId) {
        super("Social Media Account not found: " + accountId);
    }
}
