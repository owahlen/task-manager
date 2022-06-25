package keycloak.api;

import keycloak.token.VerifyEmailRestActionToken;
import org.keycloak.authentication.actiontoken.DefaultActionToken;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.ErrorResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

public class SendVerifyEmailResourceProvider extends AbstractEmailResourceProvider {

    public SendVerifyEmailResourceProvider(KeycloakSession session) {
        super(session);
    }

    @PUT
    @Path("users/{userId}")
    @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public Response sendVerifyEmail(
            @PathParam("userId") String userId,
            @QueryParam(OIDCLoginProtocol.REDIRECT_URI_PARAM) String redirectUri,
            @QueryParam(OIDCLoginProtocol.CLIENT_ID_PARAM) String clientId
    ) {
        if (userId == null) {
            return ErrorResponse.error("User id missing", Response.Status.BAD_REQUEST);
        }
        EmailActionExecutor emailActionExecutor = new EmailActionExecutor(this::createToken, EmailTemplateProvider::sendVerifyEmail);
        return emailActionExecutor.execute(session, userId, redirectUri, clientId, null);
    }

    public DefaultActionToken createToken(String userId, String email, int absoluteExpirationInSecs, String redirectUri, String clientId) {
        return new VerifyEmailRestActionToken(userId, absoluteExpirationInSecs, email, redirectUri, clientId);
    }

}