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
import org.taskmanager.task.api.dto.UserDto
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
                compareBy(UserDto::firstName, UserDto::lastName, UserDto::email)
            )
            assertThat(userResources).isEqualTo(sortedUsers)
        }
    }

    @Test
    fun `test getByUserId returns user or throws UserNotFoundException`() {
        runBlocking {
            // setup
            val userId = "00000000-0000-0000-0000-000000000001"
            // when
            val existingUserResource = userService.getByUserId(userId)
            // then
            assertThat(existingUserResource).isNotNull()
            assertThat(existingUserResource.userId).isEqualTo(userId)

            // when / then
            assertThatThrownBy {
                runBlocking {
                    userService.getByUserId("fffffff-ffff-ffff-ffff-ffffffffffff")
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
                    userService.getByUserId("00000000-0000-0000-0000-000000000001", -1)
                }
            }.isInstanceOf(UnexpectedUserVersionException::class.java)
        }
    }

}
