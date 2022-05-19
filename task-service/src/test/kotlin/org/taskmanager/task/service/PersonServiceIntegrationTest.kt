package org.taskmanager.task.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.test.annotation.DirtiesContext
import org.taskmanager.task.api.resource.PersonCreateResource
import org.taskmanager.task.api.resource.PersonUpdateResource
import org.taskmanager.task.exception.PersonNotFoundException
import org.taskmanager.task.exception.UnexpectedPersonVersionException
import org.taskmanager.task.mapper.toPerson
import org.taskmanager.task.model.Person


@SpringBootTest
@DirtiesContext
class PersonServiceIntegrationTest(@Autowired val personService: PersonService) {

    @Test
    fun `test findAllBy pageable returns page of persons`() {
        runBlocking {
            // setup
            val sort = Sort.by(Order.by("firstName"), Order.by("lastName"))
            val pageable = PageRequest.of(0, 100, sort)
            // when
            val persons = personService.findAllBy(pageable).toList()
            // then
            assertThat(persons.count()).isGreaterThan(2)
            val sortedPersons = persons.sortedWith(compareBy(Person::firstName, Person::lastName))
            assertThat(persons).isEqualTo(sortedPersons)
        }
    }

    @Test
    fun `test getById returns person or throws PersonNotFoundException`() {
        runBlocking {
            // when
            val existingPerson = personService.getById(1)
            // then
            assertThat(existingPerson).isNotNull()
            assertThat(existingPerson.id).isEqualTo(1)

            // when / then
            assertThatThrownBy {
                runBlocking {
                    personService.getById(-1)
                }
            }.isInstanceOf(PersonNotFoundException::class.java)
        }
    }

    @Test
    fun `test getById with wrong version throws UnexpectedPersonVersionException`() {
        runBlocking {
            // when / then
            assertThatThrownBy {
                runBlocking {
                    personService.getById(1,-1)
                }
            }.isInstanceOf(UnexpectedPersonVersionException::class.java)
        }
    }

    @Test
    fun `test create person`() {
        runBlocking {
            // setup
            val person = PersonCreateResource("John", "Walker").toPerson()
            // when
            val savedPerson = personService.create(person)
            // then
            assertThat(savedPerson).isNotNull
            assertThat(savedPerson.id).isNotNull
            assertThat(savedPerson.version).isNotNull
            assertThat(savedPerson.firstName).isEqualTo(person.firstName)
            assertThat(savedPerson.lastName).isEqualTo(person.lastName)
            assertThat(savedPerson.createdDate).isNotNull
            assertThat(savedPerson.lastModifiedDate).isNotNull
        }
    }

    @Test
    fun `test update person`() {
        runBlocking {
            // setup
            val person = PersonUpdateResource("John", "Walker").toPerson(2, null)
            // when
            val updatedPerson = personService.update(person)
            // then
            assertThat(updatedPerson).isNotNull
            assertThat(updatedPerson.id).isEqualTo(2)
            assertThat(updatedPerson.version).isNotNull
            assertThat(updatedPerson.firstName).isEqualTo(person.firstName)
            assertThat(updatedPerson.lastName).isEqualTo(person.lastName)
            assertThat(updatedPerson.createdDate).isNotNull
            assertThat(updatedPerson.lastModifiedDate).isNotNull
        }
    }

    @Test
    fun `test delete person`() {
        runBlocking {
            // setup
            val person = personService.getById(3)
            assertThat(person).isNotNull
            // when
            personService.deleteById(3)
            // then
            assertThatThrownBy {
                runBlocking {
                    personService.getById(3)
                }
            }.isInstanceOf(PersonNotFoundException::class.java)
        }
    }

}

