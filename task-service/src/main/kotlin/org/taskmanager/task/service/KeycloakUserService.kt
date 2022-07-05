package org.taskmanager.task.service

import org.keycloak.admin.client.CreatedResponseUtil
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.taskmanager.task.exception.GroupNotFoundException
import javax.ws.rs.core.Response


@Service
class KeycloakUserService(
    private val keycloak: Keycloak,
    @Value("\${keycloak.realm}")
    private val realm: String,
    @Value("\${keycloak.group-name}")
    private val groupName: String
) {
    private val log = LoggerFactory.getLogger(KeycloakUserService::class.java)

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

    fun assignToGroup(userId: String, groupName: String) {
        val realmResource = keycloak.realm(realm)
        val groupRepresentations = realmResource.groups().groups(groupName, 0, 100)
        if (groupRepresentations.size != 1) throw GroupNotFoundException(groupName)
        val groupRepresentation = groupRepresentations.first()
        val groupId = groupRepresentation.id
        realmResource.users().get(userId).joinGroup(groupId)
        log.debug("Keycloak user '${userId}' joined group '${groupName}' with id '${groupId}'")
    }

    /**
     * Create a new user in the Keycloak realm
     * @param email email of the user to be created
     * @param password password of the user to be created
     * @return the userId of the user from Keycloak
     */
    fun create(email: String, password: String, firstName: String?, lastName: String?): String {
        val passwordRepresentation = prepareCredentialRepresentation(password)
        val userRepresentation = prepareUserRepresentation(
            email,
            firstName,
            lastName,
            passwordRepresentation
        )
        val response = keycloak
            .realm(realm)
            .users()
            .create(userRepresentation)
        val userId = CreatedResponseUtil.getCreatedId(response)
        assignToGroup(userId, groupName)
        log.debug("Keycloak user '${email}' created with userId '${userId}'")
        return userId
    }

    /**
     * Update an existing user in the Keycloak realm
     * Only non-null parameters are respected
     * @param user user to be updated
     */
    fun update(userId: String, email: String?, password: String?, firstName: String?, lastName: String?) {
        updateUser(
            userId = userId,
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName
        )
    }

    /**
     * Delete an existing user in the Keycloak realm
     * @param userId userId of the user to be deleted
     */
    fun delete(userId: String): Response {
        return keycloak
            .realm(realm)
            .users()
            .delete(userId)
    }

    private fun prepareCredentialRepresentation(password: String): CredentialRepresentation {
        val credentialRepresentation = CredentialRepresentation()
        credentialRepresentation.isTemporary = false
        credentialRepresentation.type = CredentialRepresentation.PASSWORD
        credentialRepresentation.value = password
        return credentialRepresentation
    }

    private fun prepareUserRepresentation(
        email: String?,
        firstName: String?,
        lastName: String?,
        credentialRepresentation: CredentialRepresentation?
    ): UserRepresentation {
        val newUser = UserRepresentation()
        newUser.username = email
        newUser.email = email
        newUser.firstName = firstName
        newUser.lastName = lastName
        newUser.credentials = credentialRepresentation?.let { listOf(credentialRepresentation) }
        newUser.isEnabled = true
        return newUser
    }

    private fun updateUser(userId: String, email: String?, password: String?, firstName: String?, lastName: String?) {
        val userRepresentation = prepareUserRepresentation(
            email = email,
            firstName = firstName,
            lastName = lastName,
            credentialRepresentation = password?.let(::prepareCredentialRepresentation)
        )
        keycloak
            .realm(realm)
            .users()
            .get(userId)
            .update(userRepresentation)
    }

}