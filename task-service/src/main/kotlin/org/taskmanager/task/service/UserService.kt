package org.taskmanager.task.service

import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taskmanager.task.exception.UserNotFoundException
import org.taskmanager.task.exception.UnexpectedUserVersionException
import org.taskmanager.task.model.User
import org.taskmanager.task.repository.ItemRepository
import org.taskmanager.task.repository.ItemTagRepository
import org.taskmanager.task.repository.UserRepository


@Service
class UserService(
    private val userRepository: UserRepository,
    private val itemRepository: ItemRepository,
    private val itemTagRepository: ItemTagRepository
) {

    /**
     * Get a page of users
     * @param pageable page definition
     * @return Page of users
     */
    suspend fun findAllBy(pageable: Pageable): Page<User> {
        val dataPage = userRepository.findAllBy(pageable).toList()
        val total = userRepository.count()
        return PageImpl(dataPage, pageable, total)
    }

    /**
     * Get a user with version check
     * @param id id of the user
     * @param version if version is not null check with currently stored user
     * @return the currently stored user
     */
    suspend fun getById(id: Long, version: Long? = null): User {
        val user = userRepository.findById(id) ?: throw UserNotFoundException(id)
        if (version != null && version != user.version) {
            // Optimistic locking: pre-check
            throw UnexpectedUserVersionException(version, user.version!!)
        }
        return user
    }

    /**
     * Create a new user
     * @param user user to be created
     * @return the created user without the related entities
     */
    @Transactional
    suspend fun create(user: User): User {
        if (user.id != null || user.version != null) {
            throw IllegalArgumentException("When creating a user, the id and the version must be null")
        }
        return userRepository.save(user)
    }

    /**
     * Update a user with version check
     * @param user user to be updated; if user's version is not null check with currently stored user
     * @return the updated user
     */
    @Transactional
    suspend fun update(user: User): User {
        if (user.id == null) {
            throw IllegalArgumentException("When updating a user, the id must be provided")
        }
        // verify that the user with id exists and if version!=null then check that it matches
        val storedUser = getById(user.id, user.version)
        val userToSave = user.copy(version = storedUser.version, createdDate = storedUser.createdDate)
        // Save the user
        return userRepository.save(userToSave)
    }

    /**
     * Delete a user with version check
     * This method transitively deletes items and item-tags of the user
     * @param id id of the user to be deleted
     * @param version if not null check that version matches the version of the currently stored user
     */
    @Transactional
    suspend fun deleteById(id: Long, version: Long? = null) {
        // check that user with this id exists
        val user = getById(id, version)
        val itemsOfUser = itemRepository.findByAssigneeId(id).toList()
        itemsOfUser.forEach {
            itemTagRepository.deleteAllByItemId(it.id!!)
        }
        itemRepository.deleteByAssigneeId(id)
        userRepository.delete(user)
    }

}