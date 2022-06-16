package org.taskmanager.task.controller

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.api.resource.UserPatchResource
import org.taskmanager.task.api.resource.UserResource
import org.taskmanager.task.api.resource.UserUpdateResource
import org.taskmanager.task.exception.UserNotFoundException
import org.taskmanager.task.mapper.toUserResource
import org.taskmanager.task.model.User
import org.taskmanager.task.service.UserService
import java.util.*


@AutoConfigureWebTestClient
@IntegrationTest
@DirtiesContext
class UserControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val userService: UserService
) {

    private val DEFAULT_PAGEABLE =
        PageRequest.of(0, 100, Sort.by(Order.by("firstName"), Order.by("lastName")))
    private val SUBJECT = "test_user";
    private val USER_AUTHORITY = SimpleGrantedAuthority("ROLE_USER");

    @Test
    fun `test get user page`() {
        runBlocking {
            // setup
            val expectedUserResources =
                userService.findAllBy(DEFAULT_PAGEABLE).map(User::toUserResource).toList()
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
                .jsonPath("$.content[0].firstName").isNotEmpty
        }
    }

//    .expectStatus().isOk
//    .expectBodyList(UserResource::class.java)
//    .value<ListBodySpec<UserResource>> {
//        assertThat(it).isEqualTo(expectedUserResources)
//    }

    @Test
    fun `test get user by id`() {
        runBlocking {
            // setup
            val expectedUserResource = userService.getById(1).toUserResource()
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/user/1")
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
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/user/0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$.timestamp").isNotEmpty
                .jsonPath("$.path").isEqualTo("/user/0")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("User [0] was not found")
                .jsonPath("$.requestId").isNotEmpty
        }
    }

    @Test
    fun `test create a user`() {
        runBlocking {
            // setup
            val userCreateResource = UserCreateResource(firstName = "John", lastName = "Doe")
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .post()
                .uri("/user")
                .bodyValue(userCreateResource)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isNumber
                .jsonPath("$.version").isNumber
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.lastName").isEqualTo("Doe")
                .jsonPath("$.createdDate").isNotEmpty
                .jsonPath("$.lastModifiedDate").isNotEmpty
        }
    }

    @Test
    fun `test creating a user with blank lastName`() {
        runBlocking {
            // setup
            val userCreateResource = UserCreateResource(firstName = "Roger", lastName = "")
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
    fun `test updating a user`() {
        runBlocking {
            // setup
            val userUpdateResource = UserUpdateResource(firstName = "John", lastName = "Doe")
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .put()
                .uri("/user/2")
                .bodyValue(userUpdateResource)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(UserResource::class.java)
                .value {
                    assertThat(it.firstName).isEqualTo(userUpdateResource.firstName)
                    assertThat(it.lastName).isEqualTo(userUpdateResource.lastName)
                }
        }
    }

    @Test
    fun `test patching a user`() {
        runBlocking {
            // setup
            val loadedUserResource = userService.getById(3).toUserResource()
            val userPatchResource =
                UserPatchResource(firstName = Optional.of("William"), lastName = Optional.empty())
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .patch()
                .uri("/user/3")
                .bodyValue(userPatchResource)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(UserResource::class.java)
                .value {
                    assertThat(it.firstName).isEqualTo("William")
                    assertThat(it.lastName).isEqualTo(loadedUserResource.lastName)
                }
        }
    }

    @Test
    fun `test deleting a user`() {
        runBlocking {
            // setup
            // should not throw UserNotFoundException
            userService.getById(4)
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .delete()
                .uri("/user/4")
                .exchange()
                // then
                .expectStatus().isNoContent
            assertThatThrownBy {
                runBlocking {
                    userService.getById(4)
                }
            }.isInstanceOf(UserNotFoundException::class.java)
        }
    }

}