package org.taskmanager.task.service

import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.RoleRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.taskmanager.task.model.User
import javax.ws.rs.core.Response


@Service
class KeycloakUserService(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}")
    private val realm: String
) {

    fun findAll(): List<UserRepresentation> =
        keycloak
            .realm(realm)
            .users()
            .list()

    fun findByUsername(username: String): List<UserRepresentation> =
        keycloak
            .realm(realm)
            .users()
            .search(username)

    fun findById(id: String): UserRepresentation =
        keycloak
            .realm(realm)
            .users()
            .get(id)
            .toRepresentation()

    fun assignToGroup(userId: String, groupId: String) {
        keycloak
            .realm(realm)
            .users()
            .get(userId)
            .joinGroup(groupId)
    }

    fun assignRole(userId: String, roleRepresentation: RoleRepresentation) {
        keycloak
            .realm(realm)
            .users()
            .get(userId)
            .roles()
            .realmLevel()
            .add(listOf(roleRepresentation))
    }

    /**
     * Create a new user in the Keycloak realm
     * @param email email of the user to be created
     * @param password password of the user to be created
     * @return the uuid of the user from Keycloak
     */
    fun create(email: String, password: String): String {
        val passwordRepresentation = preparePasswordRepresentation(password)
        val userRepresentation = prepareUserRepresentation(email, passwordRepresentation)
        val response = keycloak
            .realm(realm)
            .users()
            .create(userRepresentation)
        val uuid = response.location.path.replace(".*/([^/]+)$".toRegex(), "$1")
        return uuid
    }

    /**
     * Update an existing user in the Keycloak realm
     * @param user user to be updated
     */
    fun update(uuid: String, email: String?, password: String?) {
        val passwordRepresentation = password?.let { preparePasswordRepresentation(password) }
        val userRepresentation = prepareUserRepresentation(email, passwordRepresentation)
        return keycloak
            .realm(realm)
            .users()
            .get(uuid)
            .update(userRepresentation)
    }


    /**
     * Delete an existing user in the Keycloak realm
     * @param uuid uuid of the user to be deleted
     */
    fun deleteByUuid(uuid: String): Response {
        return keycloak
            .realm(realm)
            .users()
            .delete(uuid)
    }

    private fun preparePasswordRepresentation(password: String): CredentialRepresentation {
        val credentialRepresentation = CredentialRepresentation()
        credentialRepresentation.isTemporary = false
        credentialRepresentation.type = CredentialRepresentation.PASSWORD
        credentialRepresentation.value = password
        return credentialRepresentation
    }

    private fun prepareUserRepresentation(
        email: String?,
        credentialRepresentation: CredentialRepresentation?
    ): UserRepresentation {
        val newUser = UserRepresentation()
        newUser.username = email
        newUser.credentials = credentialRepresentation?.let { listOf(credentialRepresentation) }
        newUser.isEnabled = true
        return newUser
    }

}