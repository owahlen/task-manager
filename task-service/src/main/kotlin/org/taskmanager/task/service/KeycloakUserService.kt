package org.taskmanager.task.service

import org.keycloak.admin.client.CreatedResponseUtil
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.api.resource.UserPatchResource
import org.taskmanager.task.api.resource.UserUpdateResource
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
     * @return the uuid of the user from Keycloak
     */
    fun create(userCreateResource: UserCreateResource): String {
        val email = userCreateResource.email ?: throw IllegalArgumentException(
            "When creating a Keycloak user an email must be provided"
        )
        val password = userCreateResource.password ?: throw IllegalArgumentException(
            "When creating a Keycloak user a password must be provided"
        )
        val passwordRepresentation = prepareCredentialRepresentation(password)
        val userRepresentation = prepareUserRepresentation(
            email,
            userCreateResource.firstName,
            userCreateResource.lastName,
            passwordRepresentation
        )
        val response = keycloak
            .realm(realm)
            .users()
            .create(userRepresentation)
        val uuid = CreatedResponseUtil.getCreatedId(response)
        assignToGroup(uuid, groupName)
        log.debug("Keycloak user '${email}' created with uuid '${uuid}'")
        return uuid
    }

    /**
     * Update an existing user in the Keycloak realm
     * @param user user to be updated
     */
    fun update(uuid: String, userUpdateResource: UserUpdateResource) {
        updateUser(
            uuid = uuid,
            email = userUpdateResource.email,
            password = userUpdateResource.password,
            firstName = userUpdateResource.firstName,
            lastName = userUpdateResource.lastName
        )
    }

    fun patch(uuid: String, userPatchResource: UserPatchResource) {
        updateUser(
            uuid = uuid,
            email = userPatchResource.email.orElse(null),
            password = userPatchResource.password.orElse(null),
            firstName = userPatchResource.firstName.orElse(null),
            lastName = userPatchResource.lastName.orElse(null)
        )
    }

    /**
     * Delete an existing user in the Keycloak realm
     * @param uuid uuid of the user to be deleted
     */
    fun delete(uuid: String): Response {
        return keycloak
            .realm(realm)
            .users()
            .delete(uuid)
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

    private fun updateUser(uuid: String, email: String?, password: String?, firstName: String?, lastName: String?) {
        val userRepresentation = prepareUserRepresentation(
            email = email,
            firstName = firstName,
            lastName = lastName,
            credentialRepresentation = password?.let(::prepareCredentialRepresentation)
        )
        keycloak
            .realm(realm)
            .users()
            .get(uuid)
            .update(userRepresentation)
    }

}