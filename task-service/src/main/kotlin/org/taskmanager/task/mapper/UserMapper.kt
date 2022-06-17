package org.taskmanager.task.mapper

import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.api.resource.UserPatchResource
import org.taskmanager.task.api.resource.UserResource
import org.taskmanager.task.api.resource.UserUpdateResource
import org.taskmanager.task.model.User

fun User.toUserResource() = UserResource(
    uuid = this.uuid,
    version = this.version,
    email = this.email,
    firstName = this.firstName,
    lastName = this.lastName,
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun UserResource.toUser() = User(
    uuid = this.uuid,
    version = this.version,
    email = this.email,
    firstName = this.firstName,
    lastName = this.lastName,
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun UserCreateResource.toUser() = User(
    email = this.email,
    firstName = this.firstName,
    lastName = this.lastName,
)

fun UserUpdateResource.toUser(uuid: String, version: Long?) = User(
    uuid = uuid,
    version = version,
    email = this.email,
    firstName = this.firstName,
    lastName = this.lastName,
)

fun UserPatchResource.toUser(user: User) = User(
    id = user.id,
    version = user.version,
    uuid = user.uuid,
    email = this.email.orElse(user.email),
    firstName = this.firstName.orElse(user.firstName),
    lastName = this.lastName.orElse(user.lastName)
)