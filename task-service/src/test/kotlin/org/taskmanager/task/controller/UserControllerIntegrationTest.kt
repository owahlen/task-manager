package org.taskmanager.task.controller

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.api.dto.UserDto
import org.taskmanager.task.configuration.keycloak.FakeKeycloakUserStore
import org.taskmanager.task.configuration.keycloak.KeycloakTestConfiguration
import org.taskmanager.task.repository.UserRepository
import org.taskmanager.task.service.UserService


@AutoConfigureWebTestClient
@ContextConfiguration(classes = [KeycloakTestConfiguration::class])
@IntegrationTest
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val userService: UserService,
    @Autowired val userRepository: UserRepository,
    @Autowired val fakeKeycloakUserStore: FakeKeycloakUserStore
) {

    private val DEFAULT_PAGEABLE =
        PageRequest.of(0, 100, Sort.by(Order.by("firstName"), Order.by("lastName"), Order.by("email")))
    private val SUBJECT = "test_user";
    private val ADMIN_AUTHORITY = SimpleGrantedAuthority("ROLE_ADMIN");

    @BeforeAll
    fun beforeAll() {
        fakeKeycloakUserStore.initializeFakeKeycloakUserStore(userRepository)
    }

    @Test
    fun `test get user page`() {
        runBlocking {
            // setup
            val expectedUserResources = userService.findAllBy(DEFAULT_PAGEABLE).toList()
            assertThat(expectedUserResources.count()).isGreaterThan(0)
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(ADMIN_AUTHORITY))
                .get()
                .uri("/user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.content[0].email").isNotEmpty
                .jsonPath("$.content[0].firstName").isNotEmpty
                .jsonPath("$.content[0].lastName").isNotEmpty
        }
    }

    @Test
    fun `test get user by id`() {
        runBlocking {
            // setup
            val userId = "00000000-0000-0000-0000-000000000001"
            val expectedUserResource = userService.getByUserId("00000000-0000-0000-0000-000000000001")
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(ADMIN_AUTHORITY))
                .get()
                .uri("/user/${userId}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(UserDto::class.java)
                .value {
                    assertThat(it).isEqualTo(expectedUserResource)
                }
        }
    }

    @Test
    fun `test get a user by invalid id`() {
        runBlocking {
            // setup
            val invalidUserId = "fffffff-ffff-ffff-ffff-ffffffffffff"
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(ADMIN_AUTHORITY))
                .get()
                .uri("/user/${invalidUserId}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$.timestamp").isNotEmpty
                .jsonPath("$.path").isEqualTo("/user/${invalidUserId}")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("User [${invalidUserId}] was not found")
                .jsonPath("$.requestId").isNotEmpty
        }
    }

}