package org.taskmanager.task.api.resource

import java.time.LocalDateTime

data class TagResource(
    val id: Long? = null,
    val version: Long? = null,
    var name: String? = null,
    val createdDate: LocalDateTime? = null,
    val lastModifiedDate: LocalDateTime? = null
)
