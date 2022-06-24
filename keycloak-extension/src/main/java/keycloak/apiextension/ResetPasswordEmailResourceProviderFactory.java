package keycloak.apiextension;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class ResetPasswordEmailResourceProviderFactory extends AbstractEmailResourceProviderFactory {

    public static final String ID = "reset-password-email";

    public ResetPasswordEmailResourceProviderFactory() {
        super(ID);
    }

    public RealmResourceProvider create(KeycloakSession session) {
        return new ResetPasswordEmailResourceProvider(session);
    }

}
