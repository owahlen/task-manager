package org.taskmanager.task.api.resource

import javax.validation.constraints.NotNull

data class TaskDTO(
        val id: Long? = null,

        @field:NotNull
        val description: String?,

        @field:NotNull
        val completed: Boolean? = false
)

