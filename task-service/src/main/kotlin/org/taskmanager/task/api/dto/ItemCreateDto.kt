package org.taskmanager.task.api.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class ItemCreateDto(
    @field:NotBlank
    @field:Size(max = 4000)
    var description: String? = null,
    var assigneeUserId: String? = null,
    var tagIds: Set<Long>? = null,
)
