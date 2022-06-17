package org.taskmanager.task.service

import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.api.resource.UserPatchResource
import org.taskmanager.task.api.resource.UserResource
import org.taskmanager.task.api.resource.UserUpdateResource
import org.taskmanager.task.exception.UserNotFoundException
import org.taskmanager.task.exception.UnexpectedUserVersionException
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
    private val itemTagRepository: ItemTagRepository,
    @Value("\${keycloak.realm}")
    private val realm: String
) {

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
        val email = userCreateResource.email ?: throw java.lang.IllegalArgumentException(
            "When creating a user its email must be set"
        )
        val password = userCreateResource.password ?: throw java.lang.IllegalArgumentException(
            "When creating a user its password must be set"
        )
        val user = userCreateResource.toUser()
        user.uuid = keycloakUserService.create(email, password)
        return userRepository.save(user).toUserResource()
    }

    @Transactional
    suspend fun update(uuid: String, version: Long?, userUpdateResource: UserUpdateResource): UserResource {
        val user = userUpdateResource.toUser(uuid, version)
        return updateUser(user, userUpdateResource.password).toUserResource()
    }

    @Transactional
    suspend fun patch(uuid: String, version: Long?, userPatchResource: UserPatchResource): UserResource {
        val existingUser = getUserByUuid(uuid, version)
        val patchdUser = userPatchResource.toUser(existingUser)
        return updateUser(patchdUser, userPatchResource.password.orElse(null)).toUserResource()
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
        keycloakUserService.deleteByUuid(uuid)
        val userId = user.id!!
        val itemsOfUser = itemRepository.findByAssigneeId(userId).toList()
        itemsOfUser.forEach {
            itemTagRepository.deleteAllByItemId(it.id!!)
        }
        itemRepository.deleteByAssigneeId(userId)
        userRepository.delete(user)
    }

    private suspend fun getUserByUuid(uuid: String, version: Long? = null): User {
        val user = userRepository.findByUuid(uuid) ?: throw UserNotFoundException(uuid)
        if (version != null && version != user.version) {
            // Optimistic locking: pre-check
            throw UnexpectedUserVersionException(version, user.version!!)
        }
        return user
    }

    /**
     * Update a user with version check
     * @param user user to be updated; if user's version is not null check with currently stored user
     * @return the updated user
     */
    private suspend fun updateUser(user: User, password: String?): User {
        val uuid = user.uuid
        if (uuid == null) {
            throw IllegalArgumentException("When updating a user, the uuid must be provided")
        }
        // verify that the user with uuid exists and if version!=null then check that it matches
        val storedUser = getUserByUuid(uuid, user.version)
        val userToSave = user.copy(version = storedUser.version, createdDate = storedUser.createdDate)
        if (user.email != userToSave.email || password != null) {
            // the user has a new email or the password needs to be changed
            keycloakUserService.update(uuid, user.email, password)
        }
        // Save the user
        return userRepository.save(userToSave)
    }

}
