package org.taskmanager.task

import org.taskmanager.task.model.Task
import org.taskmanager.task.model.TaskDTO

// extensions

fun Task.toDto(
        description: String = this.description,
        completed: Boolean = this.completed) = TaskDTO(description, completed)

// builders

fun createTask(id: Long? = null, description: String = "a task", completed: Boolean = false) =
        Task(id, description, completed)
