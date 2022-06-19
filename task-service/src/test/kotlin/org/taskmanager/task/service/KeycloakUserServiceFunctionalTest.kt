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
import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.api.resource.UserUpdateResource
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
            val userCreateResource = UserCreateResource(
                email = "roger.donald@test.org",
                password = "password",
                firstName = "Roger",
                lastName = "Donald"
            )
            // when: create user
            val uuid = keycloakUserService.create(userCreateResource)
            // then
            assertThat(uuid).isNotBlank
            assertThat(uuid).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")
            // when: find user
            val createdUserRepresentation = keycloakUserService.findById(uuid)
            // then
            assertThat(createdUserRepresentation.username).isEqualTo(userCreateResource.email)
            assertThat(createdUserRepresentation.email).isEqualTo(userCreateResource.email)
            assertThat(createdUserRepresentation.firstName).isEqualTo(userCreateResource.firstName)
            assertThat(createdUserRepresentation.lastName).isEqualTo(userCreateResource.lastName)
            // when: assign to group
            keycloakUserService.assignToGroup(uuid, "task-manager-users")
            // then
            val groupRepresentations = keycloak.realm(realm).users().get(uuid).groups()
            assertThat(groupRepresentations.size).isEqualTo(1)
            // when: update email
            val userUpdateResourceForEmail = UserUpdateResource(email = "roger.donald@test.com")
            keycloakUserService.update(uuid, userUpdateResourceForEmail)
            // then
            val userWithUpdatedEmail = keycloak.realm(realm).users().get(uuid).toRepresentation()
            assertThat(userWithUpdatedEmail.username).isEqualTo("roger.donald@test.com")
            assertThat(userWithUpdatedEmail.email).isEqualTo("roger.donald@test.com")
            assertThat(userWithUpdatedEmail.firstName).isEqualTo("Roger")
            assertThat(userWithUpdatedEmail.lastName).isEqualTo("Donald")
            // when: update password
            val userUpdateResourceForPassword = UserUpdateResource(password = "password2")
            keycloakUserService.update(uuid, userUpdateResourceForPassword)
            // then
            val tokenString = login("roger.donald@test.com", "password2")
            assertThat(tokenString).isNotBlank
            val accessToken = getAccessToken(tokenString)
            assertThat(accessToken).isNotNull
            assertThat(accessToken.subject).isEqualTo(uuid)
            assertThat(accessToken.preferredUsername).isEqualTo("roger.donald@test.com")
            assertThat(accessToken.email).isEqualTo("roger.donald@test.com")
            assertThat(accessToken.givenName).isEqualTo("Roger")
            assertThat(accessToken.familyName).isEqualTo("Donald")
            assertThat(accessToken.realmAccess.roles).contains("ROLE_USER")
            // when: delete user
            keycloakUserService.delete(uuid)
            // then
            Assertions.assertThatThrownBy {
                keycloakUserService.findById(uuid)
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
