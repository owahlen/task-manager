package org.taskmanager.task.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.api.resource.UserPatchResource
import org.taskmanager.task.api.resource.UserUpdateResource
import org.taskmanager.task.configuration.keycloak.FakeKeycloakUserStore
import org.taskmanager.task.configuration.keycloak.KeycloakTestConfiguration
import org.taskmanager.task.repository.UserRepository
import java.util.*

@ContextConfiguration(classes = [KeycloakTestConfiguration::class])
@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeycloakUserServiceIntegrationTest(
    @Autowired val keycloakUserService: KeycloakUserService,
    @Autowired val userRepository: UserRepository,
    @Autowired val fakeKeycloakUserStore: FakeKeycloakUserStore
) {

    @BeforeAll
    fun beforeAll() {
        fakeKeycloakUserStore.initializeFakeKeycloakUserStore(userRepository)
    }

    @Test
    fun `test findAll`() {
        // when
        val userRepresentations = keycloakUserService.findAll()
        // then
        assertThat(userRepresentations).isNotEmpty
    }

    @Test
    fun `test findByUsername`() {
        // setup
        val username = "vince.power@test.org"
        // when
        val userRepresentations = keycloakUserService.findByUsername(username)
        // then
        assertThat(userRepresentations).hasSize(1)
    }

    @Test
    fun `test findById`() {
        // setup
        val uuid = "00000000-0000-0000-0000-000000000004"
        // when
        val userRepresentation = keycloakUserService.findById(uuid)
        // then
        assertThat(userRepresentation.id).isEqualTo(uuid)
    }

    @Test
    fun `test assignToGroup`() {
        // setup
        val uuid = "00000000-0000-0000-0000-000000000004"
        val groupName = fakeKeycloakUserStore.group.name
        fakeKeycloakUserStore.groupUsers.remove(uuid)
        assertThat(fakeKeycloakUserStore.groupUsers).doesNotContain(uuid)
        // when
        keycloakUserService.assignToGroup(uuid, groupName)
        // then
        assertThat(fakeKeycloakUserStore.groupUsers).contains(uuid)
    }

    @Test
    fun `test create`() {
        // setup
        val userCreateResource = UserCreateResource(
            email = "michael.wagner@test.org",
            password = "michael1",
            firstName = "Michael",
            lastName = "Wagner"
        )
        // when
        val uuid = keycloakUserService.create(userCreateResource)
        // then
        assertThat(uuid).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")
        val fakeKeycloakUser = fakeKeycloakUserStore.users.values.find { it.username == userCreateResource.email }
        assertThat(fakeKeycloakUser).isNotNull
        assertThat(fakeKeycloakUser!!.uuid).isEqualTo(uuid)
        assertThat(fakeKeycloakUser.username).isEqualTo(userCreateResource.email)
        assertThat(fakeKeycloakUser.email).isEqualTo(userCreateResource.email)
        assertThat(fakeKeycloakUser.firstName).isEqualTo(userCreateResource.firstName)
        assertThat(fakeKeycloakUser.lastName).isEqualTo(userCreateResource.lastName)
        assertThat(fakeKeycloakUser.password).isEqualTo(userCreateResource.password)
        assertThat(fakeKeycloakUser.isEnabled).isTrue()
        assertThat(fakeKeycloakUserStore.groupUsers).contains(fakeKeycloakUser.uuid)
    }

    @Test
    fun `test update`() {
        // setup
        val uuid = "00000000-0000-0000-0000-000000000003"
        val userUpdateResource = UserUpdateResource(
            email = "benito.meier@test.com",
            lastName = "Meier"
        )
        val fakeKeycloakUser = fakeKeycloakUserStore.users[uuid]
        assertThat(fakeKeycloakUser).isNotNull
        val originalFirstName = fakeKeycloakUser!!.firstName
        val originalPassword = fakeKeycloakUser.password
        // when
        keycloakUserService.update(uuid, userUpdateResource)
        // then
        assertThat(fakeKeycloakUser.uuid).isEqualTo(uuid)
        assertThat(fakeKeycloakUser.username).isEqualTo(userUpdateResource.email)
        assertThat(fakeKeycloakUser.email).isEqualTo(userUpdateResource.email)
        assertThat(fakeKeycloakUser.firstName).isEqualTo(originalFirstName)
        assertThat(fakeKeycloakUser.lastName).isEqualTo(userUpdateResource.lastName)
        assertThat(fakeKeycloakUser.password).isEqualTo(originalPassword)
        assertThat(fakeKeycloakUser.isEnabled).isTrue()
    }

    @Test
    fun `test patch`() {
        // setup
        val uuid = "00000000-0000-0000-0000-000000000003"
        val userPatchResource = UserPatchResource(
            email = Optional.empty(),
            password = Optional.of("meier2"),
            firstName = Optional.empty(),
            lastName = Optional.empty()
        )
        val fakeKeycloakUser = fakeKeycloakUserStore.users[uuid]
        assertThat(fakeKeycloakUser).isNotNull
        val originalUser = fakeKeycloakUser!!.copy()
        // when
        keycloakUserService.patch(uuid, userPatchResource)
        // then
        assertThat(fakeKeycloakUser.uuid).isEqualTo(uuid)
        assertThat(fakeKeycloakUser.username).isEqualTo(originalUser.email)
        assertThat(fakeKeycloakUser.email).isEqualTo(originalUser.email)
        assertThat(fakeKeycloakUser.firstName).isEqualTo(originalUser.firstName)
        assertThat(fakeKeycloakUser.lastName).isEqualTo(originalUser.lastName)
        assertThat(fakeKeycloakUser.password).isEqualTo(userPatchResource.password.get())
        assertThat(fakeKeycloakUser.isEnabled).isTrue()
    }

    @Test
    fun `test delete`() {
        // setup
        val uuid = "00000000-0000-0000-0000-000000000002"
        assertThat(fakeKeycloakUserStore.users).containsKey(uuid)
        // when
        keycloakUserService.delete(uuid)
        // then
        assertThat(fakeKeycloakUserStore.users).doesNotContainKey(uuid)
    }
}
