package org.taskmanager.task.mapper

import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.api.resource.UserPatchResource
import org.taskmanager.task.api.resource.UserResource
import org.taskmanager.task.api.resource.UserUpdateResource
import org.taskmanager.task.model.User

fun User.toUserResource() = UserResource(
    id = this.id,
    version = this.version,
    firstName = this.firstName,
    lastName = this.lastName,
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun UserResource.toUser() = User(
    id = this.id,
    version = this.version,
    firstName = this.firstName,
    lastName = this.lastName,
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun UserCreateResource.toUser() = User(
    firstName = this.firstName,
    lastName = this.lastName,
)

fun UserUpdateResource.toUser(id: Long, version: Long?) = User(
    id = id,
    version = version,
    firstName = this.firstName,
    lastName = this.lastName,
)

fun UserPatchResource.toUser(user: User) = User(
    id = user.id,
    version = user.version,
    firstName = this.firstName.orElse(user.firstName),
    lastName = this.lastName.orElse(user.lastName)
)