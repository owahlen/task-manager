package org.taskmanager.task.controller

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.service.ItemService

@AutoConfigureWebTestClient
@IntegrationTest
@DirtiesContext
class AboutMeControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val itemService: ItemService
) {

    private val SUBJECT = "test_user";
    private val USER_AUTHORITY = SimpleGrantedAuthority("ROLE_USER");
    private val ADMIN_AUTHORITY = SimpleGrantedAuthority("ROLE_ADMIN");

    @Test
    fun `test get token attributes`() {
        runBlocking {
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/me")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(object : ParameterizedTypeReference<Map<String, Any>>() {})
                .value {
                    assertThat(it).isNotNull
                    assertThat(it["sub"]).isEqualTo(SUBJECT)
                }
        }
    }

    @Test
    fun `test get principal name`() {
        runBlocking {
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/me/name")
                .accept(MediaType.TEXT_PLAIN)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(String::class.java)
                .value {
                    assertThat(it).isEqualTo(SUBJECT)
                }
        }
    }

    @Test
    fun `test token`() {
        runBlocking {
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/me/token")
                .accept(MediaType.TEXT_PLAIN)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(String::class.java)
                .value {
                    assertThat(it).isNotEmpty
                }
        }
    }

    @Test
    fun `test has role Admin`() {
        runBlocking {
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(ADMIN_AUTHORITY))
                .get()
                .uri("/me/roleAdmin")
                .accept(MediaType.TEXT_PLAIN)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(String::class.java)
                .value {
                    assertThat(it).isNotEmpty
                }
        }
    }


    @Test
    fun `test does not have role Admin`() {
        runBlocking {
            // setup
            val testAuthority = SimpleGrantedAuthority("ROLE_TEST")
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(testAuthority))
                .get()
                .uri("/me/roleAdmin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isForbidden
        }
    }

}