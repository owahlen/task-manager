package org.taskmanager.task.exception

class UnexpectedPersonVersionException(expectedVersion: Long, foundVersion: Long) :
    NotFoundException(
        String.format(
            "The person has a different version than the expected one. Expected [%s], found [%s]",
            expectedVersion, foundVersion
        )
    )
