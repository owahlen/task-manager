package org.taskmanager.task.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
abstract class NotFoundException internal constructor(message: String) : RuntimeException(message)
