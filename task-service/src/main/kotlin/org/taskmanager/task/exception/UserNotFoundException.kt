package org.taskmanager.task.exception

class UserNotFoundException(userId: String) : NotFoundException(String.format("User [%s] was not found", userId))
