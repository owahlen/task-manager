package org.taskmanager.task.api.resource

import org.taskmanager.task.model.ItemStatus
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class ItemUpdateResource(
    @field:NotBlank
    @field:Size(max = 4000)
    var description: String? = null,

    @field:NotNull
    var status: ItemStatus? = null,

    var assigneeId: Long? = null,

    var tagIds: Set<Long>? = null,
)
