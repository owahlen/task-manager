package org.taskmanager.task.mapper

import org.keycloak.representations.idm.UserRepresentation
import org.taskmanager.task.api.dto.UserCreateDto
import org.taskmanager.task.api.dto.UserDto
import org.taskmanager.task.api.dto.UserUpdateDto
import org.taskmanager.task.model.User

fun User.toUserDto() = UserDto(
    userId = this.userId,
    version = this.version,
    email = this.email,
    firstName = this.firstName,
    lastName = this.lastName,
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun UserDto.toUser() = User(
    userId = this.userId,
    version = this.version,
    email = this.email,
    firstName = this.firstName,
    lastName = this.lastName,
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun UserCreateDto.toUser() = User(
    email = this.email,
    firstName = this.firstName,
    lastName = this.lastName,
)

fun UserUpdateDto.toUser(userId: String, version: Long?) = User(
    userId = userId,
    version = version,
    email = this.email,
    firstName = this.firstName,
    lastName = this.lastName,
)

fun UserRepresentation.toUser() = User(
    userId = this.id,
    email = this.email?:this.username,
    firstName = this.firstName,
    lastName = this.lastName
)
