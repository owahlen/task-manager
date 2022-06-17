package org.taskmanager.task.api.validation

import org.hibernate.validator.internal.constraintvalidators.AbstractEmailValidator
import java.util.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class OptionalEmailValidator : ConstraintValidator<OptionalEmail, Optional<String>> {

    private val delegateEmailValidator = object: AbstractEmailValidator<OptionalEmail>() {}
    private var pattern: Pattern? = null

    override fun initialize(parameters: OptionalEmail) {
        delegateEmailValidator.initialize(parameters)

        val flags = parameters.flags
        var intFlag = 0
        for (flag in flags) {
            intFlag = intFlag or flag.value
        }

        // we only apply the regexp if there is one to apply
        if (".*" != parameters.regexp || parameters.flags.isNotEmpty()) {
            pattern = try {
                Pattern.compile(parameters.regexp, intFlag)
            } catch (e: PatternSyntaxException) {
                throw java.lang.IllegalArgumentException("Invalid regular expression.")
            }
        }
    }

    override fun isValid(
        nameField: Optional<String>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (nameField.isEmpty) return true
        val value = nameField.get()

        val isValid = delegateEmailValidator.isValid(value, context)
        if (pattern == null || !isValid) {
            return isValid
        }

        val m = pattern!!.matcher(value)
        return m.matches()
    }
}