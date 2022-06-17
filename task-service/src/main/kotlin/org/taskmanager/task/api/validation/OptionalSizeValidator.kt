package org.taskmanager.task.api.validation

import java.util.*
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class OptionalSizeValidator : ConstraintValidator<OptionalSize, Optional<String>> {
    private var min: Int = 0
    private var max: Int = Integer.MAX_VALUE

    override fun initialize(parameters: OptionalSize) {
        min = parameters.min
        max = parameters.max
    }

    override fun isValid(
        nameField: Optional<String>,
        context: ConstraintValidatorContext
    ): Boolean {
        return nameField.isEmpty() || (nameField.get().length in min..max)
    }
}