package org.taskmanager.task.controller

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.api.dto.*
import org.taskmanager.task.exception.ItemNotFoundException
import org.taskmanager.task.model.ItemStatus
import org.taskmanager.task.service.ItemService
import java.util.*

@AutoConfigureWebTestClient
@IntegrationTest
@DirtiesContext
class ItemControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val itemService: ItemService
) {

    private val DEFAULT_PAGEABLE =
        PageRequest.of(0, 100, Sort.by(Order.by("lastModifiedDate"), Order.by("description")))
    private val SUBJECT = "test_user";
    private val USER_AUTHORITY = SimpleGrantedAuthority("ROLE_USER");

    @Test
    fun `test get item page`() {
        runBlocking {
            // setup
            val expectedItemResources = itemService.findAllBy(DEFAULT_PAGEABLE).toList()
            assertThat(expectedItemResources.count()).isGreaterThan(0)
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/item")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(object : ParameterizedTypeReference<Page<ItemDto>>() {})
                .value {
                    assertThat(it.totalPages).isGreaterThan(0)
                    assertThat(it.totalElements).isGreaterThan(0)
                    assertThat(it.number).isEqualTo(DEFAULT_PAGEABLE.pageNumber)
                    assertThat(it.size).isEqualTo(DEFAULT_PAGEABLE.pageSize)
                    assertThat(it.sort).isEqualTo(DEFAULT_PAGEABLE.sort)
                    assertThat(it.content).isEqualTo(expectedItemResources)
                }
        }
    }

    @Test
    fun `test get item page unauthorized`() {
        runBlocking {
            // when
            webTestClient.get()
                .uri("/item")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isUnauthorized
        }
    }

    @Test
    fun `test get item by id`() {
        runBlocking {
            // setup
            val expectedItemResource = itemService.getById(1)
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/item/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(ItemDto::class.java)
                .value {
                    assertThat(it).isEqualTo(expectedItemResource)
                }
        }
    }

    @Test
    fun `test get an item by invalid id`() {
        runBlocking {
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
                .uri("/item/0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$.timestamp").isNotEmpty
                .jsonPath("$.path").isEqualTo("/item/0")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("Item [0] was not found")
                .jsonPath("$.requestId").isNotEmpty
        }
    }

    @Test
    fun `test create an item`() {
        runBlocking {
            // setup
            val assigneeUserId = "00000000-0000-0000-0000-000000000001"
            val itemCreateDto =
                ItemCreateDto(description = "do homework", assigneeUserId = assigneeUserId, tagIds = setOf(1, 2))
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .post()
                .uri("/item")
                .bodyValue(itemCreateDto)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(ItemDto::class.java)
                .value {
                    assertThat(it.id).isNotNull
                    assertThat(it.version).isNotNull
                    assertThat(it.description).isEqualTo("do homework")
                    assertThat(it.status).isEqualTo(ItemStatus.TODO)
                    val assignee = it.assignee
                    assertThat(assignee).isNotNull
                    assertThat(assignee!!.userId).isEqualTo(assigneeUserId)
                    assertThat(assignee.version).isNotNull
                    assertThat(assignee.firstName).isNotBlank
                    assertThat(assignee.lastName).isNotBlank
                    assertThat(assignee.createdDate).isNotNull
                    assertThat(assignee.lastModifiedDate).isNotNull
                    val tags = it.tags?.sortedBy { it.id }
                    assertThat(tags).isNotNull
                    assertThat(tags!!.size).isEqualTo(2)
                    assertThat(tags[0].id).isEqualTo(1)
                    assertThat(tags[0].version).isNotNull
                    assertThat(tags[0].name).isNotBlank
                    assertThat(tags[0].createdDate).isNotNull
                    assertThat(tags[0].lastModifiedDate).isNotNull
                    assertThat(tags[1].id).isEqualTo(2)
                    assertThat(tags[1].version).isNotNull
                    assertThat(tags[1].name).isNotBlank
                    assertThat(tags[1].createdDate).isNotNull
                    assertThat(tags[1].lastModifiedDate).isNotNull
                }
        }
    }

    @Test
    fun `test creating an item with blank description`() {
        runBlocking {
            // setup
            val itemCreateDto = ItemCreateDto(description = "")
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .post()
                .uri("/item")
                .bodyValue(itemCreateDto)
                .exchange()
                // then
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.timestamp").isNotEmpty
                .jsonPath("$.path").isEqualTo("/item")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("description [] must not be blank")
                .jsonPath("$.requestId").isNotEmpty
        }
    }

    @Test
    fun `test updating an item`() {
        runBlocking {
            // setup
            val assigneeUserId = "00000000-0000-0000-0000-000000000001"
            val originalItem = itemService.getById(2) // ensure item with id 3 exists
            val itemUpdateDto = ItemUpdateDto(
                    description = "do homework",
                    status = ItemStatus.DONE,
                    assigneeUserId = assigneeUserId,
                    tagIds = setOf(1, 2)
                )
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .put()
                .uri("/item/2")
                .bodyValue(itemUpdateDto)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(ItemDto::class.java)
                .value {
                    assertThat(it.id).isEqualTo(2)
                    assertThat(it.version).isNotNull
                    assertThat(it.version).isEqualTo(originalItem.version!! + 1)
                    assertThat(it.description).isEqualTo("do homework")
                    assertThat(it.status).isEqualTo(ItemStatus.DONE)
                    val assignee = it.assignee
                    assertThat(assignee).isNotNull
                    assertThat(assignee!!.userId).isEqualTo(assigneeUserId)
                    assertThat(assignee.version).isNotNull
                    assertThat(assignee.firstName).isNotBlank
                    assertThat(assignee.lastName).isNotBlank
                    assertThat(assignee.createdDate).isNotNull
                    assertThat(assignee.lastModifiedDate).isNotNull
                    val tags = it.tags?.sortedBy { it.id }
                    assertThat(tags).isNotNull
                    assertThat(tags!!.size).isEqualTo(2)
                    assertThat(tags[0].id).isEqualTo(1)
                    assertThat(tags[0].version).isNotNull
                    assertThat(tags[0].name).isNotBlank
                    assertThat(tags[0].createdDate).isNotNull
                    assertThat(tags[0].lastModifiedDate).isNotNull
                    assertThat(tags[1].id).isEqualTo(2)
                    assertThat(tags[1].version).isNotNull
                    assertThat(tags[1].name).isNotBlank
                    assertThat(tags[1].createdDate).isNotNull
                    assertThat(tags[1].lastModifiedDate).isNotNull
                    assertThat(it.createdDate).isEqualTo(originalItem.createdDate)
                    assertThat(it.lastModifiedDate).isNotNull
                }
        }
    }

    @Test
    fun `test patching an item`() {
        runBlocking {
            // setup
            val originalItem = itemService.getById(3, null, true) // ensure item with id 3 exists
            val itemPatchDto =
                ItemPatchDto(
                    description = Optional.of("sleep"),
                    status = Optional.empty(),
                    assigneeUserId = Optional.empty(),
                    tagIds = Optional.empty()
                )
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .patch()
                .uri("/item/3")
                .bodyValue(itemPatchDto)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(ItemDto::class.java)
                .value {
                    assertThat(it.id).isEqualTo(3)
                    assertThat(it.version).isNotNull
                    assertThat(it.version).isEqualTo(originalItem.version!! + 1)
                    assertThat(it.description).isEqualTo("sleep")
                    assertThat(it.status).isEqualTo(originalItem.status)
                    assertThat(it.assignee).isEqualTo(originalItem.assignee)
                    val tagDtos = it.tags?.sortedBy(TagDto::id)
                    val originalItemTagDtos = originalItem.tags?.sortedBy(TagDto::id)
                    assertThat(tagDtos).isEqualTo(originalItemTagDtos)
                    assertThat(it.createdDate).isEqualTo(originalItem.createdDate)
                    assertThat(it.lastModifiedDate).isNotNull
                }
        }
    }

    @Test
    fun `test deleting an item`() {
        runBlocking {
            // setup
            // should not throw ItemNotFoundException
            itemService.getById(4)
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .delete()
                .uri("/item/4")
                .exchange()
                // then
                .expectStatus().isNoContent
            assertThatThrownBy {
                runBlocking {
                    itemService.getById(4)
                }
            }.isInstanceOf(ItemNotFoundException::class.java)
        }
    }

}