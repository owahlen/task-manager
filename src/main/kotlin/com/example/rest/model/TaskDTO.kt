package com.example.rest.model

data class TaskDTO(
        val description: String,
        var completed: Boolean
)

fun TaskDTO.toModel(withId: Long? = null) = Task(withId, this.description, this.completed)
