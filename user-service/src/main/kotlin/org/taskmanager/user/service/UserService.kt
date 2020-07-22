package org.taskmanager.user.service

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.taskmanager.user.model.User
import org.taskmanager.user.model.UserDTO
import org.taskmanager.user.model.toModel
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.taskmanager.user.repository.UserRepository

@Service
class UserService(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {

    suspend fun findAll() = userRepository.findAll()
    suspend fun findById(id: Long) = userRepository.findById(id)
    suspend fun findByEmail(email: String) = userRepository.findByEmailIgnoreCase(email)
    suspend fun create(userDTO: UserDTO) = userRepository.save(userDTO.toModel(passwordEncoder))

    suspend fun update(id: Long, userDTO: UserDTO): User? {
        if(findById(id)==null) return null
        return userRepository.save(userDTO.toModel(passwordEncoder, withId = id))
    }

    suspend fun delete(id: Long): Boolean {
        if(findById(id)==null) return false
        userRepository.deleteById(id)
        return true
    }

    // inspired by https://www.naturalprogrammer.com/blog/16393/spring-security-get-current-user-programmatically
    suspend fun getCurrentUser(): Any? {
        return ReactiveSecurityContextHolder.getContext()
                .map { securityContext ->
                    securityContext.authentication.principal
                }
                .awaitFirstOrNull()
    }

}
