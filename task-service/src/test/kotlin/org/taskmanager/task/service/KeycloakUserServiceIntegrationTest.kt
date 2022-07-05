package org.taskmanager.task.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.configuration.keycloak.FakeKeycloakUserStore
import org.taskmanager.task.configuration.keycloak.KeycloakTestConfiguration
import org.taskmanager.task.repository.UserRepository

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
        val userId = "00000000-0000-0000-0000-000000000004"
        // when
        val userRepresentation = keycloakUserService.findById(userId)
        // then
        assertThat(userRepresentation.id).isEqualTo(userId)
    }

    @Test
    fun `test assignToGroup`() {
        // setup
        val userId = "00000000-0000-0000-0000-000000000004"
        val groupName = fakeKeycloakUserStore.group.name
        fakeKeycloakUserStore.groupUsers.remove(userId)
        assertThat(fakeKeycloakUserStore.groupUsers).doesNotContain(userId)
        // when
        keycloakUserService.assignToGroup(userId, groupName)
        // then
        assertThat(fakeKeycloakUserStore.groupUsers).contains(userId)
    }

    @Test
    fun `test create`() {
        // setup
        val email = "michael.wagner@test.org"
        val password = "michael1"
        val firstName = "Michael"
        val lastName = "Wagner"

        // when
        val userId = keycloakUserService.create(email, password, firstName, lastName)
        // then
        assertThat(userId).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")
        val fakeKeycloakUser = fakeKeycloakUserStore.users.values.find { it.username == email }
        assertThat(fakeKeycloakUser).isNotNull
        assertThat(fakeKeycloakUser!!.userId).isEqualTo(userId)
        assertThat(fakeKeycloakUser.username).isEqualTo(email)
        assertThat(fakeKeycloakUser.email).isEqualTo(email)
        assertThat(fakeKeycloakUser.firstName).isEqualTo(firstName)
        assertThat(fakeKeycloakUser.lastName).isEqualTo(lastName)
        assertThat(fakeKeycloakUser.password).isEqualTo(password)
        assertThat(fakeKeycloakUser.isEnabled).isTrue()
        assertThat(fakeKeycloakUserStore.groupUsers).contains(fakeKeycloakUser.userId)
    }

    @Test
    fun `test update`() {
        // setup
        val userId = "00000000-0000-0000-0000-000000000003"
        val email = "benito.meier@test.com"
        val lastName = "Meier"

        val fakeKeycloakUser = fakeKeycloakUserStore.users[userId]
        assertThat(fakeKeycloakUser).isNotNull
        val originalFirstName = fakeKeycloakUser!!.firstName
        val originalPassword = fakeKeycloakUser.password
        // when
        keycloakUserService.update(userId, email, null, null, lastName)
        // then
        assertThat(fakeKeycloakUser.userId).isEqualTo(userId)
        assertThat(fakeKeycloakUser.username).isEqualTo(email)
        assertThat(fakeKeycloakUser.email).isEqualTo(email)
        assertThat(fakeKeycloakUser.firstName).isEqualTo(originalFirstName)
        assertThat(fakeKeycloakUser.lastName).isEqualTo(lastName)
        assertThat(fakeKeycloakUser.password).isEqualTo(originalPassword)
        assertThat(fakeKeycloakUser.isEnabled).isTrue()
    }

    @Test
    fun `test delete`() {
        // setup
        val userId = "00000000-0000-0000-0000-000000000002"
        assertThat(fakeKeycloakUserStore.users).containsKey(userId)
        // when
        keycloakUserService.delete(userId)
        // then
        assertThat(fakeKeycloakUserStore.users).doesNotContainKey(userId)
    }
}
