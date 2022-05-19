package org.taskmanager.task.exception

class ItemNotFoundException(id: Long) : NotFoundException(String.format("Item [%d] was not found", id))
