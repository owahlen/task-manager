package org.taskmanager.task.exception

class UserNotFoundException(id: Long) : NotFoundException(String.format("User [%d] was not found", id))
