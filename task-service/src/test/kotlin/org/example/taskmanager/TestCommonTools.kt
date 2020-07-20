package org.example.taskmanager

import org.example.taskmanager.model.Task
import org.example.taskmanager.model.TaskDTO

// extensions

fun Task.toDto(
        description: String = this.description,
        completed: Boolean = this.completed) = TaskDTO(description, completed)

// builders

fun createTask(id: Long? = null, description: String = "a task", completed: Boolean = false) = Task(id, description, completed)
