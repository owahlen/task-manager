package keycloak.api;

import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;

public interface EmailTemplateAction {
    void execute(EmailTemplateProvider emailTemplateProvider, String link, long expirationInMinutes) throws EmailException;
}
