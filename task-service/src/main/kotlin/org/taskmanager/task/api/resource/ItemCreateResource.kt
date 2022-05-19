package org.taskmanager.task.api.resource

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class ItemCreateResource(
    @field:NotBlank
    @field:Size(max = 4000)
    var description: String? = null,
    var assigneeId: Long? = null,
    var tagIds: Set<Long>,
)
