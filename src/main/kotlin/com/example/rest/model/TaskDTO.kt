package com.example.rest.model

import javax.validation.constraints.NotBlank

data class TaskDTO(
        @field:NotBlank//(message = "description must not be empty")
        val description: String?,
        var completed: Boolean = false
)

fun TaskDTO.toModel(withId: Long? = null) = Task(withId, this.description!!, this.completed)
