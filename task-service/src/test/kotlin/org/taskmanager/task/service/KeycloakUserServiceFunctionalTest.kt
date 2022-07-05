package org.taskmanager.task.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.keycloak.OAuth2Constants.PASSWORD
import org.keycloak.TokenVerifier
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.AccessToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.taskmanager.task.IntegrationTest
import javax.ws.rs.NotFoundException

/**
 * This test requires a working Keycloak instance
 */
@Disabled
@IntegrationTest
class KeycloakUserServiceFunctionalTest(
    @Value("\${keycloak.realm}") private val realm: String,
    @Value("\${keycloak.auth-server-url}") private val authUrl: String,
    @Autowired private val keycloak: Keycloak,
    @Autowired private val keycloakUserService: KeycloakUserService
) {

    @Test
    fun `test create, find, assign to group, update, login, delete of user`() {
        runBlocking {
            // setup
            val email = "roger.donald@test.org"
            val password = "password"
            val firstName = "Roger"
            val lastName = "Donald"

            // when: create user
            val userId = keycloakUserService.create(email, password, firstName, lastName)
            // then
            assertThat(userId).isNotBlank
            assertThat(userId).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")
            // when: find user
            val createdUserRepresentation = keycloakUserService.findById(userId)
            // then
            assertThat(createdUserRepresentation.username).isEqualTo(email)
            assertThat(createdUserRepresentation.email).isEqualTo(email)
            assertThat(createdUserRepresentation.firstName).isEqualTo(firstName)
            assertThat(createdUserRepresentation.lastName).isEqualTo(lastName)
            // when: assign to group
            keycloakUserService.assignToGroup(userId, "task-manager-users")
            // then
            val groupRepresentations = keycloak.realm(realm).users().get(userId).groups()
            assertThat(groupRepresentations.size).isEqualTo(1)
            // when: update email
            val updatedEmail = "roger.donald@test.com"
            keycloakUserService.update(userId, updatedEmail, null, null, null)
            // then
            val userWithUpdatedEmail = keycloak.realm(realm).users().get(userId).toRepresentation()
            assertThat(userWithUpdatedEmail.username).isEqualTo("roger.donald@test.com")
            assertThat(userWithUpdatedEmail.email).isEqualTo("roger.donald@test.com")
            assertThat(userWithUpdatedEmail.firstName).isEqualTo("Roger")
            assertThat(userWithUpdatedEmail.lastName).isEqualTo("Donald")
            // when: update password
            val updatedPassword = "password2"
            keycloakUserService.update(userId, null, updatedPassword, null, null)
            // then
            val tokenString = login("roger.donald@test.com", "password2")
            assertThat(tokenString).isNotBlank
            val accessToken = getAccessToken(tokenString)
            assertThat(accessToken).isNotNull
            assertThat(accessToken.subject).isEqualTo(userId)
            assertThat(accessToken.preferredUsername).isEqualTo("roger.donald@test.com")
            assertThat(accessToken.email).isEqualTo("roger.donald@test.com")
            assertThat(accessToken.givenName).isEqualTo("Roger")
            assertThat(accessToken.familyName).isEqualTo("Donald")
            assertThat(accessToken.realmAccess.roles).contains("ROLE_USER")
            // when: delete user
            keycloakUserService.delete(userId)
            // then
            Assertions.assertThatThrownBy {
                keycloakUserService.findById(userId)
            }.isInstanceOf(NotFoundException::class.java)

        }
    }

    private fun login(username: String, password: String): String {
        val loginKeycloak = KeycloakBuilder.builder()
            .grantType(PASSWORD)
            .serverUrl(authUrl)
            .realm(realm)
            .username(username)
            .password(password)
            .clientId("task-browser")
            .build()
        return loginKeycloak.tokenManager().accessToken.token
    }

    private fun getAccessToken(tokenString: String): AccessToken {
        return TokenVerifier.create(tokenString, AccessToken::class.java).token
    }

}
