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
import org.taskmanager.task.api.resource.PersonCreateResource
import org.taskmanager.task.api.resource.PersonPatchResource
import org.taskmanager.task.api.resource.PersonResource
import org.taskmanager.task.api.resource.PersonUpdateResource
import org.taskmanager.task.exception.PersonNotFoundException
import org.taskmanager.task.mapper.toPersonResource
import org.taskmanager.task.model.Person
import org.taskmanager.task.service.PersonService
import java.util.*


@AutoConfigureWebTestClient
@IntegrationTest
@DirtiesContext
class PersonControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val personService: PersonService
) {

    private val DEFAULT_PAGEABLE =
        PageRequest.of(0, 100, Sort.by(Order.by("firstName"), Order.by("lastName")))
    private val SUBJECT = "test_user";
    private val USER_AUTHORITY = SimpleGrantedAuthority("ROLE_USER");

    @Test
    fun `test get person page`() {
        runBlocking {
            // setup
            val expectedPersonResources =
                personService.findAllBy(DEFAULT_PAGEABLE).map(Person::toPersonResource).toList()
            assertThat(expectedPersonResources.count()).isGreaterThan(0)
            // when
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
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
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
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
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .get()
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
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .post()
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
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .post()
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
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .put()
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
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .patch()
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
            webTestClient.mutateWith(mockJwt().jwt { it.subject(SUBJECT) }.authorities(USER_AUTHORITY))
                .delete()
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

}