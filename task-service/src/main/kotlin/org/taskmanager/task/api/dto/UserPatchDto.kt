package org.taskmanager.task.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.taskmanager.task.api.validation.OptionalEmail
import org.taskmanager.task.api.validation.OptionalNotBlank
import org.taskmanager.task.api.validation.OptionalSize
import java.util.*

data class UserPatchDto(
    // Kotlin has a limitation for validating bean validations in containers.
    // @see https://youtrack.jetbrains.com/issue/KT-26605
    // Therefore custom validators are used:
    @Schema(required = false, maxLength=256, description = "optional email")
    @OptionalNotBlank
    @OptionalSize(max = 256)
    @OptionalEmail
    val email: Optional<String>,

    @Schema(required = false, maxLength=100, description = "optional first name")
    @OptionalNotBlank
    @OptionalSize(max = 100)
    val firstName: Optional<String>,

    @Schema(required = false, maxLength=100, description = "optional last name")
    @OptionalNotBlank
    @OptionalSize(max = 100)
    val lastName: Optional<String>
)



