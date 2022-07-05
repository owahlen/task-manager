package org.taskmanager.task.api.dto

import java.time.LocalDateTime

data class TagDto(
    val id: Long? = null,
    val version: Long? = null,
    var name: String? = null,
    val createdDate: LocalDateTime? = null,
    val lastModifiedDate: LocalDateTime? = null
)
