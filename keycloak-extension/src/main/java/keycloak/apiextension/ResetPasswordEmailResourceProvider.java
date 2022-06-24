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

public class ResetPasswordEmailResourceProvider extends AbstractEmailResourceProvider {

    public ResetPasswordEmailResourceProvider(KeycloakSession session) {
        super(session);
    }

    @PUT
    @Path("users/{userId}")
    @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public Response resetPasswordEmail(
            @PathParam("userId") String userId,
            @QueryParam(OIDCLoginProtocol.REDIRECT_URI_PARAM) String redirectUri,
            @QueryParam(OIDCLoginProtocol.CLIENT_ID_PARAM) String clientId
    ) {
        if (userId == null) {
            return ErrorResponse.error("User id missing", Response.Status.BAD_REQUEST);
        }
        List<String> actions = Collections.singletonList(UserModel.RequiredAction.UPDATE_PASSWORD.name());
        EmailActionExecutor emailActionExecutor = new EmailActionExecutor(EmailTemplateProvider::sendPasswordReset);
        return emailActionExecutor.execute(session, userId, redirectUri, clientId, null, actions);
    }


}