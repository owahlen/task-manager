package org.taskmanager.task.controller

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.api.resource.UserPatchResource
import org.taskmanager.task.api.resource.UserResource
import org.taskmanager.task.api.resource.UserUpdateResource
import org.taskmanager.task.configuration.keycloak.FakeKeycloakUserStore
import org.taskmanager.task.configuration.keycloak.KeycloakTestConfiguration
import org.taskmanager.task.exception.UserNotFoundException
import org.taskmanager.task.repository.UserRepository
import org.taskmanager.task.service.UserService
import java.util.*


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
    private val USER_AUTHORITY = SimpleGrantedAuthority("ROLE_USER");

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
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
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
            val uuid = "00000000-0000-0000-0000-000000000001"
            val expectedUserResource = userService.getByUuid("00000000-0000-0000-0000-000000000001")
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/user/${uuid}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(UserResource::class.java)
                .value {
                    assertThat(it).isEqualTo(expectedUserResource)
                }
        }
    }

    @Test
    fun `test get a user by invalid id`() {
        runBlocking {
            // setup
            val invalidUuid = "fffffff-ffff-ffff-ffff-ffffffffffff"
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/user/${invalidUuid}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$.timestamp").isNotEmpty
                .jsonPath("$.path").isEqualTo("/user/${invalidUuid}")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("User [${invalidUuid}] was not found")
                .jsonPath("$.requestId").isNotEmpty
        }
    }

    @Test
    fun `test create a user`() {
        runBlocking {
            // setup
            val userCreateResource = UserCreateResource(
                email = "john.doe@test.org",
                password = "password",
                firstName = "John",
                lastName = "Doe"
            )
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .post()
                .uri("/user")
                .bodyValue(userCreateResource)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(UserResource::class.java)
                .value {
                    assertThat(it.uuid).isNotBlank
                    assertThat(it.uuid).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")
                    assertThat(it.version).isEqualTo(0)
                    assertThat(it.email).isNotBlank
                    assertThat(it.firstName).isNotBlank
                    assertThat(it.lastName).isNotBlank
                    assertThat(it.createdDate).isNotNull
                    assertThat(it.lastModifiedDate).isNotNull
                }
        }
    }

    @Test
    fun `test creating a user with blank lastName`() {
        runBlocking {
            // setup
            val userCreateResource = UserCreateResource(
                email = "roger.taylor@test.org",
                password = "roger1",
                firstName = "Roger",
                lastName = ""
            )
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .post()
                .uri("/user")
                .bodyValue(userCreateResource)
                .exchange()
                // then
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.timestamp").isNotEmpty
                .jsonPath("$.path").isEqualTo("/user")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("lastName [] must not be blank")
                .jsonPath("$.requestId").isNotEmpty
        }
    }

    @Test
    fun `test updating a user without providing password`() {
        runBlocking {
            // setup
            val uuid = "00000000-0000-0000-0000-000000000002"
            val userUpdateResource = UserUpdateResource(
                email = "john.doe@test.org",
                firstName = "John",
                lastName = "Doe"
            )
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .put()
                .uri("/user/${uuid}")
                .bodyValue(userUpdateResource)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(UserResource::class.java)
                .value {
                    assertThat(it.email).isEqualTo(userUpdateResource.email)
                    // todo: test that password is not changed
                    assertThat(it.firstName).isEqualTo(userUpdateResource.firstName)
                    assertThat(it.lastName).isEqualTo(userUpdateResource.lastName)
                }
        }
    }

    @Test
    fun `test updating a user with provided password`() {
        runBlocking {
            // setup
            val uuid = "00000000-0000-0000-0000-000000000002"
            val userUpdateResource = UserUpdateResource(
                email = "john.doe@test.org",
                password = "password",
                firstName = "John",
                lastName = "Doe"
            )
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .put()
                .uri("/user/${uuid}")
                .bodyValue(userUpdateResource)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(UserResource::class.java)
                .value {
                    assertThat(it.email).isEqualTo(userUpdateResource.email)
                    // todo: test that password is changed
                    assertThat(it.firstName).isEqualTo(userUpdateResource.firstName)
                    assertThat(it.lastName).isEqualTo(userUpdateResource.lastName)
                }
        }
    }

    @Test
    fun `test patching a user`() {
        runBlocking {
            // setup
            val uuid = "00000000-0000-0000-0000-000000000003"
            val loadedUserResource = userService.getByUuid(uuid)
            val userPatchResource =
                UserPatchResource(
                    email = Optional.empty(),
                    password = Optional.empty(),
                    firstName = Optional.of("William"),
                    lastName = Optional.empty()
                )
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .patch()
                .uri("/user/${uuid}")
                .bodyValue(userPatchResource)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(UserResource::class.java)
                .value {
                    assertThat(it.email).isEqualTo(loadedUserResource.email)
                    assertThat(it.firstName).isEqualTo("William")
                    assertThat(it.lastName).isEqualTo(loadedUserResource.lastName)
                }
        }
    }

    @Test
    fun `test deleting a user`() {
        runBlocking {
            // setup
            val uuid = "00000000-0000-0000-0000-000000000004"
            // should not throw UserNotFoundException
            userService.getByUuid(uuid)
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .delete()
                .uri("/user/${uuid}")
                .exchange()
                // then
                .expectStatus().isNoContent
            assertThatThrownBy {
                runBlocking {
                    userService.getByUuid(uuid)
                }
            }.isInstanceOf(UserNotFoundException::class.java)
        }
    }

}