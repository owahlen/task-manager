package org.taskmanager.task

import org.taskmanager.task.service.dto.TaskDTO


// builders

fun createTaskDTO(id: Long? = null, description: String = "a task", completed: Boolean = false) =
    TaskDTO(id, description, completed)
