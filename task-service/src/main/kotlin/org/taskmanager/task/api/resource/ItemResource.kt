package org.taskmanager.task.api.resource

import org.taskmanager.task.model.ItemStatus
import java.time.LocalDateTime

data class ItemResource(
    val id: Long? = null,
    val version: Long? = null,
    var description: String? = null,
    var status: ItemStatus? = null,
    var assignee: UserResource? = null,
    var tags: List<TagResource>? = null,
    val createdDate: LocalDateTime? = null,
    val lastModifiedDate: LocalDateTime? = null
)
