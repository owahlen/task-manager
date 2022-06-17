package org.taskmanager.task.exception

class UserNotFoundException(uuid: String) : NotFoundException(String.format("User [%d] was not found", uuid))
