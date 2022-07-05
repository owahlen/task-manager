package org.taskmanager.task.mapper

import org.taskmanager.task.api.dto.*
import org.taskmanager.task.model.Item
import org.taskmanager.task.model.ItemStatus
import org.taskmanager.task.model.Tag

fun Item.toItemDto() = ItemDto(
    id = this.id,
    version = this.version,
    description = this.description,
    status = this.status,
    assignee = this.assignee?.toUserDto(),
    tags = this.tags?.map { it.toTagDto() } ?: listOf(),
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun ItemDto.toItem(assigneeId: Long?) = Item(
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

fun ItemCreateDto.toItem(assigneeId: Long?) = Item(
    description = this.description,
    assigneeId = assigneeId,
    tags = this.tagIds?.map { Tag(id = it) }
)

fun ItemUpdateDto.toItem(id: Long, version: Long?, assigneeId: Long?) = Item(
    id = id,
    version = version,
    description = this.description,
    status = this.status!!,
    assigneeId = assigneeId,
    tags = this.tagIds?.map { Tag(id = it) }
)

fun ItemPatchDto.toItem(item: Item, assigneeId: Long?) = Item(
    id = item.id,
    version = item.version,
    description = this.description.orElse(item.description),
    status = this.status.orElse(item.status),
    assigneeId = assigneeId,
    tags = this.tagIds.map { it.map(::Tag) }.orElse(item.tags)
)