package org.taskmanager.task.exception

class UnexpectedUserVersionException(expectedVersion: Long, foundVersion: Long) :
    NotFoundException(
        String.format(
            "The user has a different version than the expected one. Expected [%s], found [%s]",
            expectedVersion, foundVersion
        )
    )
