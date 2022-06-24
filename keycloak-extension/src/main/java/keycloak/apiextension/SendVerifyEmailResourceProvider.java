package keycloak.apiextension;

import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.ErrorResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

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
        List<String> actions = Collections.singletonList(UserModel.RequiredAction.VERIFY_EMAIL.name());
        EmailActionExecutor emailActionExecutor = new EmailActionExecutor(EmailTemplateProvider::sendVerifyEmail);
        return emailActionExecutor.execute(session, userId, redirectUri, clientId, null, actions);
    }

}