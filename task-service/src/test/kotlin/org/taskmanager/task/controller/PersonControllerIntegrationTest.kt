package org.taskmanager.task.controller

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.data.domain.*
import org.springframework.data.domain.Sort.Order
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ListBodySpec
import org.taskmanager.task.api.resource.PersonCreateResource
import org.taskmanager.task.api.resource.PersonPatchResource
import org.taskmanager.task.api.resource.PersonResource
import org.taskmanager.task.api.resource.PersonUpdateResource
import org.taskmanager.task.exception.PersonNotFoundException
import org.taskmanager.task.mapper.toPerson
import org.taskmanager.task.mapper.toPersonResource
import org.taskmanager.task.model.Person
import org.taskmanager.task.service.PersonService
import java.util.*


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
class PersonControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val personService: PersonService
) {

    private val DEFAULT_PAGEABLE =
        PageRequest.of(0, 100, Sort.by(Order.by("firstName"), Order.by("lastName")))

    @Test
    fun `test get person page`() {
        runBlocking {
            // setup
            val expectedPersonResources =
                personService.findAllBy(DEFAULT_PAGEABLE).map(Person::toPersonResource).toList()
            assertThat(expectedPersonResources.count()).isGreaterThan(0)
            // when
            webTestClient.get()
                .uri("/person")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.content[0].firstName").isNotEmpty
        }
    }

//    .expectStatus().isOk
//    .expectBodyList(PersonResource::class.java)
//    .value<ListBodySpec<PersonResource>> {
//        assertThat(it).isEqualTo(expectedPersonResources)
//    }

    @Test
    fun `test get person by id`() {
        runBlocking {
            // setup
            val expectedPersonResource = personService.getById(1).toPersonResource()
            // when
            webTestClient.get()
                .uri("/person/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(PersonResource::class.java)
                .value {
                    assertThat(it).isEqualTo(expectedPersonResource)
                }
        }
    }

    @Test
    fun `test get a person by invalid id`() {
        runBlocking {
            // when
            webTestClient.get()
                .uri("/person/0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // then
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$.timestamp").isNotEmpty
                .jsonPath("$.path").isEqualTo("/person/0")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("Person [0] was not found")
                .jsonPath("$.requestId").isNotEmpty
        }
    }

    @Test
    fun `test create a person`() {
        runBlocking {
            // setup
            val personCreateResource = PersonCreateResource(firstName = "John", lastName = "Doe")
            // when
            webTestClient.post()
                .uri("/person")
                .bodyValue(personCreateResource)
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
    fun `test creating a person with blank lastName`() {
        runBlocking {
            // setup
            val personCreateResource = PersonCreateResource(firstName = "Roger", lastName = "")
            // when
            webTestClient.post()
                .uri("/person")
                .bodyValue(personCreateResource)
                .exchange()
                // then
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.timestamp").isNotEmpty
                .jsonPath("$.path").isEqualTo("/person")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("lastName [] must not be blank")
                .jsonPath("$.requestId").isNotEmpty
        }
    }

    @Test
    fun `test updating a person`() {
        runBlocking {
            // setup
            val personUpdateResource = PersonUpdateResource(firstName = "John", lastName = "Doe")
            // when
            webTestClient.put()
                .uri("/person/2")
                .bodyValue(personUpdateResource)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(PersonResource::class.java)
                .value {
                    assertThat(it.firstName).isEqualTo(personUpdateResource.firstName)
                    assertThat(it.lastName).isEqualTo(personUpdateResource.lastName)
                }
        }
    }

    @Test
    fun `test patching a person`() {
        runBlocking {
            // setup
            val loadedPersonResource = personService.getById(3).toPersonResource()
            val personPatchResource =
                PersonPatchResource(firstName = Optional.of("William"), lastName = Optional.empty())
            // when
            webTestClient.patch()
                .uri("/person/3")
                .bodyValue(personPatchResource)
                .exchange()
                // then
                .expectStatus().isOk
                .expectBody(PersonResource::class.java)
                .value {
                    assertThat(it.firstName).isEqualTo("William")
                    assertThat(it.lastName).isEqualTo(loadedPersonResource.lastName)
                }
        }
    }

    @Test
    fun `test deleting a person`() {
        runBlocking {
            // setup
            // should not throw PersonNotFoundException
            personService.getById(4)
            // when
            webTestClient.delete()
                .uri("/person/4")
                .exchange()
                // then
                .expectStatus().isNoContent
            assertThatThrownBy {
                runBlocking {
                    personService.getById(4)
                }
            }.isInstanceOf(PersonNotFoundException::class.java)
        }
    }


//    @Test
//    fun GIVEN_no_item_WHEN_getting_all_THEN_the_item_are_returned() {
//
//        // Given no items
//        val item1: Item = itemRepository.save(
//            Item().setStatus(ItemStatus.TODO)
//                .setDescription("Item1")
//        ).block()
//        assertNotNull(item1)
//        val item2: Item = itemRepository.save(
//            Item().setStatus(ItemStatus.DONE)
//                .setDescription("Item2")
//        ).block()
//        assertNotNull(item2)
//
//        // When
//        webTestClient!!.get()
//            .uri(URI)
//            .exchange() // Then
//            .expectStatus().isOk
//            .expectBodyList(ItemResource::class.java)
//            .value { itemResources ->
//                assertEquals(2, itemResources.size())
//                assertItemEquals(item1, itemResources.get(0))
//                assertItemEquals(item2, itemResources.get(1))
//            }
//    }
//
//    //-----------------------------------
//    //
//    //               POST
//    //
//    //-----------------------------------
//    @Test
//    fun GIVEN_a_resource_to_be_created_WHEN_posting_it_THEN_it_is_created() {
//
//        // Given
//        val itemResource: NewItemResource = NewItemResource().setDescription("New")
//
//        // When
//        val location = webTestClient!!.post()
//            .uri(URI)
//            .bodyValue(itemResource)
//            .exchange() // Then
//            .expectStatus().isCreated
//            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/items/[0-9]+")
//            .returnResult(ResponseEntity::class.java).responseHeaders[HttpHeaders.LOCATION]!![0]
//        val id = java.lang.Long.valueOf(location.substring(location.lastIndexOf('/') + 1))
//        val createdItem: Item = itemRepository.findById(id).block()
//        assertNotNull(createdItem)
//        assertEquals(itemResource.getDescription(), createdItem.getDescription())
//        assertEquals(ItemStatus.TODO, createdItem.getStatus())
//        assertEquals(0L, createdItem.getVersion())
//    }
//
//    @Test
//    fun GIVEN_a_resource_to_be_created_without_description_WHEN_posting_it_THEN_it_is_NOT_created() {
//
//        // Given
//        val itemResource = NewItemResource()
//
//        // When
//        webTestClient!!.post()
//            .uri(URI)
//            .bodyValue(itemResource)
//            .exchange() // Then
//            .expectStatus().isBadRequest
//    }
//
//    //-----------------------------------
//    //
//    //               Put
//    //
//    //-----------------------------------
//    @Test
//    fun GIVEN_no_item_WHEN_trying_to_update_a_non_existing_item_THEN_an_error_is_returned() {
//        // Given
//
//        // When
//        val itemUpdateResource: ItemUpdateResource = ItemUpdateResource()
//            .setDescription("updated")
//            .setStatus(ItemStatus.DONE)
//
//        // When
//        webTestClient!!.put()
//            .uri(URI + "/-1")
//            .bodyValue(itemUpdateResource)
//            .header(IF_MATCH, "0")
//            .exchange() // Then
//            .expectStatus().isNotFound
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun GIVEN_an_item_WHEN_updating_it_THEN_it_is_updated() {
//
//        // Given
//        val item: Item = itemRepository.save(
//            Item()
//                .setStatus(ItemStatus.DONE)
//                .setDescription("description")
//        ).block()
//        assertNotNull(item)
//        assertEquals(0L, item.getVersion())
//
//        // When
//        val itemResource: ItemUpdateResource = ItemUpdateResource()
//            .setDescription("New description")
//            .setStatus(ItemStatus.TODO)
//        webTestClient!!.put()
//            .uri(URI + item.getId())
//            .header(IF_MATCH, item.getVersion().toString())
//            .bodyValue(itemResource)
//            .exchange() // Then
//            .expectStatus().isNoContent
//        val updatedItem: Item = itemRepository.findById(item.getId()).block()
//        assertNotNull(updatedItem)
//        assertEquals(itemResource.getDescription(), updatedItem.getDescription())
//        assertEquals(itemResource.getStatus(), updatedItem.getStatus())
//        assertEquals(1L, updatedItem.getVersion())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testOptimisticLockingPut_preconditionFailed() {
//        // Given
//        val item: Item = createItemWithTwoRevisions()
//
//        // When
//        val itemUpdateResource: ItemUpdateResource = ItemUpdateResource()
//            .setDescription("update old version")
//            .setStatus(ItemStatus.DONE)
//
//        // Precondition failed
//        webTestClient!!.put()
//            .uri(URI + item.getId())
//            .bodyValue(itemUpdateResource)
//            .header(IF_MATCH, "0")
//            .exchange() // Then
//            .expectStatus().isEqualTo(HttpStatus.PRECONDITION_FAILED)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testOptimisticLockingPut_preconditionSucceeded() {
//
//        // Given
//        val item: Item = createItemWithTwoRevisions()
//
//        // When
//        val itemUpdateResource: ItemUpdateResource = ItemUpdateResource()
//            .setDescription("update old version")
//            .setStatus(ItemStatus.DONE)
//
//        // Precondition succeeded
//        webTestClient!!.put()
//            .uri(URI + item.getId())
//            .bodyValue(itemUpdateResource)
//            .header(IF_MATCH, "1")
//            .exchange() // Then
//            .expectStatus().isNoContent
//        val updatedItem: Item = itemRepository.findById(item.getId()).block()
//        assertNotNull(updatedItem)
//        assertEquals(2L, updatedItem.getVersion())
//        assertEquals(itemUpdateResource.getDescription(), updatedItem.getDescription())
//    }
//
//    //-----------------------------------
//    //
//    //               Delete
//    //
//    //-----------------------------------
//    @Test
//    @Throws(Exception::class)
//    fun GIVEN_no_item_WHEN_trying_to_delete_a_non_existing_item_THEN_an_error_is_returned() {
//        webTestClient!!.delete()
//            .uri(URI + "-1")
//            .header(IF_MATCH, "0")
//            .exchange() // Then
//            .expectStatus().isNotFound
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun GIVEN_an_item_WHEN_deleting_it_THEN_it_is_deleted() {
//
//        // Given
//        val item: Item = itemRepository.save(
//            Item()
//                .setStatus(ItemStatus.DONE)
//                .setDescription("description")
//        ).block()
//        assertNotNull(item)
//
//        // When deleting it
//        webTestClient!!.delete()
//            .uri(URI + item.getId())
//            .header(IF_MATCH, item.getVersion().toString())
//            .exchange() // Then
//            .expectStatus().isOk
//
//        // When trying ot delete it once more
//        webTestClient!!.delete()
//            .uri(URI + item.getId())
//            .header(IF_MATCH, item.getVersion().toString())
//            .exchange() // Then
//            .expectStatus().isNotFound
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testOptimisticLockingDelete_preconditionFailed() {
//
//        // Given
//        val item: Item = itemRepository.save(
//            Item()
//                .setStatus(ItemStatus.DONE)
//                .setDescription("description")
//        ).block()
//        assertNotNull(item)
//
//        // When
//        webTestClient!!.delete()
//            .uri(URI + item.getId())
//            .header(IF_MATCH, "1")
//            .exchange() // Then
//            .expectStatus().isEqualTo(HttpStatus.PRECONDITION_FAILED)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testOptimisticLockingDelete_preconditionSucceeded() {
//
//        // Given
//        val item: Item = itemRepository.save(
//            Item()
//                .setStatus(ItemStatus.DONE)
//                .setDescription("description")
//        ).block()
//        assertNotNull(item)
//
//        // When
//        webTestClient!!.delete()
//            .uri(URI + item.getId())
//            .header(IF_MATCH, "0")
//            .exchange() // Then
//            .expectStatus().isOk
//    }
//
//    //-----------------------------------
//    //
//    //              Patch
//    //
//    //-----------------------------------
//    @Test
//    @Throws(Exception::class)
//    fun GIVEN_no_item_WHEN_trying_to_patch_a_non_existing_item_THEN_an_error_is_returned() {
//
//        // When
//        webTestClient!!.patch()
//            .uri(URI + "-1")
//            .header(IF_MATCH, "0")
//            .bodyValue("{\"description\": \"updated\"}")
//            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//            .exchange() // Then
//            .expectStatus().isNotFound
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun GIVEN_an_item_WHEN_patching_its_status_THEN_only_the_status_is_patched() {
//
//        // Given
//        val item: Item = itemRepository.save(
//            Item()
//                .setStatus(ItemStatus.DONE)
//                .setDescription("description")
//        ).block()
//        assertNotNull(item)
//
//        // When
//        webTestClient!!.patch()
//            .uri(URI + item.getId())
//            .header(IF_MATCH, item.getVersion().toString())
//            .bodyValue("{\"description\": \"updated\"}")
//            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//            .exchange() // Then
//            .expectStatus().isNoContent
//        val updatedItem: Item = itemRepository.findById(item.getId()).block()
//        assertNotNull(updatedItem)
//        assertEquals("updated", updatedItem.getDescription())
//        assertEquals(ItemStatus.DONE, updatedItem.getStatus())
//        assertEquals(1L, updatedItem.getVersion())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testOptimisticLockingPatch_preconditionFailed() {
//
//        // Given
//        val item: Item = createItemWithTwoRevisions()
//
//        // When
//        webTestClient!!.patch()
//            .uri(URI + item.getId())
//            .bodyValue("{\"description\": \"updated\"}")
//            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//            .header(IF_MATCH, "0")
//            .exchange() // Then
//            .expectStatus().isEqualTo(HttpStatus.PRECONDITION_FAILED)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testOptimisticLockingPatch_preconditionSucceeded() {
//
//        // Given
//        val item: Item = createItemWithTwoRevisions()
//
//        // When
//        webTestClient!!.patch()
//            .uri(URI + item.getId())
//            .bodyValue("{\"description\": \"updated\"}")
//            .header(IF_MATCH, "1")
//            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//            .exchange() // Then
//            .expectStatus().isNoContent
//        val updatedItem: Item = itemRepository.findById(item.getId()).block()
//        assertNotNull(updatedItem)
//        assertEquals(2L, updatedItem.getVersion())
//        assertEquals("updated", updatedItem.getDescription())
//    }
//
//    //-----------------------------------
//    //
//    //         Private methods
//    //
//    //-----------------------------------
//    private fun createItemWithTwoRevisions(): Item {
//        var item: Item = itemRepository.save(
//            Item()
//                .setStatus(ItemStatus.DONE)
//                .setDescription("description")
//        ).block()
//        assertNotNull(item)
//        assertEquals(0L, item.getVersion())
//        item = itemRepository.save(item.setDescription("description version 1")).block()
//        assertNotNull(item)
//        assertEquals(1L, item.getVersion())
//        return item
//    }
//
//    private fun assertItemEquals(item: Item, itemResource: ItemResource) {
//        assertEquals(item.getDescription(), itemResource.getDescription())
//        assertEquals(item.getStatus(), itemResource.getStatus())
//        assertEquals(item.getVersion(), itemResource.getVersion())
//    }

}