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
import org.taskmanager.task.api.dto.TagCreateDto
import org.taskmanager.task.api.dto.TagPatchDto
import org.taskmanager.task.api.dto.TagDto
import org.taskmanager.task.api.dto.TagUpdateDto
import org.taskmanager.task.exception.TagNotFoundException
import org.taskmanager.task.service.TagService
import java.util.*


@AutoConfigureWebTestClient
@IntegrationTest
@DirtiesContext
class TagControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val tagService: TagService
) {

    private val DEFAULT_PAGEABLE =
        PageRequest.of(0, 100, Sort.by(Order.by("name")))
    private val SUBJECT = "test_user";
    private val USER_AUTHORITY = SimpleGrantedAuthority("ROLE_USER");

    @Test
    fun `test get tag page`() {
        runBlocking {
            // setup
            val expectedTagResources =
                tagService.findAllBy(DEFAULT_PAGEABLE).toList()
            assertThat(expectedTagResources.count()).isGreaterThan(0)
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/tag")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.content[0].name").isNotEmpty
        }
    }

    @Test
    fun `test get tag by id`() {
        runBlocking {
            // setup
            val expectedTagResource = tagService.getById(1)
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/tag/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(TagDto::class.java)
                .value {
                    assertThat(it).isEqualTo(expectedTagResource)
                }
        }
    }

    @Test
    fun `test get a tag by invalid id`() {
        runBlocking {
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/tag/0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$.timestamp").isNotEmpty
                .jsonPath("$.path").isEqualTo("/tag/0")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("Tag [0] was not found")
                .jsonPath("$.requestId").isNotEmpty
        }
    }

    @Test
    fun `test create a tag`() {
        runBlocking {
            // setup
            val tagCreateDto = TagCreateDto(name = "Weather")
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .post()
                .uri("/tag")
                .bodyValue(tagCreateDto)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isNumber
                .jsonPath("$.version").isNumber
                .jsonPath("$.name").isEqualTo("Weather")
                .jsonPath("$.createdDate").isNotEmpty
                .jsonPath("$.lastModifiedDate").isNotEmpty
        }
    }

    @Test
    fun `test creating a tag with blank name`() {
        runBlocking {
            // setup
            val tagCreateDto = TagCreateDto(name = "")
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .post()
                .uri("/tag")
                .bodyValue(tagCreateDto)
                .exchange()
                // then
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.timestamp").isNotEmpty
                .jsonPath("$.path").isEqualTo("/tag")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("name [] must not be blank")
                .jsonPath("$.requestId").isNotEmpty
        }
    }

    @Test
    fun `test updating a tag`() {
        runBlocking {
            // setup
            val tagUpdateDto = TagUpdateDto(name = "Vacation")
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .put()
                .uri("/tag/2")
                .bodyValue(tagUpdateDto)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(TagDto::class.java)
                .value {
                    assertThat(it.name).isEqualTo(tagUpdateDto.name)
                }
        }
    }

    @Test
    fun `test patching a tag`() {
        runBlocking {
            // setup
            tagService.getById(3)
            val tagPatchDto =
                TagPatchDto(name = Optional.of("Zoo"))
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .patch()
                .uri("/tag/3")
                .bodyValue(tagPatchDto)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(TagDto::class.java)
                .value {
                    assertThat(it.name).isEqualTo("Zoo")
                }
        }
    }

    @Test
    fun `test deleting a tag`() {
        runBlocking {
            // setup
            // should not throw TagNotFoundException
            tagService.getById(4)
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .delete()
                .uri("/tag/4")
                .exchange()
                // then
                .expectStatus().isNoContent
            assertThatThrownBy {
                runBlocking {
                    tagService.getById(4)
                }
            }.isInstanceOf(TagNotFoundException::class.java)
        }
    }

}