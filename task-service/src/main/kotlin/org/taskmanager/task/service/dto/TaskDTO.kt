package org.taskmanager.task.service.dto

import javax.validation.constraints.NotNull

data class TaskDTO(
        val id: Long? = null,

        @field:NotNull
        val description: String?,

        @field:NotNull
        val completed: Boolean? = false
)

