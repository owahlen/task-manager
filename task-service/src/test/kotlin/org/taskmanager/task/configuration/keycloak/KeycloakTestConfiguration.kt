package org.taskmanager.task.configuration.keycloak

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.GroupsResource
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.admin.client.resource.UsersResource
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.taskmanager.task.exception.UserNotFoundException
import java.net.URI
import java.util.UUID.randomUUID
import javax.ws.rs.core.Response


@TestConfiguration
class KeycloakTestConfiguration(
    @Value("\${keycloak.credentials.secret}")
    private val secretKey: String,
    @Value("\${keycloak.resource}")
    private val clientId: String,
    @Value("\${keycloak.auth-server-url}")
    private val authUrl: String,
    @Value("\${keycloak.realm}")
    private val realm: String,
    @Value("\${keycloak.group-name}")
    private val groupName: String
) {

    @Bean
    fun fakeKeycloakUserStore(): FakeKeycloakUserStore {
        return FakeKeycloakUserStore(groupName)
    }

    /**
     * Fake Keycloak client with underlying fakeKeycloakUserStore
     */
    @Bean
    fun keycloak(): Keycloak {
        val userStore = fakeKeycloakUserStore()
        val keycloak: Keycloak = mockk()
        // realm
        val realmResource: RealmResource = mockk()
        every { keycloak.realm(realm) } returns realmResource
        // realm.users
        val usersResource: UsersResource = mockk()
        every { realmResource.users() } returns usersResource
        // realm.users.create
        val userRepresentation = slot<UserRepresentation>()
        every { usersResource.create(capture(userRepresentation)) } answers {
            val capturedUserRepresentation = userRepresentation.captured
            val userId = randomUUID().toString()
            userStore.users[userId] = FakeKeycloakUser(
                userId = userId,
                username = capturedUserRepresentation.username,
                email = capturedUserRepresentation.email,
                firstName = capturedUserRepresentation.firstName,
                lastName = capturedUserRepresentation.lastName,
                password = capturedUserRepresentation.credentials.first().value,
                isEnabled = capturedUserRepresentation.isEnabled
            )
            val response: Response = mockk()
            // response
            every { response.location } returns URI("http://localhost/${userId}")
            every { response.statusInfo } returns Response.Status.CREATED
            response
        }
        // realm.users.get
        val userId = slot<String>()
        every { usersResource.get(capture(userId)) } answers {
            val capturedUserId = userId.captured
            val userResource: UserResource = mockk()
            every { userResource.joinGroup(any()) } answers {
                fakeKeycloakUserStore().groupUsers.add(capturedUserId)
            }
            // realm.users.get.update
            every { userResource.update(capture(userRepresentation)) } answers {
                val user = fakeKeycloakUserStore().users[capturedUserId] ?: throw UserNotFoundException(capturedUserId)
                val capturedUserRepresentation = userRepresentation.captured
                capturedUserRepresentation.username?.also { user.username = it }
                capturedUserRepresentation.email?.also { user.email = it }
                capturedUserRepresentation.firstName?.also { user.firstName = it }
                capturedUserRepresentation.lastName?.also { user.lastName = it }
                capturedUserRepresentation.credentials?.first()?.value?.also { user.password = it }
                capturedUserRepresentation.isEnabled?.also { user.isEnabled = it }
            }
            // realm.users.get.toRepresentation
            every { userResource.toRepresentation() } answers {
                fakeKeycloakUserStore().users[capturedUserId]?.toUserRepresentation()
                    ?: throw UserNotFoundException(capturedUserId)
            }
            userResource
        }
        // realm.users.list
        every { usersResource.list() } answers {
            fakeKeycloakUserStore().users.values
                .map { it.toUserRepresentation() }
        }
        // realm.users.search
        val username = slot<String>()
        every { usersResource.search(capture(username)) } answers {
            val capturedUsername = username.captured
            fakeKeycloakUserStore().users.values
                .filter { it.username == capturedUsername }
                .map { it.toUserRepresentation() }
        }
        // realm.users.delete
        every { usersResource.delete(capture(userId)) } answers {
            val capturedUserId = userId.captured
            fakeKeycloakUserStore().users.remove(capturedUserId)
            fakeKeycloakUserStore().groupUsers.remove(capturedUserId)
            val response: Response = mockk()
            response
        }

        // realm.groups
        val groupsResource: GroupsResource = mockk()
        every { realmResource.groups() } returns groupsResource
        every { groupsResource.groups(groupName, 0, any()) } returns listOf(fakeKeycloakUserStore().group.toGroupRepresentation())

        return keycloak
    }
}
