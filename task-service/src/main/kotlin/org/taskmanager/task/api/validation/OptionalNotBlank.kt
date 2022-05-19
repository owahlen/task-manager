package org.taskmanager.task.api.validation

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [OptionalNotBlankValidator::class])
@Target(FIELD)
@Retention(RUNTIME)
annotation class OptionalNotBlank(
    val message: String = "{javax.validation.constraints.NotBlank.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
