package org.taskmanager.task.api.validation

import java.util.*
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class OptionalNotBlankValidator : ConstraintValidator<OptionalNotBlank, Optional<String>> {
    override fun initialize(parameters: OptionalNotBlank) {}

    override fun isValid(
        nameField: Optional<String>,
        cxt: ConstraintValidatorContext
    ): Boolean {
        return nameField.isEmpty() || nameField.get().trim().isNotEmpty()
    }
}
