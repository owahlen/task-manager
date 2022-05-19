package org.taskmanager.task.exception

class UnexpectedItemVersionException(expectedVersion: Long, foundVersion: Long) :
    NotFoundException(
        String.format(
            "The item has a different version than the expected one. Expected [%s], found [%s]",
            expectedVersion, foundVersion
        )
    )
