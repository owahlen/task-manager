package org.taskmanager.task.mapper

import org.taskmanager.task.api.resource.*
import org.taskmanager.task.model.Item
import org.taskmanager.task.model.ItemStatus
import org.taskmanager.task.model.Tag

fun Item.toItemResource() = ItemResource(
    id = this.id,
    version = this.version,
    description = this.description,
    status = this.status,
    assignee = this.assignee?.toUserResource(),
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
    assignee = this.assignee?.toUser(),
    tags = this.tags?.map { it.toTag() },
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun ItemCreateResource.toItem() = Item(
    description = this.description,
    assigneeId = this.assigneeId,
    tags = this.tagIds?.map { Tag(id = it) }
)

fun ItemUpdateResource.toItem(id: Long, version: Long?) = Item(
    id = id,
    version = version,
    description = this.description,
    status = this.status!!,
    assigneeId = this.assigneeId,
    tags = this.tagIds?.map { Tag(id = it) }
)

fun ItemPatchResource.toItem(item: Item) = Item(
    id = item.id,
    version = item.version,
    description = this.description.orElse(item.description),
    status = this.status.orElse(item.status),
    assigneeId = this.assigneeId.orElse(item.assigneeId),
    tags = if (this.tagIds.isPresent()) {
        this.tagIds.get().map { Tag(id = it) }
    } else {
        item.tags
    }
)