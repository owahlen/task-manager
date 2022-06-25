package keycloak.api;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class SendVerifyEmailResourceProviderFactory extends AbstractEmailResourceProviderFactory {

    public static final String ID = "send-verify-email";

    public SendVerifyEmailResourceProviderFactory() {
        super(ID);
    }

    public RealmResourceProvider create(KeycloakSession session) {
        return new SendVerifyEmailResourceProvider(session);
    }

}
