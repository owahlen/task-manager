package keycloak.apiextension;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public abstract class AbstractEmailResourceProvider implements RealmResourceProvider {

    protected final KeycloakSession session;

    public AbstractEmailResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    public void close() {
    }

    public Object getResource() {
        return this;
    }

}