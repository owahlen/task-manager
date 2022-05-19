package org.taskmanager.task.api.resource

import org.taskmanager.task.model.ItemStatus
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class ItemPatchResource(
    val description: Optional<@NotBlank @Size(max = 4000) String>? = null,
    val status: Optional<@NotNull ItemStatus>? = null,
    val assigneeId: Optional<Long>? = null,
    val tagIds: Optional<Set<Long>>? = null
)
