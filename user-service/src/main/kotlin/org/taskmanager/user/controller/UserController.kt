package org.taskmanager.user.controller

import kotlinx.coroutines.flow.Flow
import org.taskmanager.user.model.User
import org.taskmanager.user.model.UserDTO
import org.taskmanager.user.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid


@RestController
@RequestMapping("/api")
class UserController(private val userService: UserService) {
    private val logger = LoggerFactory.getLogger(UserController::class.java)

    @GetMapping("/user")
    suspend fun findAll(): Flow<User> {
        return userService.findAll()
    }

    @GetMapping("/user/search")
    suspend fun search(@RequestParam criterion: Map<String, String?>): Flow<User> {
        if (criterion.containsKey("email")) {
            val email = criterion.get("email")
            if (email==null || email.isBlank()) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "search criterion for 'email' must not be empty")
            }
            return userService.findByEmail(email)
        } else {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The search parameter 'email' must be provided")
        }
    }

    @GetMapping("/user/{id}")
    suspend fun findUser(@PathVariable id: String): User {
        val entityId = id.toLongOrNull()
        if (entityId == null || entityId < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User id must be a positive integer")
        return userService.findById(entityId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with id '${entityId}' could not found")
    }

    @PostMapping("/user")
    suspend fun createUser(@Valid @RequestBody UserDTO: UserDTO): User {
        return userService.create(UserDTO)
    }

    @PutMapping("/user/{id}")
    suspend fun updateUser(@PathVariable id: String, @Valid @RequestBody UserDTO: UserDTO): User {
        val entityId = id.toLongOrNull()
        if (entityId == null || entityId < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User id must be a positive integer")
        return userService.update(entityId, UserDTO)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with id '$id' not found")
    }

    @DeleteMapping("/user/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteUser(@PathVariable id: String) {
        val principal = userService.getCurrentUser()
        val entityId = id.toLongOrNull()
        if (entityId == null || entityId < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User id must be a positive integer")
        if (!userService.delete(entityId)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with id '$id' not found")
    }
}
