package org.taskmanager.task.service

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taskmanager.task.api.dto.UserDto
import org.taskmanager.task.exception.UnexpectedUserVersionException
import org.taskmanager.task.exception.UserNotFoundException
import org.taskmanager.task.mapper.toUser
import org.taskmanager.task.mapper.toUserDto
import org.taskmanager.task.model.User
import org.taskmanager.task.repository.ItemRepository
import org.taskmanager.task.repository.ItemTagRepository
import org.taskmanager.task.repository.UserRepository
import javax.ws.rs.NotFoundException


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
        log.info("Synchronizing user with id '$userId' from Keycloak")

        val keycloakUserRepresentation = try {
            keycloakUserService.findById(userId)
        } catch (e: NotFoundException) {
            null
        }

        runBlocking {
            val localUser = userRepository.findByUserId(userId)
            if (keycloakUserRepresentation != null) {
                if (localUser != null) {
                    log.info("Updating user with id '$userId' after an update in Keycloak")
                    update(keycloakUserRepresentation)
                } else {
                    log.info("Creating user user with id '$userId' after creation in Keycloak")
                    create(keycloakUserRepresentation)
                }
            } else {
                if (localUser != null) {
                    log.info("Deleting user with id '$userId' after deletion from Keycloak")
                    delete(userId)
                } else null
            }
        }
    }

    /**
     * Get a page of users
     * @param pageable page definition
     * @return Page of users
     */
    suspend fun findAllBy(pageable: Pageable): Page<UserDto> {
        val dataPage = userRepository.findAllBy(pageable).toList()
        val total = userRepository.count()
        return PageImpl(dataPage, pageable, total).map(User::toUserDto)
    }

    /**
     * Get a user with version check
     * @param userId id of the user
     * @param version if version is not null check with currently stored user
     * @return the currently stored user
     */
    suspend fun getByUserId(userId: String, version: Long? = null): UserDto {
        return getUserByUserId(userId, version).toUserDto()
    }

    /**
     * Create a new user
     * @param user user to be created
     * @return the created user without the related entities
     */
    @Transactional
    protected suspend fun create(userRepresentation: UserRepresentation): UserDto {
        val user = userRepresentation.toUser()
        return userRepository.save(user).toUserDto()
    }

    @Transactional
    protected suspend fun update(userRepresentation: UserRepresentation): UserDto {
        val user = userRepresentation.toUser()
        return updateUser(user).toUserDto()
    }

    /**
     * Delete a user with version check
     * This method transitively deletes items and item-tags of the user
     * @param userId userId of the user to be deleted
     * @param version if not null check that version matches the version of the currently stored user
     */
    @Transactional
    protected suspend fun delete(userId: String, version: Long? = null) {
        // check that user with this id exists
        val user = getUserByUserId(userId, version)
        keycloakUserService.delete(userId)
        val userId = user.id!!
        val itemsOfUser = itemRepository.findByAssigneeId(userId).toList()
        itemsOfUser.forEach {
            itemTagRepository.deleteAllByItemId(it.id!!)
        }
        itemRepository.deleteByAssigneeId(userId)
        userRepository.delete(user)
    }

    private suspend fun getUserByUserId(userId: String, version: Long? = null): User {
        val user = userRepository.findByUserId(userId)
            ?: throw UserNotFoundException(userId)
        if (version != null && version != user.version) {
            // Optimistic locking: pre-check
            throw UnexpectedUserVersionException(version, user.version!!)
        }
        return user
    }

    /**
     * Update a user with version check
     * @param userId userId of the user to be updated
     * @param user object that contains the values to be updated
     * @return the updated user
     */
    private suspend fun updateUser(user: User): User {
        // verify that the user with userId exists and if version!=null then check that it matches
        val userId = user.userId ?: throw IllegalArgumentException("When updating a user a userId must be provided")
        val storedUser = getUserByUserId(userId, user.version)
        val userToSave = user.copy(
            id = storedUser.id,
            userId = storedUser.userId,
            version = storedUser.version,
            createdDate = storedUser.createdDate
        )
        // Save the user
        return userRepository.save(userToSave)
    }

}
