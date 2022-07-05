package org.taskmanager.task.api.dto

import java.time.LocalDateTime

data class UserDto(
    val userId: String? = null,
    val version: Long? = null,
    var email: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    val createdDate: LocalDateTime? = null,
    val lastModifiedDate: LocalDateTime? = null
)
