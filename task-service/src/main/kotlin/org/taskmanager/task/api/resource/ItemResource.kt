package org.taskmanager.task.api.resource

import org.taskmanager.task.model.ItemStatus
import org.taskmanager.task.model.Tag
import java.time.LocalDateTime

data class ItemResource(
    val id: Long? = null,
    val version: Long? = null,
    var description: String? = null,
    var status: ItemStatus? = null,
    var assignee: PersonResource? = null,
    var tags: List<TagResource>,
    val createdDate: LocalDateTime? = null,
    val lastModifiedDate: LocalDateTime? = null
)
