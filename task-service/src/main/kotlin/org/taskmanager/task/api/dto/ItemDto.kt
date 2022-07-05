package org.taskmanager.task.api.dto

import org.taskmanager.task.model.ItemStatus
import java.time.LocalDateTime

data class ItemDto(
    val id: Long? = null,
    val version: Long? = null,
    var description: String? = null,
    var status: ItemStatus? = null,
    var assignee: UserDto? = null,
    var tags: List<TagDto>? = null,
    val createdDate: LocalDateTime? = null,
    val lastModifiedDate: LocalDateTime? = null
)
