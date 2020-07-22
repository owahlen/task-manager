package org.taskmanager.user.model

import org.springframework.security.crypto.password.PasswordEncoder
import javax.validation.constraints.NotBlank

data class UserDTO(
        @field:NotBlank
        val email: String?,
        var rawPassword: String?
)

fun UserDTO.toModel(passwordEncoder: PasswordEncoder, withId: Long? = null) : User {
        val password = passwordEncoder.encode(rawPassword)
        return User(withId, this.email!!, password)
}
