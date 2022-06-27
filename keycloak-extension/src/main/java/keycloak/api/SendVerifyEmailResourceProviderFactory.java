package keycloak.api;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * This factory adds a REST endpoint to Keycloak that triggers an email verification flow of a Keycloak user
 * without the need of the Keycloak UI.
 * <p>
 * It requires the following parameters:
 * user_id: uuid of the user whose email shall be verified
 * client_id: the client the user will use in order to verify his/her email
 * redirect_uri: a URL to which Keycloak will redirect to after the verification was successful
 * The format is as follows:
 * http://<host>:<port>/auth/realms/TaskManager/send-verify-email/users/<user_id>?client_id=<client_id>&redirect_uri=<redirect_uri>
 * <p>
 * The flow works as follows:
 * The endpoint will trigger Keycloak to email the user a message with a verification link.
 * When the user clicks on this link (within 12 hours) his/her email will be verified in Keycloak
 * and he/she will get redirected to the provided redirect_uri.
 * In case of errors (like link expiration) an error page in Keycloak is shown.
 */
public class SendVerifyEmailResourceProviderFactory extends AbstractEmailResourceProviderFactory {

    public static final String ID = "send-verify-email";

    public SendVerifyEmailResourceProviderFactory() {
        super(ID);
    }

    public RealmResourceProvider create(KeycloakSession session) {
        return new SendVerifyEmailResourceProvider(session);
    }

}
