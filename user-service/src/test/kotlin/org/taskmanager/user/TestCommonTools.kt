package org.taskmanager.user

import org.taskmanager.user.model.User
import org.taskmanager.user.model.UserDTO

// extensions

fun User.toDto(
        username: String = this.email,
        password: String = this.password) = UserDTO(username, password)

// builders

fun createUser(id: Long? = null, email: String = "newUser@test.com", password: String = "encryptedPassword") =
        User(id, email, password)
