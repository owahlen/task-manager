package org.taskmanager.task.api.validation

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [OptionalSizeValidator::class])
@Target(FIELD)
@Retention(RUNTIME)
annotation class OptionalSize(
    val message: String = "{javax.validation.constraints.Size.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    val min: Int = 0,
    val max: Int = Integer.MAX_VALUE
)
