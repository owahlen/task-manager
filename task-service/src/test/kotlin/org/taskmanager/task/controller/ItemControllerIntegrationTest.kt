package org.taskmanager.task.controller

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.taskmanager.task.api.resource.ItemCreateResource
import org.taskmanager.task.api.resource.ItemPatchResource
import org.taskmanager.task.api.resource.ItemResource
import org.taskmanager.task.api.resource.ItemUpdateResource
import org.taskmanager.task.exception.ItemNotFoundException
import org.taskmanager.task.mapper.toItemResource
import org.taskmanager.task.model.Item
import org.taskmanager.task.service.ItemService
import java.util.*

// todo: improve tests!!!

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
class ItemControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val itemService: ItemService
) {

    private val DEFAULT_PAGEABLE =
        PageRequest.of(0, 100, Sort.by(Order.by("lastModifiedDate")))

    @Test
    fun `test get item page`() {
        runBlocking {
            // setup
            val expectedItemResources =
                itemService.findAllBy(DEFAULT_PAGEABLE).map(Item::toItemResource).toList()
            assertThat(expectedItemResources.count()).isGreaterThan(0)
            // when
            webTestClient.get()
                .uri("/item")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.content[0].firstName").isNotEmpty
        }
    }

    @Test
    fun `test get item by id`() {
        runBlocking {
            // setup
            val expectedItemResource = itemService.getById(1).toItemResource()
            // when
            webTestClient.get()
                .uri("/item/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(ItemResource::class.java)
                .value {
                    assertThat(it).isEqualTo(expectedItemResource)
                }
        }
    }

    @Test
    fun `test get a item by invalid id`() {
        runBlocking {
            // when
            webTestClient.get()
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
    fun `test create a item`() {
        runBlocking {
            // setup
            val itemCreateResource =
                ItemCreateResource(description = "do homework", assigneeId = 1, tagIds = setOf(1, 2))
            // when
            webTestClient.post()
                .uri("/item")
                .bodyValue(itemCreateResource)
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
    fun `test creating a item with blank description`() {
        runBlocking {
            // setup
            val itemCreateResource = ItemCreateResource(description = "")
            // when
            webTestClient.post()
                .uri("/item")
                .bodyValue(itemCreateResource)
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
    fun `test updating a item`() {
        runBlocking {
            // setup
            val itemUpdateResource =
                ItemUpdateResource(description = "do homework", assigneeId = 1, tagIds = setOf(1, 2))
            // when
            webTestClient.put()
                .uri("/item/2")
                .bodyValue(itemUpdateResource)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(ItemResource::class.java)
                .value {
                    assertThat(it.description).isEqualTo(itemUpdateResource.description)
                }
        }
    }

    @Test
    fun `test patching a item`() {
        runBlocking {
            // setup
            val loadedItemResource = itemService.getById(3).toItemResource()
            val itemPatchResource =
                ItemPatchResource(
                    description = Optional.of("sleep"),
                    status = Optional.empty(),
                    assigneeId = Optional.empty(),
                    tagIds = Optional.empty()
                )
            // when
            webTestClient.patch()
                .uri("/item/3")
                .bodyValue(itemPatchResource)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(ItemResource::class.java)
                .value {
                    assertThat(it.description).isEqualTo("sleep")
                }
        }
    }

    @Test
    fun `test deleting a item`() {
        runBlocking {
            // setup
            // should not throw ItemNotFoundException
            itemService.getById(4)
            // when
            webTestClient.delete()
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