package org.taskmanager.task.service

import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.api.resource.UserPatchResource
import org.taskmanager.task.api.resource.UserResource
import org.taskmanager.task.api.resource.UserUpdateResource
import org.taskmanager.task.exception.UnexpectedUserVersionException
import org.taskmanager.task.exception.UserNotFoundException
import org.taskmanager.task.mapper.toUser
import org.taskmanager.task.mapper.toUserResource
import org.taskmanager.task.model.User
import org.taskmanager.task.repository.ItemRepository
import org.taskmanager.task.repository.ItemTagRepository
import org.taskmanager.task.repository.UserRepository


@Service
class UserService(
    private val keycloakUserService: KeycloakUserService,
    private val userRepository: UserRepository,
    private val itemRepository: ItemRepository,
    private val itemTagRepository: ItemTagRepository
) {

    private val log = LoggerFactory.getLogger(UserService::class.java)

    /**
     * Synchronize user from Keycloak
     */
    fun synchronizeUserFromKeycloak(userId: String) {
        // todo: delete, create, or update the user
        log.info("Synchronizing user with id '$userId' from Keycloak")
    }

    /**
     * Get a page of users
     * @param pageable page definition
     * @return Page of users
     */
    suspend fun findAllBy(pageable: Pageable): Page<UserResource> {
        val dataPage = userRepository.findAllBy(pageable).toList()
        val total = userRepository.count()
        return PageImpl(dataPage, pageable, total).map(User::toUserResource)
    }

    /**
     * Get a user with version check
     * @param uuid id of the user
     * @param version if version is not null check with currently stored user
     * @return the currently stored user
     */
    suspend fun getByUuid(uuid: String, version: Long? = null): UserResource {
        return getUserByUuid(uuid, version).toUserResource()
    }

    /**
     * Create a new user
     * @param user user to be created
     * @return the created user without the related entities
     */
    @Transactional
    suspend fun create(userCreateResource: UserCreateResource): UserResource {
        val uuid = keycloakUserService.create(userCreateResource)
        val user = userCreateResource.toUser()
        user.uuid = uuid
        return userRepository.save(user).toUserResource()
    }

    @Transactional
    suspend fun update(uuid: String, version: Long?, userUpdateResource: UserUpdateResource): UserResource {
        val user = userUpdateResource.toUser(uuid, version)
        keycloakUserService.update(uuid, userUpdateResource)
        return updateUser(uuid, user).toUserResource()
    }

    @Transactional
    suspend fun patch(uuid: String, version: Long?, userPatchResource: UserPatchResource): UserResource {
        val existingUser = getUserByUuid(uuid, version)
        val patchUser = userPatchResource.toUser(existingUser)
        keycloakUserService.patch(uuid, userPatchResource)
        return updateUser(uuid, patchUser).toUserResource()
    }

    /**
     * Delete a user with version check
     * This method transitively deletes items and item-tags of the user
     * @param uuid uuid of the user to be deleted
     * @param version if not null check that version matches the version of the currently stored user
     */
    @Transactional
    suspend fun delete(uuid: String, version: Long? = null) {
        // check that user with this id exists
        val user = getUserByUuid(uuid, version)
        keycloakUserService.delete(uuid)
        val userId = user.id!!
        val itemsOfUser = itemRepository.findByAssigneeId(userId).toList()
        itemsOfUser.forEach {
            itemTagRepository.deleteAllByItemId(it.id!!)
        }
        itemRepository.deleteByAssigneeId(userId)
        userRepository.delete(user)
    }

    private suspend fun getUserByUuid(uuid: String, version: Long? = null): User {
        val user = userRepository.findByUuid(uuid)
            ?: throw UserNotFoundException(uuid)
        if (version != null && version != user.version) {
            // Optimistic locking: pre-check
            throw UnexpectedUserVersionException(version, user.version!!)
        }
        return user
    }

    /**
     * Update a user with version check
     * @param uuid uuid of the user to be updated
     * @param user object that contains the values to be updated
     * @return the updated user
     */
    private suspend fun updateUser(uuid: String, user: User): User {
        // verify that the user with uuid exists and if version!=null then check that it matches
        val storedUser = getUserByUuid(uuid, user.version)
        val userToSave = user.copy(
            id = storedUser.id,
            uuid = storedUser.uuid,
            version = storedUser.version,
            createdDate = storedUser.createdDate
        )
        // Save the user
        return userRepository.save(userToSave)
    }

}
