package org.taskmanager.task.api.resource

import java.time.LocalDateTime

data class UserResource(
    val uuid: String? = null,
    val version: Long? = null,
    var email: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    val createdDate: LocalDateTime? = null,
    val lastModifiedDate: LocalDateTime? = null
)
