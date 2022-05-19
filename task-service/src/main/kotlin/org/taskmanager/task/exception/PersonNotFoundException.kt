package org.taskmanager.task.exception

class PersonNotFoundException(id: Long) : NotFoundException(String.format("Person [%d] was not found", id))
