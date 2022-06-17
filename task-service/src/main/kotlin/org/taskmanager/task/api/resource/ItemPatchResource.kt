package org.taskmanager.task.api.resource

import io.swagger.v3.oas.annotations.media.Schema
import org.taskmanager.task.api.validation.OptionalNotBlank
import org.taskmanager.task.api.validation.OptionalSize
import org.taskmanager.task.model.ItemStatus
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class ItemPatchResource(
    // Kotlin has a limitation for validating bean validations in containers.
    // @see https://youtrack.jetbrains.com/issue/KT-26605
    // Therefore custom validators are used:
    @Schema(required = false, maxLength = 4000, description = "optional description")
    @OptionalNotBlank
    @OptionalSize(max = 4000)
    val description: Optional<String>,

    val status: Optional<ItemStatus>,

    val assigneeUuid: Optional<String>,

    val tagIds: Optional<Set<Long>>
)
