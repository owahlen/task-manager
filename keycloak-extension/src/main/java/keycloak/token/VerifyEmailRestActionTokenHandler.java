package keycloak.token;

import org.keycloak.authentication.actiontoken.AbstractActionTokenHandler;
import org.keycloak.TokenVerifier.Predicate;
import org.keycloak.authentication.actiontoken.*;
import org.keycloak.events.*;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserModel.RequiredAction;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import java.util.Objects;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public class VerifyEmailRestActionTokenHandler extends AbstractActionTokenHandler<VerifyEmailRestActionToken>  {

    public VerifyEmailRestActionTokenHandler() {
        super(
                VerifyEmailRestActionToken.TOKEN_TYPE,
                VerifyEmailRestActionToken.class,
                Messages.STALE_VERIFY_EMAIL_LINK,
                EventType.VERIFY_EMAIL,
                Errors.INVALID_TOKEN
        );
    }

    @Override
    public Predicate<? super VerifyEmailRestActionToken>[] getVerifiers(ActionTokenContext<VerifyEmailRestActionToken> tokenContext) {
        return TokenUtils.predicates(
                TokenUtils.checkThat(
                        t -> Objects.equals(t.getEmail(), tokenContext.getAuthenticationSession().getAuthenticatedUser().getEmail()),
                        Errors.INVALID_EMAIL, getDefaultErrorMessage()
                )
        );
    }

    @Override
    public Response handleToken(VerifyEmailRestActionToken token, ActionTokenContext<VerifyEmailRestActionToken> tokenContext) {
        UserModel user = tokenContext.getAuthenticationSession().getAuthenticatedUser();
        EventBuilder event = tokenContext.getEvent();

        event.event(EventType.VERIFY_EMAIL).detail(Details.EMAIL, user.getEmail());

        AuthenticationSessionModel authSession = tokenContext.getAuthenticationSession();
        final UriInfo uriInfo = tokenContext.getUriInfo();
        final RealmModel realm = tokenContext.getRealm();
        final KeycloakSession session = tokenContext.getSession();

        if (tokenContext.isAuthenticationSessionFresh()) {
            // Update the authentication session in the token
            String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authSession).getEncodedId();
            token.setCompoundAuthenticationSessionId(authSessionEncodedId);
            UriBuilder builder = Urls.actionTokenBuilder(uriInfo.getBaseUri(), token.serialize(session, realm, uriInfo),
                    authSession.getClient().getClientId(), authSession.getTabId());
            String confirmUri = builder.build(realm.getName()).toString();

            return session.getProvider(LoginFormsProvider.class)
                    .setAuthenticationSession(authSession)
                    .setSuccess(Messages.CONFIRM_EMAIL_ADDRESS_VERIFICATION, user.getEmail())
                    .setAttribute(Constants.TEMPLATE_ATTR_ACTION_URI, confirmUri)
                    .createInfoPage();
        }

        // verify user email as we know it is valid as this entry point would never have gotten here.
        user.setEmailVerified(true);
        user.removeRequiredAction(RequiredAction.VERIFY_EMAIL);
        authSession.removeRequiredAction(RequiredAction.VERIFY_EMAIL);

        event.success();

        if (token.getCompoundAuthenticationSessionId() != null) {
            AuthenticationSessionManager asm = new AuthenticationSessionManager(tokenContext.getSession());
            asm.removeAuthenticationSession(tokenContext.getRealm(), authSession, true);

            return tokenContext.getSession().getProvider(LoginFormsProvider.class)
                    .setAuthenticationSession(authSession)
                    .setSuccess(Messages.EMAIL_VERIFIED)
                    .createInfoPage();
        }

        tokenContext.setEvent(event.clone().removeDetail(Details.EMAIL).event(EventType.LOGIN));

        String nextAction = AuthenticationManager.nextRequiredAction(session, authSession, tokenContext.getRequest(), event);
        return AuthenticationManager.redirectToRequiredActions(session, realm, authSession, uriInfo, nextAction);
    }

}
