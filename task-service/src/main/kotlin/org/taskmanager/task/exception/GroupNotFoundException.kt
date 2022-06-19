package org.taskmanager.task.exception

class GroupNotFoundException(groupName: String) : NotFoundException(String.format("Group [%d] was not found", groupName))
