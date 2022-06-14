package org.taskmanager.task.repository

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.test.annotation.DirtiesContext
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.model.Person


@IntegrationTest
@DirtiesContext
class PersonRepositoryIntegrationTest(@Autowired val personRepository: PersonRepository) {

    @Test
    fun `test that persons can be loaded from db`() {
        runBlocking {
            // when
            val nPersons = personRepository.count()
            // then
            assertThat(nPersons).isGreaterThan(0)
            // when
            val existingPerson = personRepository.findById(1)
            // then
            assertThat(existingPerson).isNotNull()
            assertThat(existingPerson!!.id).isEqualTo(1)
            assertThat(existingPerson.version).isEqualTo(1)
            assertThat(existingPerson.firstName).isEqualTo("Richard")
            assertThat(existingPerson.lastName).isEqualTo("Countin")
            assertThat(existingPerson.createdDate).isNotNull()
            assertThat(existingPerson.lastModifiedDate).isNotNull()
        }
    }

    @Test
    fun `test creation, update and optimistic locking for persons`() {
        runBlocking {
            // when
            var existingPerson = personRepository.save(Person(firstName = "John", lastName = "Doe"))
            // then
            assertThat(existingPerson).isNotNull()
            assertThat(existingPerson.id).isNotNull()
            assertThat(existingPerson.version).isEqualTo(0)

            // when
            existingPerson.lastName = "Walker"
            existingPerson = personRepository.save(existingPerson)
            // then
            assertThat(existingPerson).isNotNull()
            assertThat(existingPerson.version).isEqualTo(1)

            // setup
            val personToUpdate = Person(id = existingPerson.id, version = 0, lastName = "Dalton")
            // When / Then
            assertThatThrownBy {
                runBlocking {
                    personRepository.save(personToUpdate)
                }
            }.isInstanceOf(OptimisticLockingFailureException::class.java)
        }
    }

    @Test
    fun `test findAllBy pageable returns page of persons`() {
        runBlocking {
            // setup
            val sort = Sort.by(Order.by("firstName"), Order.by("lastName"))
            val pageable = PageRequest.of(0, 100, sort)
            // when
            val persons = personRepository.findAllBy(pageable).toList()
            // then
            assertThat(persons.count()).isGreaterThan(2)
            val sortedPersons = persons.sortedWith(compareBy(Person::firstName, Person::lastName))
            assertThat(persons).isEqualTo(sortedPersons)
        }
    }
}
