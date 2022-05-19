package org.taskmanager.task.exception

class UnexpectedTagVersionException(expectedVersion: Long, foundVersion: Long) :
    NotFoundException(
        String.format(
            "The tag has a different version than the expected one. Expected [%s], found [%s]",
            expectedVersion, foundVersion
        )
    )
