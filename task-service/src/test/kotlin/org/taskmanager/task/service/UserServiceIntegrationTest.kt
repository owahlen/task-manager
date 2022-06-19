package org.taskmanager.task.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.api.resource.UserResource
import org.taskmanager.task.api.resource.UserUpdateResource
import org.taskmanager.task.configuration.keycloak.FakeKeycloakUserStore
import org.taskmanager.task.configuration.keycloak.KeycloakTestConfiguration
import org.taskmanager.task.exception.UnexpectedUserVersionException
import org.taskmanager.task.exception.UserNotFoundException
import org.taskmanager.task.repository.UserRepository


@ContextConfiguration(classes = [KeycloakTestConfiguration::class])
@IntegrationTest
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceIntegrationTest(
    @Autowired val userService: UserService,
    @Autowired val userRepository: UserRepository,
    @Autowired val fakeKeycloakUserStore: FakeKeycloakUserStore
) {
    @BeforeAll
    fun beforeAll() {
        fakeKeycloakUserStore.initializeFakeKeycloakUserStore(userRepository)
    }

    @Test
    fun `test findAllBy pageable returns page of users`() {
        runBlocking {
            // setup
            val sort = Sort.by(Order.by("firstName"), Order.by("lastName"), Order.by("email"))
            val pageable = PageRequest.of(0, 100, sort)
            // when
            val userResources = userService.findAllBy(pageable).toList()
            // then
            assertThat(userResources.count()).isGreaterThan(2)
            val sortedUsers = userResources.sortedWith(
                compareBy(UserResource::firstName, UserResource::lastName, UserResource::email)
            )
            assertThat(userResources).isEqualTo(sortedUsers)
        }
    }

    @Test
    fun `test getByUuid returns user or throws UserNotFoundException`() {
        runBlocking {
            // setup
            val uuid = "00000000-0000-0000-0000-000000000001"
            // when
            val existingUserResource = userService.getByUuid(uuid)
            // then
            assertThat(existingUserResource).isNotNull()
            assertThat(existingUserResource.uuid).isEqualTo(uuid)

            // when / then
            assertThatThrownBy {
                runBlocking {
                    userService.getByUuid("fffffff-ffff-ffff-ffff-ffffffffffff")
                }
            }.isInstanceOf(UserNotFoundException::class.java)
        }
    }

    @Test
    fun `test getById with wrong version throws UnexpectedUserVersionException`() {
        runBlocking {
            // when / then
            assertThatThrownBy {
                runBlocking {
                    userService.getByUuid("00000000-0000-0000-0000-000000000001", -1)
                }
            }.isInstanceOf(UnexpectedUserVersionException::class.java)
        }
    }

    @Test
    fun `test create user`() {
        runBlocking {
            // setup
            val userCreateResource = UserCreateResource(
                "john.walker@test.org",
                "password",
                "John",
                "Walker"
            )
            // when
            val savedUser = userService.create(userCreateResource)
            // then
            assertThat(savedUser).isNotNull
            assertThat(savedUser.uuid).isNotNull
            assertThat(savedUser.version).isNotNull
            assertThat(savedUser.email).isEqualTo(userCreateResource.email)
            assertThat(savedUser.firstName).isEqualTo(userCreateResource.firstName)
            assertThat(savedUser.lastName).isEqualTo(userCreateResource.lastName)
            assertThat(savedUser.createdDate).isNotNull
            assertThat(savedUser.lastModifiedDate).isNotNull
            val keycloakUser = fakeKeycloakUserStore.users[savedUser.uuid]
            assertThat(keycloakUser).isNotNull
            assertThat(keycloakUser!!.uuid).isEqualTo(savedUser.uuid)
            assertThat(keycloakUser.username).isEqualTo(userCreateResource.email)
            assertThat(keycloakUser.email).isEqualTo(userCreateResource.email)
            assertThat(keycloakUser.password).isEqualTo(userCreateResource.password)
            assertThat(keycloakUser.firstName).isEqualTo(userCreateResource.firstName)
            assertThat(keycloakUser.lastName).isEqualTo(userCreateResource.lastName)
        }
    }

    @Test
    fun `test update user without password change`() {
        runBlocking {
            // setup
            val uuid = "00000000-0000-0000-0000-000000000002"
            val userUpdateResource = UserUpdateResource(
                "john.walker@test.org",
                null,
                "John",
                "Walker"
            )
            val keycloakUser = fakeKeycloakUserStore.users[uuid]
            assertThat(keycloakUser).isNotNull
            val oldPassword = keycloakUser!!.password
            // when
            val updatedUser = userService.update(uuid, null, userUpdateResource)
            // then
            assertThat(updatedUser).isNotNull
            assertThat(updatedUser.uuid).isEqualTo(uuid)
            assertThat(updatedUser.version).isNotNull
            assertThat(updatedUser.email).isEqualTo(userUpdateResource.email)
            assertThat(updatedUser.firstName).isEqualTo(userUpdateResource.firstName)
            assertThat(updatedUser.lastName).isEqualTo(userUpdateResource.lastName)
            assertThat(updatedUser.createdDate).isNotNull
            assertThat(updatedUser.lastModifiedDate).isNotNull
            assertThat(keycloakUser.uuid).isEqualTo(uuid)
            assertThat(keycloakUser.username).isEqualTo(userUpdateResource.email)
            assertThat(keycloakUser.email).isEqualTo(userUpdateResource.email)
            assertThat(keycloakUser.password).isEqualTo(oldPassword)
            assertThat(keycloakUser.firstName).isEqualTo(userUpdateResource.firstName)
            assertThat(keycloakUser.lastName).isEqualTo(userUpdateResource.lastName)
        }
    }

    @Test
    fun `test update user with password change`() {
        runBlocking {
            // setup
            val uuid = "00000000-0000-0000-0000-000000000002"
            val userUpdateResource = UserUpdateResource(
                "till.harper@test.org",
                "new_password",
                "Till",
                "Harper"
            )
            val keycloakUser = fakeKeycloakUserStore.users[uuid]
            assertThat(keycloakUser).isNotNull
            // when
            val updatedUser = userService.update(uuid, null, userUpdateResource)
            // then
            assertThat(updatedUser).isNotNull
            assertThat(updatedUser.uuid).isEqualTo(uuid)
            assertThat(updatedUser.version).isNotNull
            assertThat(updatedUser.email).isEqualTo(userUpdateResource.email)
            // todo: test that the password is changed
            assertThat(updatedUser.firstName).isEqualTo(userUpdateResource.firstName)
            assertThat(updatedUser.lastName).isEqualTo(userUpdateResource.lastName)
            assertThat(updatedUser.createdDate).isNotNull
            assertThat(updatedUser.lastModifiedDate).isNotNull
            assertThat(keycloakUser!!.uuid).isEqualTo(uuid)
            assertThat(keycloakUser.username).isEqualTo(userUpdateResource.email)
            assertThat(keycloakUser.email).isEqualTo(userUpdateResource.email)
            assertThat(keycloakUser.password).isEqualTo(userUpdateResource.password)
            assertThat(keycloakUser.firstName).isEqualTo(userUpdateResource.firstName)
            assertThat(keycloakUser.lastName).isEqualTo(userUpdateResource.lastName)
        }
    }

    @Test
    fun `test delete user`() {
        runBlocking {
            // setup
            val uuid = "00000000-0000-0000-0000-000000000003"
            val userResource = userService.getByUuid(uuid)
            assertThat(userResource).isNotNull
            val keycloakUser = fakeKeycloakUserStore.users[uuid]
            assertThat(keycloakUser).isNotNull
            // when
            userService.delete(uuid)
            // then
            assertThatThrownBy {
                runBlocking {
                    userService.getByUuid(uuid)
                }
            }.isInstanceOf(UserNotFoundException::class.java)
        }
    }

}
