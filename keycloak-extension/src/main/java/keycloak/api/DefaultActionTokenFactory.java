package keycloak.api;

import org.keycloak.authentication.actiontoken.DefaultActionToken;

public interface DefaultActionTokenFactory {
    DefaultActionToken create(String userId, String email, int absoluteExpirationInSecs, String redirectUri, String clientId);
}
