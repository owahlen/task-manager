package org.taskmanager.task.configuration

import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import javax.validation.ConstraintViolationException

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException): HttpStatus {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.constraintViolations.joinToString { it.message }, e)
    }

    @ExceptionHandler(TypeMismatchException::class)
    fun handleTypeMismatchException(e: TypeMismatchException): HttpStatus {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid value '${e.value}'", e)
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleWebExchangeBindException(e: WebExchangeBindException): HttpStatus {
        throw object : WebExchangeBindException(e.methodParameter!!, e.bindingResult) {
            override val message: String =
                    if (fieldError != null) "${fieldError?.field} [${fieldError?.rejectedValue}] ${fieldError?.defaultMessage}"
                    else "invalid parameters"
        }
    }

}
