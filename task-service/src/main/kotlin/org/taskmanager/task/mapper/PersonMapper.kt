package org.taskmanager.task.mapper

import org.taskmanager.task.api.resource.PersonCreateResource
import org.taskmanager.task.api.resource.PersonPatchResource
import org.taskmanager.task.api.resource.PersonResource
import org.taskmanager.task.api.resource.PersonUpdateResource
import org.taskmanager.task.model.Person

fun Person.toPersonResource() = PersonResource(
    id = this.id,
    version = this.version,
    firstName = this.firstName,
    lastName = this.lastName,
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun PersonResource.toPerson() = Person(
    id = this.id,
    version = this.version,
    firstName = this.firstName,
    lastName = this.lastName,
    createdDate = this.createdDate,
    lastModifiedDate = this.lastModifiedDate
)

fun PersonCreateResource.toPerson() = Person(
    firstName = this.firstName,
    lastName = this.lastName,
)

fun PersonUpdateResource.toPerson(id: Long, version: Long?) = Person(
    id = id,
    version = version,
    firstName = this.firstName,
    lastName = this.lastName,
)

fun PersonPatchResource.toPerson(person: Person) = Person(
    id = person.id,
    version = person.version,
    firstName = this.firstName.orElse(person.firstName),
    lastName = this.lastName.orElse(person.lastName)
)