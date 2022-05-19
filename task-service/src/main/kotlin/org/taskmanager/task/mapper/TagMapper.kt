package org.taskmanager.task.mapper

import org.taskmanager.task.api.resource.*
import org.taskmanager.task.model.Person
import org.taskmanager.task.model.Tag

fun Tag.toTagResource() = TagResource(
    id = this.id,
    version = this.version,
    name = this.name,
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun TagResource.toTag() = Tag(
    id = this.id,
    version = this.version,
    name = this.name,
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun TagCreateResource.toTag() = Tag(
    name = this.name
)

fun TagUpdateResource.toTag(id: Long, version: Long?) = Tag(
    id = id,
    version = version,
    name = this.name
)

fun TagPatchResource.toTag(tag: Tag) = Tag(
    id = tag.id,
    version = tag.version,
    name = this.name.orElse(tag.name)
)
