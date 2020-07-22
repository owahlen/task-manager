package org.taskmanager.user.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.body
import org.springframework.test.web.reactive.server.expectBody
import org.taskmanager.user.configuration.ApiSecurityConfiguration
import org.taskmanager.user.configuration.UserDetailsConfiguration
import org.taskmanager.user.createUser
import org.taskmanager.user.model.User
import org.taskmanager.user.model.UserDTO
import org.taskmanager.user.model.toModel
import org.taskmanager.user.service.UserService
import org.taskmanager.user.toDto

@WebFluxTest(UserController::class)
@Import(ApiSecurityConfiguration::class, UserDetailsConfiguration::class)
internal class UserControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @MockkBean
    private lateinit var userService: UserService

    @Test
    fun `GET user returns array of all users`() {
        // setup
        val users = arrayOf(createUser(id = 1), createUser(id = 2))
        coEvery { userService.findAll() } returns users.asFlow()
        // when
        webTestClient.get()
                .uri("/api/user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody<Array<User>>()
                .isEqualTo(users)
    }

    @Test
    fun `GET user returns empty array if no users exist`() {
        // setup
        coEvery { userService.findAll() } returns emptyFlow()
        // when
        webTestClient.get()
                .uri("/api/user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                //then
                .expectStatus().isOk
                .expectBody<Array<User>>()
                .isEqualTo(arrayOf())
    }

    @Test
    fun `GET user-search returns filtered users`() {
        // setup
        val users = arrayOf(createUser(id = 1, email = "user1@test.com"))
        coEvery { userService.findByEmail("user1@test.com") } returns users.asFlow()
        // when
        webTestClient.get()
                .uri("/api/user/search?email=user1@test.com")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody<Array<User>>()
                .consumeWith {
                    val responseUsers: Array<User>? = it.responseBody
                    assertThat(responseUsers).isNotNull()
                    assertThat(responseUsers!!.size).isEqualTo(1)
                    assertThat(responseUsers.first().email).isEqualTo(users.first().email)
                    assertThat(responseUsers.first().password).isNotBlank()
                }

    }

    @Test
    fun `GET user-id returns existing user`() {
        // setup
        val user = createUser(id = 1)
        coEvery { userService.findById(1) } returns user
        // when
        webTestClient.get()
                .uri("/api/user/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                //then
                .expectStatus().isOk
                .expectBody<User>()
                .isEqualTo(user)
    }

    @Test
    fun `GET user-id returns NOT_FOUND for nonexistent user`() {
        // setup
        coEvery { userService.findById(999) } returns null
        // when
        webTestClient.get()
                .uri("/api/user/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                //then
                .expectStatus().isNotFound
                .expectBody<Map<String, String>>()
                .consumeWith { assertErrorResponse(it.responseBody, "/api/user/999", HttpStatus.NOT_FOUND) }
    }

    @Test
    fun `GET user-id returns BAD_REQUEST if id is not a positive integer`() {
        // when
        webTestClient.get()
                .uri("/api/user/ABC")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                //then
                .expectStatus().isBadRequest
                .expectBody<Map<String, String>>()
                .consumeWith { assertErrorResponse(it.responseBody, "/api/user/ABC", HttpStatus.BAD_REQUEST) }
    }

    @Test
    fun `POST user creates a user`() {
        // setup
        val user = createUser(id = 999)
        val userDto = user.toDto()
        coEvery { userService.create(any()) } answers {
            firstArg<UserDTO>().toModel(passwordEncoder).copy(id = 999)
        }
        // when
        webTestClient.post()
                .uri("/api/user")
                .body<UserDTO>(CompletableDeferred(userDto))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody<User>()
                .consumeWith {
                    val responseUser: User? = it.responseBody
                    assertThat(responseUser).isNotNull()
                    assertThat(responseUser!!.email).isEqualTo(user.email)
                    assertThat(responseUser.password).isNotBlank()
                }

    }

    @Test
    fun `POST user returns BAD_REQUEST if called with invalid body`() {
        // when
        webTestClient.post()
                .uri("/api/user")
                .body<UserDTO>(CompletableDeferred(""))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isBadRequest
                .expectBody<Map<String, String>>()
                .consumeWith { assertErrorResponse(it.responseBody, "/api/user", HttpStatus.BAD_REQUEST) }
    }

    @Test
    fun `POST user returns INTERNAL_SERVER_ERROR if save fails`() {
        // setup
        val userDTO = createUser(id = 999).toDto()
        coEvery { userService.create(any()) } throws IllegalArgumentException("save failed due to a mocked error")
        // when
        webTestClient.post()
                .uri("/api/user")
                .body<UserDTO>(CompletableDeferred(userDTO))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody<Map<String, String>>()
                .consumeWith { assertErrorResponse(it.responseBody, "/api/user", HttpStatus.INTERNAL_SERVER_ERROR) }
    }

    @Test
    fun `PUT user-id updates an existing user`() {
        // setup
        val user = createUser(id = 2, email = "newUser@test.com", password = "encryptedPassword")
        val userDTO = user.toDto()
        coEvery { userService.update(any(), any()) } answers {
            secondArg<UserDTO>().toModel(passwordEncoder, firstArg<Long>())
        }
        // when
        webTestClient.put()
                .uri("/api/user/2")
                .body<UserDTO>(CompletableDeferred(userDTO))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody<User>()
                .consumeWith {
                    val responseUser: User? = it.responseBody
                    assertThat(responseUser).isNotNull()
                    assertThat(responseUser!!.email).isEqualTo(user.email)
                    assertThat(responseUser.password).isNotBlank()
                }
    }

    @Test
    fun `PUT user-id returns BAD_REQUEST if id not a positive integer`() {
        // when
        webTestClient.put()
                .uri("/api/user/ABC")
                .body<UserDTO>(CompletableDeferred(createUser().toDto()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isBadRequest
                .expectBody<Map<String, String>>()
                .consumeWith { assertErrorResponse(it.responseBody, "/api/user/ABC", HttpStatus.BAD_REQUEST) }
    }

    @Test
    fun `PUT user-id returns BAD_REQUEST if called with invalid body`() {
        // when
        webTestClient.put()
                .uri("/api/user/2")
                .body<UserDTO>(CompletableDeferred(""))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isBadRequest
                .expectBody<Map<String, String>>()
                .consumeWith { assertErrorResponse(it.responseBody, "/api/user/2", HttpStatus.BAD_REQUEST) }
    }

    @Test
    fun `PUT user-id returns NOT_FOUND for nonexistent user`() {
        // setup
        coEvery { userService.update(999, any()) } returns null
        // when
        webTestClient.put()
                .uri("/api/user/999")
                .body<UserDTO>(CompletableDeferred(createUser().toDto()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isNotFound
                .expectBody<Map<String, String>>()
                .consumeWith { assertErrorResponse(it.responseBody, "/api/user/999", HttpStatus.NOT_FOUND) }
    }

    @Test
    fun `DELETE user-id returns NO_CONTENT on success`() {
        // setup
        coEvery { userService.delete(2) } returns true
        coEvery { userService.getCurrentUser() } returns "mock of current user"
        // when
        webTestClient.delete()
                .uri("/api/user/2")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isNoContent
                .expectBody<Any>()
                .consumeWith { result ->
                    assertThat(result.responseBody).isNull()
                }
    }

    @Test
    fun `DELETE user-id returns NOT_FOUND for nonexistent user`() {
        // setup
        coEvery { userService.delete(2) } returns false
        coEvery { userService.getCurrentUser() } returns "mock of current user"
        // when
        webTestClient.delete()
                .uri("/api/user/2")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isNotFound
                .expectBody<Map<String, String>>()
                .consumeWith { assertErrorResponse(it.responseBody, "/api/user/2", HttpStatus.NOT_FOUND) }
    }

    @Test
    fun `DELETE user-id returns BAD_REQUEST if id is not a positive integer`() {
        // setup
        coEvery { userService.delete(any()) } returns true
        coEvery { userService.getCurrentUser() } returns "mock of current user"
        // when
        webTestClient.delete()
                .uri("/api/user/ABC")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isBadRequest
                .expectBody<Map<String, String>>()
                .consumeWith { assertErrorResponse(it.responseBody, "/api/user/ABC", HttpStatus.BAD_REQUEST) }
    }

    private fun assertErrorResponse(apiError: Map<String, String>?, path: String, httpStatus: HttpStatus) {
        assertThat(apiError).isNotNull()
        val error = apiError!!
        assertThat(error["timestamp"]).isNotBlank()
        assertThat(error["path"]).isEqualTo(path)
        assertThat(error["status"]).isEqualTo(httpStatus.value().toString())
        assertThat(error["error"]).isEqualTo(httpStatus.reasonPhrase)
        assertThat(error["message"]).isNotNull()
        assertThat(error["requestId"]).isNotBlank()
    }

}
