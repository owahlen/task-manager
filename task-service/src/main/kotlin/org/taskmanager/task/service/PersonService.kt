package org.taskmanager.task.service

import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taskmanager.task.exception.PersonNotFoundException
import org.taskmanager.task.exception.UnexpectedPersonVersionException
import org.taskmanager.task.model.Person
import org.taskmanager.task.repository.ItemRepository
import org.taskmanager.task.repository.ItemTagRepository
import org.taskmanager.task.repository.PersonRepository


@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val itemRepository: ItemRepository,
    private val itemTagRepository: ItemTagRepository
) {

    /**
     * Get a page of persons
     * @param pageable page definition
     * @return Page of persons
     */
    suspend fun findAllBy(pageable: Pageable): Page<Person> {
        val dataPage = personRepository.findAllBy(pageable).toList()
        val total = personRepository.count()
        return PageImpl(dataPage, pageable, total)
    }

    /**
     * Get a person with version check
     * @param id id of the person
     * @param version if version is not null check with currently stored person
     * @return the currently stored person
     */
    suspend fun getById(id: Long, version: Long? = null): Person {
        val person = personRepository.findById(id) ?: throw PersonNotFoundException(id)
        if (version != null && version != person.version) {
            // Optimistic locking: pre-check
            throw UnexpectedPersonVersionException(version, person.version!!)
        }
        return person
    }

    /**
     * Create a new person
     * @param person person to be created
     * @return the created person without the related entities
     */
    @Transactional
    suspend fun create(person: Person): Person {
        if (person.id != null || person.version != null) {
            throw IllegalArgumentException("When creating a person, the id and the version must be null")
        }
        return personRepository.save(person)
    }

    /**
     * Update a person with version check
     * @param person person to be updated; if person's version is not null check with currently stored person
     * @return the updated person
     */
    @Transactional
    suspend fun update(person: Person): Person {
        if (person.id == null) {
            throw IllegalArgumentException("When updating a person, the id must be provided")
        }
        // verify that the person with id exists and if version!=null then check that it matches
        val storedPerson = getById(person.id, person.version)
        val personToSave = person.copy(version = storedPerson.version, createdDate = storedPerson.createdDate)
        // Save the person
        return personRepository.save(personToSave)
    }

    /**
     * Delete a person with version check
     * This method transitively deletes items and item-tags of the person
     * @param id id of the person to be deleted
     * @param version if not null check that version matches the version of the currently stored person
     */
    @Transactional
    suspend fun deleteById(id: Long, version: Long? = null) {
        // check that person with this id exists
        val person = getById(id, version)
        val itemsOfPerson = itemRepository.findByAssigneeId(id).toList()
        itemsOfPerson.forEach {
            itemTagRepository.deleteAllByItemId(it.id!!)
        }
        itemRepository.deleteByAssigneeId(id)
        personRepository.delete(person)
    }

}