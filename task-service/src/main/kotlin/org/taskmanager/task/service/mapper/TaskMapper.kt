package org.taskmanager.task.service.mapper

import org.taskmanager.task.domain.Task
import org.taskmanager.task.service.dto.TaskDTO

fun TaskDTO.toTask() = Task(this.id, this.description, this.completed)
fun Task.toTaskDTO() = TaskDTO(this.id, this.description, this.completed)
