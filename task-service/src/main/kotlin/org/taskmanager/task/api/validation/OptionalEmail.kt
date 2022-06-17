package org.taskmanager.task.api.validation

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.Pattern
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [OptionalEmailValidator::class])
@Target(FIELD)
@Retention(RUNTIME)
annotation class OptionalEmail(
    val message: String = "{javax.validation.constraints.Email.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    val regexp: String = ".*",
    val flags: Array<Pattern.Flag> = []
)
