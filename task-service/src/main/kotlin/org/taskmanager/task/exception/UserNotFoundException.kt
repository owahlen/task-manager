package org.taskmanager.task.exception

class UserNotFoundException(uuid: String) : NotFoundException(String.format("User [%s] was not found", uuid))
