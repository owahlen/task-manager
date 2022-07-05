package org.taskmanager.task.mapper

import org.taskmanager.task.api.dto.*
import org.taskmanager.task.model.Tag

fun Tag.toTagDto() = TagDto(
    id = this.id,
    version = this.version,
    name = this.name,
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun TagDto.toTag() = Tag(
    id = this.id,
    version = this.version,
    name = this.name,
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun TagCreateDto.toTag() = Tag(
    name = this.name
)

fun TagUpdateDto.toTag(id: Long, version: Long?) = Tag(
    id = id,
    version = version,
    name = this.name
)

fun TagPatchDto.toTag(tag: Tag) = Tag(
    id = tag.id,
    version = tag.version,
    name = this.name.orElse(tag.name)
)
