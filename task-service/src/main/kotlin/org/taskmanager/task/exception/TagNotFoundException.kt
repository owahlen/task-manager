package org.taskmanager.task.exception

class TagNotFoundException(id: Long) : NotFoundException(String.format("Tag [%d] was not found", id))
