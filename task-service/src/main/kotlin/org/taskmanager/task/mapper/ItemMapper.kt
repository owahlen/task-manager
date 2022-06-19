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

fun ItemResource.toItem(assigneeId: Long?) = Item(
    id = this.id,
    version = this.version,
    description = this.description,
    status = this.status ?: ItemStatus.TODO,
    assigneeId = assigneeId,
    assignee = this.assignee?.toUser(),
    tags = this.tags?.map { it.toTag() },
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun ItemCreateResource.toItem(assigneeId: Long?) = Item(
    description = this.description,
    assigneeId = assigneeId,
    tags = this.tagIds?.map { Tag(id = it) }
)

fun ItemUpdateResource.toItem(id: Long, version: Long?, assigneeId: Long?) = Item(
    id = id,
    version = version,
    description = this.description,
    status = this.status!!,
    assigneeId = assigneeId,
    tags = this.tagIds?.map { Tag(id = it) }
)

fun ItemPatchResource.toItem(item: Item, assigneeId: Long?) = Item(
    id = item.id,
    version = item.version,
    description = this.description.orElse(item.description),
    status = this.status.orElse(item.status),
    assigneeId = assigneeId,
    tags = this.tagIds.map { it.map(::Tag) }.orElse(item.tags)
)