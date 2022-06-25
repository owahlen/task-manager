package keycloak.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.authentication.actiontoken.DefaultActionToken;

public class VerifyEmailRestActionToken extends DefaultActionToken {
    public static final String TOKEN_TYPE = "verify-email-rest";

    private static final String JSON_FIELD_REDIRECT_URI = "reduri";

    public VerifyEmailRestActionToken(String userId, int absoluteExpirationInSecs, String email, String redirectUri, String clientId) {
        super(userId, TOKEN_TYPE, absoluteExpirationInSecs, null);
        setEmail(email);
        setRedirectUri(redirectUri);
        this.issuedFor = clientId;
    }

    private VerifyEmailRestActionToken() {
    }

    @JsonProperty(value = JSON_FIELD_REDIRECT_URI)
    public String getRedirectUri() {
        return (String) getOtherClaims().get(JSON_FIELD_REDIRECT_URI);
    }

    @JsonProperty(value = JSON_FIELD_REDIRECT_URI)
    public final void setRedirectUri(String redirectUri) {
        if (redirectUri == null) {
            getOtherClaims().remove(JSON_FIELD_REDIRECT_URI);
        } else {
            setOtherClaims(JSON_FIELD_REDIRECT_URI, redirectUri);
        }
    }
}
