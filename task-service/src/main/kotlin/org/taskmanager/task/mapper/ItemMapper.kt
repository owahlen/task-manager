package org.taskmanager.task.mapper

import org.taskmanager.task.api.resource.ItemResource
import org.taskmanager.task.model.Item
import org.taskmanager.task.model.ItemStatus

fun Item.toItemResource() = ItemResource(
    id = this.id,
    version = this.version,
    description = this.description,
    status = this.status,
    assignee = this.assignee?.toPersonResource(),
    tags = this.tags?.map { it.toTagResource() } ?: listOf(),
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun ItemResource.toItem() = Item(
    id = this.id,
    version = this.version,
    description = this.description,
    status = this.status ?: ItemStatus.TODO,
    assigneeId = this.assignee?.id,
    assignee = this.assignee?.toPerson(),
    tags = this.tags.map { it.toTag() },
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

