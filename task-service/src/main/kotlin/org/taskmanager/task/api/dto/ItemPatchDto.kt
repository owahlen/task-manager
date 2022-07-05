package org.taskmanager.task.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.taskmanager.task.api.validation.OptionalNotBlank
import org.taskmanager.task.api.validation.OptionalSize
import org.taskmanager.task.model.ItemStatus
import java.util.*

data class ItemPatchDto(
    // Kotlin has a limitation for validating bean validations in containers.
    // @see https://youtrack.jetbrains.com/issue/KT-26605
    // Therefore custom validators are used:
    @Schema(required = false, maxLength = 4000, description = "optional description")
    @OptionalNotBlank
    @OptionalSize(max = 4000)
    val description: Optional<String>,

    val status: Optional<ItemStatus>,

    val assigneeUserId: Optional<String>,

    val tagIds: Optional<Set<Long>>
)
