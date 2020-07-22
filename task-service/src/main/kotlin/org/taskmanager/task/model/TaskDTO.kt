package org.taskmanager.task.model

import javax.validation.constraints.NotBlank

data class TaskDTO(
        @field:NotBlank
        val description: String?,
        var completed: Boolean = false
)

fun TaskDTO.toModel(withId: Long? = null) = Task(withId, this.description!!, this.completed)
