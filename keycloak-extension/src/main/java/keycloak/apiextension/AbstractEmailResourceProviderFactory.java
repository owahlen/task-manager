package keycloak.apiextension;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public abstract class AbstractEmailResourceProviderFactory implements RealmResourceProviderFactory {

    private final String id;

    public AbstractEmailResourceProviderFactory(String id) {
        this.id = id;
    }

    abstract public RealmResourceProvider create(KeycloakSession session);

    public void init(Scope config) {
    }

    public void postInit(KeycloakSessionFactory factory) {
    }

    public void close() {
    }

    public String getId() {
        return id;
    }
}
