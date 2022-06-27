package keycloak.api;

import org.jboss.logging.Logger;
import org.keycloak.authentication.actiontoken.DefaultActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.resources.LoginActionsService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.concurrent.TimeUnit;

public class EmailActionExecutor {

    private static final Logger log = Logger.getLogger(EmailActionExecutor.class);

    private final DefaultActionTokenFactory tokenFactory;

    private final EmailTemplateAction emailTemplateAction;

    public EmailActionExecutor(DefaultActionTokenFactory tokenFactory, EmailTemplateAction emailTemplateAction) {
        this.tokenFactory = tokenFactory;
        this.emailTemplateAction = emailTemplateAction;
    }

    public Response execute(KeycloakSession session,
                            String userId,
                            String redirectUri,
                            String clientId,
                            Integer lifespan
    ) {

        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, userId);

        if (user.getEmail() == null) {
            return ErrorResponse.error("User email missing", Response.Status.BAD_REQUEST);
        }

        if (!user.isEnabled()) {
            throw new WebApplicationException(
                    ErrorResponse.error("User is disabled", Response.Status.BAD_REQUEST));
        }

        if (redirectUri != null && clientId == null) {
            throw new WebApplicationException(
                    ErrorResponse.error("Client id missing", Response.Status.BAD_REQUEST));
        }

        if (clientId == null) {
            clientId = Constants.ACCOUNT_MANAGEMENT_CLIENT_ID;
        }

        ClientModel client = realm.getClientByClientId(clientId);
        if (client == null) {
            log.debugf("Client %s doesn't exist", clientId);
            throw new WebApplicationException(
                    ErrorResponse.error("Client doesn't exist", Response.Status.BAD_REQUEST));
        }
        if (!client.isEnabled()) {
            log.debugf("Client %s is not enabled", clientId);
            throw new WebApplicationException(
                    ErrorResponse.error("Client is not enabled", Response.Status.BAD_REQUEST));
        }

        String redirect;
        if (redirectUri != null) {
            redirect = RedirectUtils.verifyRedirectUri(session, redirectUri, client);
            if (redirect == null) {
                throw new WebApplicationException(
                        ErrorResponse.error("Invalid redirect uri.", Response.Status.BAD_REQUEST));
            }
        }

        if (lifespan == null) {
            lifespan = realm.getActionTokenGeneratedByAdminLifespan();
        }
        int expiration = Time.currentTime() + lifespan;

        DefaultActionToken token = tokenFactory.create(user.getId(), user.getEmail(), expiration, redirectUri, clientId);

        try {
            UriBuilder builder = LoginActionsService.actionTokenProcessor(session.getContext().getUri());
            builder.queryParam("key", token.serialize(session, realm, session.getContext().getUri()));

            String link = builder.build(realm.getName()).toString();

            EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class)
                    .setRealm(realm)
                    .setUser(user);
            emailTemplateAction.execute(emailTemplateProvider, link, TimeUnit.SECONDS.toMinutes(lifespan));
            return Response.noContent().build();
        } catch (EmailException e) {
            ServicesLogger.LOGGER.failedToSendActionsEmail(e);
            return ErrorResponse.error("Failed to send execute actions email", Response.Status.INTERNAL_SERVER_ERROR);
        }


    }
}
